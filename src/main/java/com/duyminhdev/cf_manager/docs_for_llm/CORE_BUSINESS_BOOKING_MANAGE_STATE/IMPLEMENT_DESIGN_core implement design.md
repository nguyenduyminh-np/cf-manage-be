# CORE IMPLEMENT DESIGN - FINAL V3

## 1) Muc tieu

Tai lieu nay la ban final de implement day du flow dat ban theo BUSINESS_ANALYS_matrix rule and business flow, cover toan bo happy-case va edge-case (Rule 1 -> Rule 18), tren nen project hien tai:

- Java 21 + Spring Boot 3.x
- MySQL 8
- Spring Data JPA
- Redis + Redisson (distributed lock + cache)
- Spring Scheduler + ShedLock
- WebSocket/STOMP (notify realtime)

Tai lieu uu tien tinh kha thi voi codebase hien tai (`TableBookingServiceImpl`, `ServiceSupport`, `TableBookingRepository`, `TableBookingController`) va bo sung cac diem bat buoc de dung nghiep vu.

---

## 2) Chuan hoa domain cho project hien tai

### 2.1 Booking status

Business flow bat buoc co cac trang thai sau:

- `PENDING_CONFIRMATION`
- `CONFIRMED`
- `CHECKED_IN`
- `COMPLETED`
- `CANCELLED`
- `EXPIRED`

### 2.2 Table status

- `AVAILABLE`
- `BOOKED`
- `OCCUPIED`

### 2.3 Ghi chu mapping voi code hien tai

Code hien tai da co enum `BookingStatusEnum` nhung chua co `CHECKED_IN`.
Ban final yeu cau bo sung `CHECKED_IN` vao enum va toan bo nhung noi parse/validate label enum de dong bo voi matrix business.

---

## 3) State machine bat buoc

## 3.1 Transition hop le

- `PENDING_CONFIRMATION -> CONFIRMED`
- `PENDING_CONFIRMATION -> CANCELLED`
- `CONFIRMED -> CHECKED_IN`
- `CONFIRMED -> CANCELLED`
- `CONFIRMED -> EXPIRED`
- `CHECKED_IN -> COMPLETED`
- `CHECKED_IN -> CANCELLED` (case khong order sau 20 phut)

Tat ca transition phai qua 1 ham trung tam (vi du `BookingStateMachine.transition(...)`), khong set status truc tiep trong service.

## 3.2 Guard chung

Moi transition deu bat buoc check:

- booking ton tai va `is_active = 1`
- lock theo table
- version/updated_at de tranh lost-update (neu dung optimistic locking)
- re-validate business rule tai thoi diem transition

---

## 4) Rule engine va diem chen trong flow

Dung `RuleValidatorChain` gom cac validator sau:

1. `AdvanceBookingValidator`
2. `DurationValidator`
3. `NoConflictValidator`
4. `DepositValidator`
5. `EarlyArrivalValidator`
6. `LateArrivalValidator`
7. `ExtensionValidator`
8. `WalkInGuardValidator`
9. `PendingConfirmationAdvisoryValidator`
10. `BookingOwnershipValidator` (Rule 11)
11. `WalkInPreAssignWarningValidator` (Rule 16 proactive warning)

### 4.1 Thoi diem chay validator

- `createBooking`: 1,2,3
- `confirmBooking`: 3,4 (re-check conflict tai luc confirm)
- `checkIn`: 5,6,10
- `extendBooking`: 7
- `createWalkIn`: 8,9,11
- `createWalkInFromLateArrival` (Rule 12): 3,8,9,11

---

## 5) Matrix implement day du Rule 1 -> Rule 18

Bang duoi day la nguon su that implement:

