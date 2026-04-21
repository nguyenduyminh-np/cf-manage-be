# IMPLEMENTATION PLAN - MODULE BREAKDOWN

Tai lieu nay liet ke cac module can cai dat business theo thiet ke FINAL V3.
Moi module gom 3 phan: Story, Subtask ky thuat, Acceptance criteria.

## Software Requirement Specification:

- Các module triển khai state machine : yêu cầu tạo folder mới tron project source, không code trong service.
- folder service: là nơi CHỈ CHO PHÉP các thao tác CRUD, sử dụng các validator, rule, state machine theo đúng business. Các phần như validator, rule, state machine triển khai code logic ở các folder riêng (validator, rule, state machine, ...) . Tầng service chỉ call, bind input và sử dụng các hàm này trong flow.
- Các thao tác INSERT/UPDATE phải sử dụng Transaction dể đảm bảo tính ACID của dữ liệu.
- Các Exception do business (validator, rule, state machine) bắn ra phải được handle bằng cách custom Exception riêng với mã lỗi HTTP chuẩn , tham khảo cách cấu hình hiện tại trong : 'src\main\java\com\duyminhdev\cf_manager\exceptions'
- Các câu lệnh select phức tạp (Gom nhóm từ 2 bảng trở lên, kiểm tra dữ liệu, tìm kiếm động dynamic search, ...) hãy sử dụng kĩ thuật native query: Viết câu lệnh SQL trên java , call xuống DB. Dữ liệu được fetch phải được map vào DTO riêng custom cho từng nghiệp vụ, phân bổ vào các module tương ứng trong src\main\java\com\duyminhdev\cf_manager\dto (có thể tạo folder nếu chưa có).
- Các chuỗi string/giá trị dùng lặp lại nhiều lần trong project cần khai báo tập trung thành các CONSTANT trong src\main\java\com\duyminhdev\cf_manager\constant (có thể tạo file gom nhóm các constant theo module/business).
- Tập trung enum vào thư mục enums
- Tuân thủ API contract trong: src\main\java\com\duyminhdev\cf_manager\dto\base.

## Module 1 - Domain Model va Status Chuan

Story:
Chuan hoa domain cho booking/table/deposit de dap ung day du Rule 1-18 va tranh logic workaround o service.

Subtask ky thuat:

1. Bo sung `CHECKED_IN` vao `BookingStatusEnum`.
2. Chuan hoa mapping status code trong mapper/validator.
3. Bo sung field deposit can thiet trong `TableBooking` (`depositPaid`, `depositPaidAt`, `isDepositForfeited`, `depositTxnRef`).
4. Dam bao cac field time (`expectedArriveTime`, `expectedCheckOut`, `checkInAt`, `checkOutAt`) duoc validate co ban.
5. Can nhac bo sung truong version de ho tro optimistic locking.

Acceptance criteria:

1. Domain compile pass, khong vo enum parse/runtime mapping.
2. Co the luu doc cac trang thai `PENDING_CONFIRMATION`, `CONFIRMED`, `CHECKED_IN`, `COMPLETED`, `CANCELLED`, `EXPIRED`.
3. Du lieu deposit du thong tin de xu ly forfeit/refund theo policy.

## Module 2 - Booking State Machine

Story:
Tap trung hoa transition trang thai booking de dam bao tat ca flow di dung quy tac nghiep vu.

Subtask ky thuat:

1. Tao `BookingStateMachine` voi danh sach transition hop le.
2. Tao method transition trung tam (vi du `transition(current, target, context)`).
3. Ap dung guard chung: ton tai booking, active, lock table, re-validate rule.
4. Refactor service de khong set status truc tiep.
5. Chuan hoa exception message cho transition fail.

Acceptance criteria:

1. Khong con set `bookingStatus` truc tiep trong flow business chinh.
2. Moi transition sai deu fail voi business error ro nghia.
3. Unit test transition pass cho toan bo cap hop le/khong hop le.

## Module 3 - Rule Validator Chain

Story:
Tach rule nghiep vu thanh cac validator doc lap de de test, de mo rong va de doc.

Subtask ky thuat:

1. Tao `BookingValidationRule` va `BookingRuleValidatorChain`.
2. Cai dat cac validator: `AdvanceBooking`, `Duration`, `NoConflict`, `Deposit`, `EarlyArrival`, `LateArrival`, `Extension`, `WalkInGuard`, `PendingConfirmationAdvisory`, `BookingOwnership`, `WalkInPreAssignWarning`.
3. Khai bao validator sequence theo use-case: create, confirm, check-in, extend, walk-in, late-arrival->walk-in.
4. Chuan hoa `BookingContext` gom du lieu booking/table/time/force/warnings.

Acceptance criteria:

1. Moi rule co unit test rieng.
2. Validator chain chay dung thu tu cho tung use-case.
3. Rule 11, 12, 16, 14 duoc enforce dung policy da chot.

## Module 4 - Locking va Transaction Orchestration

Story:
Dam bao khong race condition khi nhieu nhan vien thao tac cung ban/cung khung gio.

Subtask ky thuat:

1. Tao `BookingLockService` dung Redis/Redisson key `lock:table:{tableId}`.
2. Ap dung lock cho create/confirm/check-in/check-out/cancel/extend/walk-in/update doi ban.
3. Doi ban lock 2 table theo thu tu id tang dan de tranh deadlock.
4. Chuan transaction boundary: lock -> load -> validate -> transition -> save -> publish event -> commit -> notify after commit.

