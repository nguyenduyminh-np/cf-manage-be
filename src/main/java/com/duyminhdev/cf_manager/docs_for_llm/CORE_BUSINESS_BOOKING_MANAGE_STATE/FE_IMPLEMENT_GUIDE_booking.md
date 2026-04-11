# FE IMPLEMENT GUIDE - BOOKING MODULE (match BE implementation)

## 1) Muc tieu tai lieu

Tai lieu nay huong dan FE team implement man hinh Booking theo dung:

- Business flow va rule trong thu muc CORE_BUSINESS_BOOKING_MANAGE_STATE.
- API/Realtime contract da duoc implement o BE (Module 1 -> 9 da hoan thanh).

Scope: Booking list, create/update, action endpoints, realtime update, error handling, UAT checklist.

---

## 2) Nguon su that (source of truth)

Business + design + plan:

- BUSINESS_ANALYS_matrix rule and business flow.md
- IMPLEMENT_DESIGN_core implement design.md
- IMPLEMENT_PLAN_module_breakdown.md

BE implementation dang chay:

- Controller: /api/v1/table-booking/\*
- Realtime endpoint: /ws (SockJS + STOMP)
- Topics:
  - /topic/booking-updates
  - /topic/table-status
  - /topic/table-alerts
  - /topic/deposit-events

---

## 3) Trang thai va y nghia FE can dung

### 3.1 Booking status

- PENDING_CONFIRMATION
- CONFIRMED
- CHECKED_IN
- COMPLETED
- CANCELLED
- EXPIRED

### 3.2 Table status

- AVAILABLE
- BOOKED
- OCCUPIED

### 3.3 Rule FE can nho

- FE chi hien thi action hop le theo bookingStatus, khong cho user bam action sai ngu canh.
- BE van la noi enforce cuoi cung, FE can doc loi business va hien thi message ro rang.

---

## 4) API base contract

### 4.1 Base URL

- Prefix: /api/v1/table-booking

### 4.2 Success response wrapper

Tat ca API thanh cong tra theo mau:

```json
{
  "status": 200,
  "message": "SOME_SUCCESS_CODE",
  "data": {}
}
```

### 4.3 Error response wrapper

Tat ca loi business/validation tra theo mau:

```json
{
  "timestamp": "2026-04-10T15:00:00+07:00",
  "status": 400,
  "error": "Bad Request",
  "code": "INVALID_DATA",
  "message": "depositAmount must be > 0",
  "details": null,
  "path": "/api/v1/table-booking/123/deposit"
}
```

Luu y code hay gap:

- INVALID_DATA
- BOOKING_STATE_TRANSITION_INVALID
- TABLE_LOCK_BUSY
- INTERNAL_SERVER_ERROR

---

## 5) API matrix cho FE implement

## 5.1 Legacy endpoints (van support)

### POST /search

- Nghiep vu: Tim booking theo bo loc + phan trang.
- Rule: request co page/limit hop le (neu truyen).
- Chuc nang: Do du lieu list booking cho table view.

Request:

```json
{
  "page": 1,
  "limit": 20,
  "sortField": "expectedArriveTime",
  "sortDir": "desc",
  "tableId": 8,
  "bookingStatus": "CONFIRMED",
  "customerName": "Nguyen",
  "phoneNumber": "0901234567",
  "check_in_at": null,
  "check_out_at": null,
  "active": true
}
```

Response data la PageResponse:

```json
{
  "data": [
    {
      "bookingId": 123,
      "tableId": 8,
      "tableCode": "T08",
      "tableName": "Ban 08",
      "expectedArriveTime": "2026-04-10T18:00:00",
      "checkInAt": null,
      "expectedCheckOut": "2026-04-10T20:00:00",
      "checkOutAt": null,
      "bookingStatus": "CONFIRMED",
      "bookingStatusName": "Da xac nhan",
      "customerName": "A",
      "phoneNumber": "0901234567",
      "depositAmount": 200000,
      "depositPaid": true,
      "depositPaidAt": "2026-04-10T10:30:00",
      "depositForfeited": false,
      "depositTxnRef": "TXN001",
      "note": "",
      "accountId": 1,
      "accountUsername": "staff1",
      "accountFullName": "Staff 1",
      "active": true,
      "createdAt": "2026-04-10T09:00:00"
    }
  ],
  "pageNo": 1,
  "pageSize": 20,
  "totalElements": 10,
  "totalPages": 1
}
```

### POST /create