| Rule | Trigger                                            | Dieu kien                                                                                              | Ket qua bat buoc                                                                                                                                                                      |
| ---- | -------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | Tao booking moi                                    | `expected_arrive_time >= now + 2h`, set `expected_check_out = expected_arrive_time + 2h` (hoac policy) | Tao `PENDING_CONFIRMATION`, ban van `AVAILABLE`                                                                                                                                       |
| 2    | Confirm booking                                    | Booking hop le, dat coc theo policy, khong conflict                                                    | `CONFIRMED`, ban van `AVAILABLE`                                                                                                                                                      |
| 3    | Job sap den gio (30p)                              | Booking `CONFIRMED` co `expected_arrive_time` trong 30p, ban dang `AVAILABLE`                          | Chuyen ban thanh `BOOKED`, push thong bao reserve                                                                                                                                     |
| 4    | Check-in dung gio/trong +-30p                      | Booking `CONFIRMED`, `now <= expected_arrive_time + 30p`                                               | Chuyen `CHECKED_IN`, ban `OCCUPIED`, giu nguyen expected_check_out                                                                                                                    |
| 5    | Check-out                                          | Booking `CHECKED_IN`                                                                                   | `COMPLETED`, set `check_out_at`, ban `AVAILABLE`, push thanh cong                                                                                                                     |
| 6    | Huy booking truoc gio                              | Booking `PENDING_CONFIRMATION/CONFIRMED`, `now < expected_arrive_time`                                 | `CANCELLED`, xu ly refund coc theo policy                                                                                                                                             |
| 7    | No-show                                            | `expected_arrive_time + 30p < now`, chua check-in                                                      | `EXPIRED`, ban `AVAILABLE`, xu ly mat coc                                                                                                                                             |
| 8    | Khet ban                                           | Job quet booking sap den 30p, ban dang `OCCUPIED`                                                      | Khong doi status ban, push canh bao do                                                                                                                                                |
| 9    | Nhan vien xep walk-in vao ban `BOOKED`             | Ban `BOOKED` va co booking sap den <=30p                                                               | Chan thao tac, tra loi business error                                                                                                                                                 |
| 10   | Check-in som >30p tren ban `AVAILABLE`             | `now < expected_arrive_time - 30p`                                                                     | Cho phep check-in, tinh lai expected_check_out = `now + max_duration`, check conflict booking ke tiep; neu conflict yeu cau `force=true`                                              |
| 11   | Check-in som khi ban dang `BOOKED`                 | booking own ban do, check-in som                                                                       | Truoc khi cho vao, bat buoc verify ban dang `BOOKED` la do chinh booking nay reserve. Neu khong phai, phai chan. Neu dung owner thi cho phep va **giu nguyen expected_check_out goc** |
| 12   | Den muon >30p nhung con ban                        | booking cu qua han                                                                                     | booking cu -> `EXPIRED`, tao booking moi dang walk-in (`CHECKED_IN`) va bat buoc re-check `WalkInGuardValidator` + `NoConflictValidator` cho booking moi                              |
| 13   | Gia han trong luc dang ngoi                        | booking `CHECKED_IN`                                                                                   | Neu booking ke tiep den <= new_check_out + 30p -> tu choi (safe mode). Optional soft mode: neu vuot <15p thi de xuat doi ban cho booking ke tiep                                      |
| 14   | Check-in nhung khong order                         | Sau 10p canh bao, sau 20p van khong order                                                              | Chot policy final: tu dong `CANCELLED`, ban `AVAILABLE`, hoan coc (neu co) de tranh tranh chap                                                                                        |
| 15   | Hai NV cung dat 1 ban cung khung gio               | race condition booking                                                                                 | lock theo table, giao dich sau fail voi message ro rang                                                                                                                               |
| 16   | Walk-in dang ngoi gan gio booking da confirm       | Ban `OCCUPIED` va con 30p den booking confirmed                                                        | Push canh bao do. Ngoai ra, ngay luc nhan vien xep walk-in vao ban `AVAILABLE` ma da co booking confirmed tuong lai gan, he thong phai canh bao truoc khi xep                         |
| 17   | Hai walk-in cung chon 1 ban trong                  | race condition walk-in                                                                                 | lock pessimistic/distributed lock, giao dich sau fail                                                                                                                                 |
| 18   | Walk-in gap ban co `PENDING_CONFIRMATION` chua coc | pending chua confirm                                                                                   | Khong auto doi ban thanh `BOOKED`, cho phep walk-in nhung canh bao NV goi xac nhan khach dat truoc                                                                                    |

---

## 6) Thiet ke service layer tren codebase hien tai

## 6.1 Service phan tach

- `TableBookingService` (orchestrator use-case)
- `BookingStateMachine` (transition)
- `BookingRuleValidatorChain` (rule)
- `BookingLockService` (redis lock)
- `DepositService`
- `BookingSchedulerService`
- `BookingNotificationService`

