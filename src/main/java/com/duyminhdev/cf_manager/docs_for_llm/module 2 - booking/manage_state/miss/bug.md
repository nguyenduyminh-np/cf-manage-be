# Báo Cáo Kiểm Tra Mismatch: BE Code vs Core Business Docs

Sau khi rà soát mã nguồn so với tài liệu thiết kế nghiệp vụ (RSD/FE Guide), tôi xác nhận **toàn bộ 6 điểm mismatch mà bạn đã nêu là chính xác 100%**. Dưới đây là phân tích chi tiết kèm trích dẫn văn bản và mã nguồn:

---

## 1. [Critical] Trạng thái bàn chưa phản ánh đúng CHECKED_IN/WALK-IN

**Yêu cầu thiết kế (RSD):**
Theo mục A3 (Check-in), tại dòng 258 của `RSD_API_USAGE_GUIDE.md`:

> 3. Chuyển bàn từ `AVAILABLE`/`BOOKED` sang `OCCUPIED`.

**Code triển khai thực tế:**
Trong `ServiceSupport.java` (dòng 185-195), hàm phân giải trạng thái bàn ưu tiên Order thay vì Booking Status:

```java
    public TableStatusEnum resolveTableStatus(Integer tableId) {
        if (hasUnfinishedOrders(tableId)) {
            return TableStatusEnum.OCCUPIED; // Bắt buộc phải CÓ MÓN CHƯA XONG mới lên OCCUPIED
        }
        if (hasUpcomingActiveBooking(tableId)) {
            return TableStatusEnum.BOOKED;
        }
        return TableStatusEnum.AVAILABLE;
    }
```

**Lỗ hổng:** Khách walk-in ngồi vào bàn (`CHECKED_IN`) nhưng chưa gọi món thì hàm này sẽ bỏ qua điều kiện `hasUnfinishedOrders` và trả về `AVAILABLE`. Bàn không được đổi sang `OCCUPIED` như kỳ vọng.

---

## 2. [Critical] Walk-in chưa enforce đủ điều kiện phòng ngừa xung đột

**Yêu cầu thiết kế (RSD):**
Walk-in phải kiểm tra đụng độ các booking sắp tới.

**Code triển khai thực tế:**
Trong `WalkInGuardValidator.java` (dòng 37), hệ thống chỉ check có booking `CONFIRMED` _trong 30 phút_ (hardcode) phía trước, thay vì kiểm tra toàn bộ khung giờ khách sử dụng:

```java
        boolean hasNearConfirmed = tableBookingRepository.existsActiveBookingOnTableInWindowAndStatuses(
                tableId, now, now.plusMinutes(30), // Chỉ check 30p tương lai
                List.of(BookingStatusEnum.CONFIRMED.getCode()), context.resolveBookingId()
        );
```

Nguy hiểm hơn, bộ kiểm tra xung đột thời gian `NoConflictValidator.java` (dòng 24-28) lại cố tình né tránh (skip) nhánh `WALK_IN_BOOKING`:

```java
    @Override
    public boolean supports(BookingValidationUseCase useCase) {
        // Hoàn toàn KHÔNG SUPPORTS useCase == WALK_IN_BOOKING
        return useCase == BookingValidationUseCase.CREATE_BOOKING
                || useCase == BookingValidationUseCase.CONFIRM_BOOKING
                || useCase == BookingValidationUseCase.LATE_ARRIVAL_WALK_IN;
    }
```

---

## 3. [Critical] Rule 12 chưa expose API rõ ràng cho FE

**Yêu cầu thiết kế (RSD):**
Theo Bảng Ma Trận (dòng 1769 của `RSD_API_USAGE_GUIDE.md`):

> | 12 | Đến muộn >30p | Booking cũ EXPIRED → tạo walk-in mới | NV tạo walk-in | POST /walk-in |

**Code triển khai thực tế:**
Dưới hàm `createWalkInFromLateArrival` của Service có hỗ trợ logic trên, nhưng trong `TableBookingController.java` (dòng 167-174), endpoint `walk-in` không hề định nghĩa biến cho phép FE truyền ID booking cũ nộp phạt:

```java
    @PostMapping("/walk-in")
    public ApiResponse<TableBookingResponseDTO> walkIn(
            @Valid @RequestBody TableBookingCreateRequestDTO request,
            @RequestParam(value = "force", required = false, defaultValue = "false") boolean force
    ) {
        return new ApiResponse<>(200, "...", tableBookingService.createWalkIn(request, force));
    }
```

---

## 4. [High] Hành vi deposit khác RSD (Không auto-confirm)

**Yêu cầu thiết kế (RSD):**
Theo mục A6 (Ghi nhận đặt cọc), dòng 587 của `RSD_API_USAGE_GUIDE.md` chỉ rõ nghiệp vụ:

> 5. Kiểm tra auto-confirm:
>    Nếu booking đang PENDING_CONFIRMATION → Tự động chuyển sang CONFIRMED (qua State Machine)

**Code triển khai thực tế:**
Trong hàm `markDepositPaid` thuộc `BookingUseCaseServiceImpl.java` (dòng 388), service chỉ thiết lập giá trị cọc rồi đem đi lưu, hoàn toàn phớt lờ state transition:

```java
            booking.setDepositAmount(effectiveAmount);
            booking.setDepositPaid(true);
            booking.setDepositPaidAt(paidAt != null ? paidAt : LocalDateTime.now());
            // CHƯA GỌI bookingStateMachine.transition(...) ĐỂ ĐỔI SANG CONFIRMED
            TableBooking saved = tableBookingRepository.save(booking);
```

---

## 5. [High] Endpoint available-slots thiếu so với tài liệu FE

**Yêu cầu thiết kế (RSD):**
Tại dòng 954 của `RSD_API_USAGE_GUIDE.md` có ghi chú đặc tả cho frontend fetch:

> ## A10. Xem khung giờ trống (Available Slots)
>
> GET /api/v1/table-booking/tables/{tableId}/available-slots?date=2026-04-10

**Code triển khai thực tế:**
Quét toàn bộ Class `TableBookingController.java`, hoàn toàn **không tìm ra phương thức mapping `@GetMapping("/tables/{tableId}/available-slots")`**. Backend Dev chưa implement chức năng này.

---

## 6. [High] Contract walk-in gây khó cho FE

**Yêu cầu thiết kế (RSD):**
FE gọi API walk-in như một request tức thời (Đến ngay, Ngồi liền).

**Code triển khai thực tế:**
Vấn đề đền từ `TableBookingCreateRequestDTO.java` có sử dụng các `@NotNull` block request nghiêm ngặt:

```java
    @NotNull(message = "expectedArriveTime is required")
    private LocalDateTime expectedArriveTime; // Buộc FE gửi dù không cần
```

Tuy nhiên bên trong `BookingUseCaseServiceImpl.java` hàm `buildWalkInBooking` (dòng 448), giá trị FE gửi lên này bị backend cho **ra thùng rác**:

```java
        booking.setExpectedArriveTime(now); // Đè luôn biến now (LocalDateTime.now()) bỏ qua payload client.
```

**Lỗ hổng UX/DX:** FE buộc phải fake Data thời gian `expectedArriveTime` gửi vào Payload chỉ để pass qua validation vòng ngoài của Java.