- Nghiep vu: Tao booking moi.
- Rule: tableId > 0, expectedArriveTime >= now, expectedCheckOut bat buoc.
- Chuc nang: Tao don dat ban va tra booking moi.

### POST /update

- Nghiep vu: Cap nhat booking hien co.
- Rule: bookingId > 0, tableId > 0, thoi gian hop le.
- Chuc nang: Chinh sua booking va tra ban ghi sau cap nhat.

### POST /update-status

- Nghiep vu: Endpoint tuong thich he cu de doi trang thai.
- Rule: bookingId bat buoc, bookingStatus dung format code.
- Chuc nang: FE cu van co the goi, nhung FE moi uu tien action endpoints ben duoi.

---

## 5.2 Action endpoints (FE moi nen dung)

### POST /{id}/confirm

- Nghiep vu: Xac nhan booking.
- Rule: id > 0, booking dang cho xac nhan.
- Chuc nang: Chuyen sang CONFIRMED.

Body: none

### POST /{id}/check-in

- Nghiep vu: Nhan ban cho khach.
- Rule: id > 0, cho phep force khi edge case can override.
- Chuc nang: Chuyen booking sang CHECKED_IN.

Body (optional):

```json
{
  "checkInAt": "2026-04-10T17:50:00",
  "force": false
}
```

### POST /{id}/check-out

- Nghiep vu: Ket thuc su dung ban.
- Rule: id > 0 va booking dang CHECKED_IN.
- Chuc nang: Chuyen booking sang COMPLETED, giai phong ban.

Body (optional):

```json
{
  "checkOutAt": "2026-04-10T20:05:00"
}
```

### POST /{id}/cancel

- Nghiep vu: Huy booking.
- Rule: id > 0 va khong o terminal state.
- Chuc nang: Chuyen booking sang CANCELLED.

Body: none

### POST /{id}/deposit

- Nghiep vu: Ghi nhan coc.
- Rule: id > 0, depositAmount > 0.
- Chuc nang: Cap nhat thong tin coc va push deposit event.

Body:

```json
{
  "depositAmount": 300000,
  "depositPaidAt": "2026-04-10T14:30:00",
  "depositTxnRef": "MOMO-20260410-001"
}
```

### POST /{id}/extend

- Nghiep vu: Gia han expectedCheckOut.
- Rule: expectedCheckOut bat buoc, co the force neu can.
- Chuc nang: Cap nhat gio ket thuc du kien.

Body:

```json
{
  "expectedCheckOut": "2026-04-10T21:00:00",
  "force": false
}
```

### POST /walk-in?force=false

- Nghiep vu: Tao booking walk-in.
- Rule: dung payload create booking, co query force.
- Chuc nang: Tao booking cho khach den truc tiep.

Body: giong /create

---

## 6) Mapping button/action tren UI

De xuat action map theo bookingStatus:

- PENDING_CONFIRMATION: Confirm, Update, Cancel, Deposit.
- CONFIRMED: Check-in, Update, Cancel, Deposit.
- CHECKED_IN: Check-out, Extend.
- COMPLETED: Chi cho xem chi tiet.
- CANCELLED: Chi cho xem chi tiet.
- EXPIRED: Chi cho xem chi tiet, co the tao walk-in moi neu can.

FE nen disable button khong hop le ngay tren UI de giam click sai.

---

## 7) Realtime integration (bat buoc)

## 7.1 Ket noi

- STOMP endpoint: /ws
- Broker topics: /topic/\*
- Co SockJS support.

Client de xuat:

- @stomp/stompjs
- sockjs-client

Pseudo init:

```ts
const socket = new SockJS("http://<host>/ws");
const client = Stomp.over(socket);
client.connect({}, () => {
  client.subscribe("/topic/booking-updates", onBookingEvent);
  client.subscribe("/topic/table-status", onTableStatusEvent);
  client.subscribe("/topic/table-alerts", onTableAlertEvent);
  client.subscribe("/topic/deposit-events", onDepositEvent);
});
```

## 7.2 Envelope realtime (BE da chuan hoa)

Moi message realtime co it nhat:

- event
- at
- topic
- dedupKey
- eventId

Ngoai ra se co payload fields theo nghiep vu: bookingId, tableId, tableCode, bookingStatus, tableStatus, expectedArriveTime, expectedCheckOut, message, source...

Vi du:

```json
{
  "event": "BOOKING_CHECKED_IN",
  "mutationType": "CHECK_IN",
  "bookingId": 101,
  "tableId": 7,
  "tableCode": "T07",
  "tableStatus": "OCCUPIED",
  "bookingStatus": "CHECKED_IN",
  "at": "2026-04-10T14:53:17.2409234",
  "message": "Booking mutation committed: BOOKING_CHECKED_IN",
  "source": "BOOKING_MUTATION_AFTER_COMMIT",
  "topic": "/topic/booking-updates",
  "dedupKey": "booking:mutation:booking:CHECK_IN:101:7:2026-04-10T14:53:17.2409234",
  "eventId": "9f3b8f95-c57e-4f53-8a11-6f8ccf2d9d16"
}
```

## 7.3 Event name FE can gap

Tu booking mutation (after commit):

- BOOKING_CREATED
- BOOKING_UPDATED
- BOOKING_STATUS_UPDATED
- BOOKING_CONFIRMED
- BOOKING_CHECKED_IN
- BOOKING_CHECKED_OUT
- BOOKING_CANCELLED
- BOOKING_EXPIRED
- BOOKING_EXTENDED
- BOOKING_WALK_IN_CREATED
- BOOKING_LATE_ARRIVAL_WALK_IN_CREATED
- BOOKING_AUTO_CANCELLED_NO_ORDER
- BOOKING_DEPOSIT_UPDATED
- TABLE_STATUS_SYNC (topic table-status)
- BOOKING_DEPOSIT_PAID (topic deposit-events)

Tu scheduler:

- TABLE_RESERVED
- BOOKING_EXPIRED_NO_SHOW
- TABLE_OCCUPIED_CONFLICT
- NO_ORDER_WARNING
- NO_ORDER_AUTO_CANCELLED
- CHECKOUT_REMINDER

## 7.4 De-dup phia FE

- Dung dedupKey lam key idempotent trong cache memory (TTL ngan, vd 2-5 phut).
- Neu dedupKey da xu ly thi bo qua event trung.
- eventId la unique id moi lan push, phu hop cho trace/log.

---

## 8) Rule mapping nhanh theo nghiep vu

- Rule 1-2: Create -> Confirm flow (pending -> confirmed).
- Rule 3: 30p truoc gio den, table co the chuyen BOOKED + event TABLE_RESERVED.
- Rule 4-5: Check-in -> Check-out (CHECKED_IN -> COMPLETED).
- Rule 6-7: Cancel truoc gio hoac no-show expire.
- Rule 8-9: Bao dong/chan thao tac khi ban bi xung dot booking.
- Rule 10-13: Check-in som, den tre, gia han theo logic conflict.
- Rule 14: Khong order 10p warning, 20p auto cancel.
- Rule 15-17: Co lock tranh race condition.
- Rule 18: Pending confirmation khong auto giu ban BOOKED.

FE khong tu enforce business cung, nhung can huong dan thao tac dung theo flow tren de user experience thong suot.

---

## 9) Validation va UX recommendation cho FE

- Validate client-side truoc call API:
  - id > 0
  - expectedCheckOut bat buoc cho extend
  - depositAmount > 0 cho deposit
- Hien thi server error.message truc tiep cho loi business.
- Neu code = TABLE_LOCK_BUSY: hien thi toast "Ban dang duoc thao tac boi nguoi khac, vui long thu lai".
- Neu code = BOOKING_STATE_TRANSITION_INVALID: refresh row booking roi cho user thao tac lai.
- Sau moi action thanh cong:
  - update optimistic row local
  - sau do van uu tien du lieu realtime de dong bo chinh xac

---

## 10) FE UAT checklist truoc release

1. Search + paging + sort hoat dong dung.
2. Action button an/hien dung theo status.
3. Confirm/check-in/check-out/cancel/deposit/extend/walk-in goi dung endpoint.
4. Error business hien thi dung message/code.
5. Realtime 4 topic subscribe thanh cong va update UI real-time.
6. Khong xu ly trung event khi dedupKey trung.
7. Case no-show/no-order/checkout-reminder nhan duoc canh bao dung.
8. List booking va table board dong bo nhau sau event.

---

## 11) Ghi chu moi truong

- Security hien tai dang permit all cho API de FE test nhanh.
- CORS hien tai allow origin: http://localhost:4200.
- Neu FE chay host khac, can align lai CORS tu BE config.

---

## 12) Ket luan

FE nen uu tien action endpoints + realtime topics de dat dung flow nghiep vu booking hien tai.
Tai lieu nay da match voi implementation BE da hoan thanh va rule matrix trong thu muc CORE_BUSINESS_BOOKING_MANAGE_STATE.