`TableBookingServiceImpl` hien tai la diem vao chinh, can tach logic theo module tren de don vi test ro rang.

## 6.2 Lock strategy chuan

Khoa theo table id, key:

- `lock:table:{tableId}`

Tat ca action mutate booking/table phai lock table:

- create booking
- confirm
- check-in
- check-out
- cancel
- extend
- create walk-in
- update booking doi ban (lock oldTable + newTable theo thu tu id tang dan)

Khong duoc lock theo booking id neu dang tranh race tren cung mot ban.

## 6.3 Transaction boundary

Moi use-case mutate dat trong `@Transactional` va lock bao ngoai service action:

1. acquire lock
2. load booking/table moi nhat
3. validate + transition
4. save booking/table
5. publish domain event trong transaction
6. commit transaction
7. gui WebSocket notify sau commit (`@TransactionalEventListener(phase = AFTER_COMMIT)`)
8. release lock

Luu y: khong push notify truoc commit de tranh FE nhan event thanh cong trong khi DB rollback.

---

## 7) Scheduler final

Tat ca scheduler can idempotent, co `ShedLock`, tranh gui notify lap vo han.

### 7.1 Job A - reserve truoc 30p

- Trigger: moi phut
- Query booking `CONFIRMED` trong cua so `(now, now+30p]`
- Neu ban `AVAILABLE` -> doi `BOOKED`
- Neu ban `OCCUPIED` -> khong doi, de Job C canh bao

### 7.2 Job B - no-show expire

- Trigger: moi phut
- Query `CONFIRMED` ma `expected_arrive_time + 30p < now` va `check_in_at is null`
- Transition -> `EXPIRED`
- Forfeit coc neu co
- Dong bo ban -> `AVAILABLE` neu co the

### 7.3 Job C - canh bao khet ban

- Trigger: moi phut
- Query `CONFIRMED` sap den 30p nhung ban dang `OCCUPIED`
- Push `table-alerts`

### 7.3b Guard truoc khi xep walk-in vao ban AVAILABLE (Rule 16 proactive)

- Trigger: tai service `createWalkIn`, khong phai scheduler
- Neu ban co booking `CONFIRMED` trong khoang `now -> now + 30p`: chan xep walk-in
- Neu ban co booking `CONFIRMED` trong khoang `(now + 30p) -> now + 2h`: cho phep nhung warning bat buoc cho NV

### 7.4 Job D - check-in khong order

- Trigger: moi phut
- Booking `CHECKED_IN` chua co order
- Neu qua 10p chua alert -> gui canh bao lan 1
- Neu qua 20p -> auto cancel/completed(0) theo policy

### 7.5 Job E - canh bao sap checkout

- Trigger: moi phut
- Booking `CHECKED_IN` con 15p den `expected_check_out`
- Push notify thanh toan

---

## 8) API final (giu tuong thich voi project hien tai)

Controller hien tai dang theo namespace `/api/v1/table-booking`.

### 8.1 API dang co (giu backward compatibility)

- `POST /api/v1/table-booking/search`
- `POST /api/v1/table-booking/create`
- `POST /api/v1/table-booking/update`
- `POST /api/v1/table-booking/update-status`

### 8.2 API de nghi bo sung de ro use-case

- `POST /api/v1/table-booking/{id}/confirm`
- `POST /api/v1/table-booking/{id}/check-in`
- `POST /api/v1/table-booking/{id}/check-out`
- `POST /api/v1/table-booking/{id}/cancel`
- `POST /api/v1/table-booking/{id}/deposit`
- `POST /api/v1/table-booking/{id}/extend`
- `POST /api/v1/table-booking/walk-in`
- `GET /api/v1/table-booking/tables/{tableId}/available-slots`

Neu chua mo endpoint moi, `update-status` van duoc dung nhung service ben trong phai route vao state machine theo tung action.

---

## 9) Repository/query bat buoc

Can bo sung cac query theo nghiep vu (de nghi dat ten ro nghia):