Acceptance criteria:

1. Integration test concurrent pass cho Rule 15 va Rule 17.
2. Khong co lost update khi concurrent mutate booking/table.
3. Notify khong bi gui neu transaction rollback.

## Module 5 - Booking Use-case Service

Story:
Refactor service business de map truc tiep vao cac use-case nghiep vu, thay vi update status chung chung.

Subtask ky thuat:

1. Xay dung use-case methods: createBooking, confirmBooking, checkIn, checkOut, cancelBooking, extendBooking, createWalkIn, createWalkInFromLateArrival.
2. Rule 11: ownership check khi check-in som o ban BOOKED.
3. Rule 12: expire booking cu va tao walk-in moi co re-check `NoConflict` + `WalkInGuard`.
4. Rule 14: khong order sau 20p -> `CANCELLED` + refund deposit.
5. Rule 13: safe mode tu choi gia han khi conflict, soft mode optional qua config.

Acceptance criteria:

1. API/service behavior map dung Rule 1-18.
2. Khong check-in duoc khi den muon >30p neu chua qua flow expire + tao moi.
3. Chinh sach deposit trong Rule 14 dung nhu da chot.

## Module 6 - Scheduler Jobs

Story:
Tu dong hoa cac tinh huong theo thoi gian de van hanh nha hang on dinh.

Subtask ky thuat:

1. Job A: reserve truoc 30p (BOOKED).
2. Job B: no-show expire sau 30p.
3. Job C: canh bao do khi ket ban.
4. Job D: canh bao 10p va auto cancel 20p neu khong order.
5. Job E: canh bao sap checkout 15p.
6. Them ShedLock va idempotency key/de-dup de tranh xu ly lap.

Acceptance criteria:

1. Moi job chay an toan tren multi-instance.
2. Khong spam canh bao lap vo han cho cung booking/table.
3. Rule 3, 7, 8, 14, 16 duoc cover bang integration test.

## Module 7 - Repository va Query Layer

Story:
Bo sung query chinh xac cho conflict check, next booking lookup va scheduler windows.

Subtask ky thuat:

1. Them query `findConfirmedBookingsComingInWindow(start, end)`.
2. Them query `findConfirmedBookingsOnTableBetween(tableId, start, end)`.
3. Them query `findNextConfirmedBookingOnTable(tableId, afterTime)`.
4. Them query `findConfirmedNoShowCandidates(now)`.
5. Them query `findCheckedInWithoutOrderOlderThan(minutes)`.
6. Them query `existsConflictBookingOnTable(tableId, start, end, statuses)`.
7. Them query `findPendingConfirmationsOnTableAfter(tableId, now)`.
8. Bo sung index: `idx_tb_table_status_arrive`, `idx_tb_active_status_arrive`, `idx_tb_table_arrive_status`.

Acceptance criteria:

1. Query tra ket qua dung theo test data matrix.
2. Explain plan khong full scan o query nong.
3. Thoi gian query conflict/next booking dat muc chap nhan theo SLA noi bo.

## Module 8 - API va Contract

Story:
Cung cap API ro nghia theo action nghiep vu, van giu tuong thich endpoint cu.

Subtask ky thuat:

1. Giu endpoint legacy `/api/v1/table-booking/*`.
2. Bo sung action endpoints: confirm/check-in/check-out/cancel/deposit/extend/walk-in.
3. Map endpoint `update-status` vao state machine action neu van phai giu.
4. Chuan hoa response code/message theo business scenario.

Acceptance criteria:

1. FE goi API theo action, khong can tu suy luan transition.
2. Legacy endpoint van hoat dong de tranh break FE cu.
3. API test pass cho happy path va edge case quan trong.

## Module 9 - Event va Realtime Notification

Story:
Thong bao realtime dung trang thai DB, dung cau truc JSON on dinh cho FE/POS.

Subtask ky thuat:

1. Chuan hoa event payload JSON cho `/topic/table-status`, `/topic/booking-updates`, `/topic/table-alerts`, `/topic/deposit-events`.
2. Publish domain event trong transaction.
3. Gui WebSocket o `AFTER_COMMIT` listener.
4. Them event id/de-dup key neu can de tranh client xu ly trung.

Acceptance criteria:

1. Khong co truong hop FE nhan event thanh cong nhung DB rollback.
2. Payload event dong nhat va de parse.
3. Realtime scenarios pass qua integration test.

## Module 10 - Test Strategy va UAT Readiness

Story:
Dam bao chat luong truoc go-live va giam rui ro regression.

Subtask ky thuat:

1. Unit test cho state machine va tung validator.
2. Integration test cho lock concurrency, scheduler, transaction rollback.
3. API test end-to-end theo Rule 1-18.
4. Soak test nhe cho cac query va scheduler jobs.
5. Tao checklist UAT cho POS/FE.

Acceptance criteria:

1. Test suite pass on CI.
2. Rule matrix 1-18 duoc cover day du.
3. Co bien ban UAT pass truoc khi release production.

## De xuat thu tu implement

1. Module 1 -> 2 -> 3
2. Module 4 -> 5
3. Module 7
4. Module 6 -> 9
5. Module 8
6. Module 10 va UAT