1. `findConfirmedBookingsComingInWindow(startTime, endTime)`
2. `findConfirmedBookingsOnTableBetween(tableId, startTime, endTime)` (check conflict theo interval)
3. `findNextConfirmedBookingOnTable(tableId, afterTime)` (phuc vu extend + check-in som)
4. `findConfirmedNoShowCandidates(now)`
5. `findCheckedInWithoutOrderOlderThan(minutes)`
6. `existsConflictBookingOnTable(tableId, startTime, endTime, statuses)`
7. `findPendingConfirmationsOnTableAfter(tableId, now)`

Chi so de nghi:

- `idx_tb_table_status_arrive (dining_table_id, booking_status, expected_arrive_time)`
- `idx_tb_active_status_arrive (is_active, booking_status, expected_arrive_time)`
- `idx_tb_table_arrive_status (dining_table_id, expected_arrive_time, booking_status)` (toi uu conflict check va next-booking lookup)

---

## 10) Realtime notification contract

Topic de nghi:

- `/topic/table-status`
- `/topic/booking-updates`
- `/topic/table-alerts`
- `/topic/deposit-events`

Payload JSON de FE de xu ly:

```json
{
  "event": "BOOKING_EXPIRED",
  "bookingId": 123,
  "tableId": 8,
  "tableCode": "T08",
  "at": "2026-04-08T10:00:00",
  "message": "Booking 123 da EXPIRED do no-show"
}
```

Khong gui string thuong cho event quan trong, uu tien JSON co schema on dinh.

---

## 11) Chinh sach dat coc (final)

- Dat coc la policy theo booking.
- `deposit_amount > 0` va yeu cau coc thi phai `deposit_paid = true` truoc confirm.
- No-show -> forfeit coc.
- Cancel truoc gio -> refund theo policy.
- Rule 14 (khong order sau 20p): auto `CANCELLED` va refund coc (neu co).
- Check-out -> coc duoc khau tru vao invoice.

De xuat bo sung thong tin:

- `deposit_paid` (boolean)
- `deposit_paid_at`
- `is_deposit_forfeited`
- `deposit_txn_ref`

---

## 12) Dieu chinh bat buoc so voi code hien tai

1. Them `CHECKED_IN` vao `BookingStatusEnum`.
2. Chuyen toan bo update status sang state machine, khong set truc tiep.
3. Khoa theo `tableId` cho moi action mutate.
4. Re-validate conflict khi confirm, check-in som, extend.
5. Tach scheduler thanh tung job idempotent co dedup notify.
6. Chuan hoa topic notification + payload JSON.
7. Chuan hoa endpoint action hoac map `update-status` theo action contract.
8. Bo sung ownership check cho Rule 11 + proactive walk-in warning/block cho Rule 16.
9. Chuyen notify sang co che after-commit event.

---

## 13) Test acceptance theo business matrix

Phai co test tu dong cover day du:

- Unit test state machine transition
- Unit test tung validator
- Integration test lock race (Rule 15, 17)
- Integration test scheduler (Rule 3, 7, 8, 14, 16)
- API test happy path Rule 1 -> 7
- API test edge Rule 8 -> 18

Checklist pass:

1. Khong booking nao check-in duoc khi tre >30p ma khong qua flow expire + walk-in moi.
2. Khong xay ra double-booking cung table/cung slot khi concurrent.
3. Ban chuyen AVAILABLE/BOOKED/OCCUPIED dung theo quy tac trung tam.
4. Event notify day du va khong spam trung lap.

---

## 14) Lo trinh implement de xuat

1. Refactor enum + state machine + lock strategy.
2. Implement validator chain + repository query bo sung.
3. Implement scheduler jobs idempotent.
4. Chuan hoa API action va response code.
5. Bo sung test matrix 1-18 va chot UAT voi FE/POS.

---

## 15) Ket luan final

Ban final v2 nay bao phu day du business flow, happy-case, edge-case theo matrix nghiep vu va kha thi tren codebase hien tai.

Nguyen tac van hanh cot loi:

- State transition thong qua state machine
- Rule check dung thoi diem transition
- Lock theo table de chan race condition
- Scheduler idempotent + notify realtime
- Dong bo table status theo 1 nguon su that duy nhat

Neu implement dung tai lieu nay, he thong dat ban se on dinh trong ca flow binh thuong lan tinh huong bien tai nha hang/POS.
