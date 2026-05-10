# CF-MANAGER — TÀI LIỆU THAM CHIẾU TOÀN DIỆN CORE API & NGHIỆP VỤ

> **Mục đích:** Cung cấp đầy đủ context cho LLM hiểu kiến trúc, luồng nghiệp vụ, API contract, state machine, và quy tắc business để có thể build giao diện frontend với workflow đúng chuẩn.

> **Scope:** Hệ thống quản lý quán cà phê (Coffee CRM) — Module **TableList** (Quản lý bàn, đặt bàn, gọi món, thanh toán).

---

## MỤC LỤC

1. [Tổng Quan Kiến Trúc](#1-tổng-quan-kiến-trúc)
2. [Data Model & Quan Hệ Entity](#2-data-model--quan-hệ-entity)
3. [State Machine — Máy Trạng Thái Nghiệp Vụ](#3-state-machine--máy-trạng-thái-nghiệp-vụ)
4. [API Contract Registry — Bảng Đăng Ký 20 API](#4-api-contract-registry--bảng-đăng-ký-20-api)
5. [Module 1: Authentication](#5-module-1-authentication)
6. [Module 2: Table — Quản Lý Bàn](#6-module-2-table--quản-lý-bàn)
7. [Module 3: Table Booking — Đặt Bàn](#7-module-3-table-booking--đặt-bàn)
8. [Module 4: Dish & Dish Category — Menu Món](#8-module-4-dish--dish-category--menu-món)
9. [Module 5: Dish Order — Gọi Món](#9-module-5-dish-order--gọi-món)
10. [Module 6: Dish Order Detail — Chi Tiết Gọi Món](#10-module-6-dish-order-detail--chi-tiết-gọi-món)
11. [Module 7: Payment Method — Phương Thức Thanh Toán](#11-module-7-payment-method--phương-thức-thanh-toán)
12. [Module 8: Invoice — Hoá Đơn & Thanh Toán](#12-module-8-invoice--hoá-đơn--thanh-toán)
13. [Luồng Nghiệp Vụ End-to-End (4 Flow Chính)](#13-luồng-nghiệp-vụ-end-to-end)
14. [Response Envelope & Error Handling](#14-response-envelope--error-handling)
15. [Business Rules Tổng Hợp](#15-business-rules-tổng-hợp)

---

## 1. TỔNG QUAN KIẾN TRÚC

### 1.1. Tech Stack Backend
- **Framework:** Spring Boot 3.x + Java 17+
- **ORM:** Spring Data JPA / Hibernate
- **Security:** JWT (Access Token + Refresh Token)
- **Convention:** Tất cả API dùng **POST method** + `@RequestBody` JSON

### 1.2. Layer Architecture
```
Controller (POST-only, @Valid @RequestBody)
    ↓
Service Interface → ServiceImpl (business logic)
    ↓
ServiceSupport (shared helper: validate, resolve entity, recompute table status)
    ↓
Repository (JPA) / Specification (dynamic filter) / NativeSQL (aggregate query)
    ↓
Entity ↔ Database
```

### 1.3. Response Wrapper Convention 

**Thành công:**
```json
{
  "status": 200,
  "message": "ACTION_CODE_SUCCESS",
  "data": { ... }
}
```

**Phân trang (PageResponse):**
```json
{
  "status": 200,
  "message": "SEARCH_SUCCESS",
  "data": {
    "data": [ ... ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

**Lỗi (ErrorResponse):**
```json
{
  "timestamp": "2026-03-31T10:15:30+07:00",
  "status": 400,
  "error": "Bad Request",
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": { "fieldA": "must not be blank" },
  "path": "/api/v1/table/search"
}
```

### 1.4. HTTP Status Mapping
| Exception | HTTP Status | Error Code |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `VALIDATION_ERROR` |
| `InvalidDataException` | 400 | `INVALID_DATA` |
| `DuplicatedUsernameException` | 409 | `DUPLICATED_USERNAME` |
| `AuthenticationException` | 401 | `AUTH_UNAUTHORIZED` |
| `AccessDeniedException` | 403 | `ACCESS_DENIED` |
| `Exception` (fallback) | 500 | `INTERNAL_SERVER_ERROR` |

---

## 2. DATA MODEL & QUAN HỆ ENTITY

### 2.1. Entity Relationship
```
Table (dining_table)
  ├── 1:N → TableBooking (đặt bàn)
  ├── 1:N → DishOrder (đơn gọi món)
  └── 1:N → Invoice (hoá đơn)

Account
  ├── 1:N → TableBooking (nhân viên tạo booking)
  ├── 1:N → DishOrder (nhân viên đặt món)
  └── 1:N → Invoice (nhân viên thu ngân)

DishOrder
  ├── N:1 → DishOrderStatus (trạng thái đơn, bảng lookup)
  └── 1:N → DishOrderDetail (chi tiết dòng món)

DishOrderDetail
  └── N:1 → Dish (món ăn)

Dish
  └── N:1 → DishCategory (danh mục)

Invoice
  └── 1:N → InvoiceDetail (chi tiết hoá đơn)

InvoiceDetail
  └── N:1 → Dish
```

### 2.2. Bảng Tham Gia Chính

| Entity | DB Table | Vai Trò |
|---|---|---|
| `TableEntity` | `dining_table` | Bàn (tên, mã, tầng, slot, trạng thái) |
| `TableBooking` | `table_booking` | Lịch đặt bàn (khách, giờ, cọc, trạng thái) |
| `DishOrder` | `dish_order` | Đơn gọi món gán theo bàn + nhân viên |
| `DishOrderDetail` | `dish_order_detail` | Chi tiết dòng món (dish, số lượng, giá snapshot) |
| `DishOrderStatus` | `dish_order_status` | Bảng lookup trạng thái đơn |
| `Dish` | `dish` | Món ăn/đồ uống (tên, mã, giá, ảnh) |
| `DishCategory` | `dish_category` | Phân loại món |
| `Invoice` | `invoice` | Hoá đơn thanh toán |
| `InvoiceDetail` | `invoice_detail` | Chi tiết dòng hoá đơn |
| `Account` | `account` | Tài khoản nhân viên |

### 2.3. Soft Delete Convention
- Tất cả entity đều có field `active` (Boolean). Query mặc định chỉ lấy `active = true`.
- **Ngoại lệ:** `DishOrderDetail` dùng **hard delete** khi update đơn (replace toàn bộ detail lines).

---

## 3. STATE MACHINE — MÁY TRẠNG THÁI NGHIỆP VỤ

### 3.1. Table Status (`TableStatusEnum`)

| Code | Label | Ý Nghĩa |
|---|---|---|
| `AVAILABLE` | Bàn trống | Không có order chưa hoàn tất, không có booking sắp tới |
| `OCCUPIED` | Đang sử dụng | Có ít nhất 1 DishOrder chưa hoàn tất (≠ DONE, ≠ CANCELLED) |
| `BOOKED` | Đã đặt | Không có order chưa hoàn tất nhưng có booking upcoming trong 2 giờ |

**CRITICAL — Quy tắc tự động tính trạng thái bàn (resolveTableStatus):**
```
1. Nếu có order chưa hoàn tất (status ≠ DONE và ≠ CANCELLED) → OCCUPIED
2. Else nếu có booking sắp tới trong 2 giờ (status = PENDING_CONFIRMATION hoặc CONFIRMED) → BOOKED  
3. Else → AVAILABLE
```
> **Backend tự động recompute** trạng thái bàn sau MỌI thay đổi booking, order, hoặc thanh toán. Frontend KHÔNG cần tính trạng thái — chỉ cần reload danh sách bàn.

### 3.2. Booking Status (`BookingStatusEnum`)

| Code | Label | Transition Hợp Lệ |
|---|---|---|
| `PENDING_CONFIRMATION` | Chờ xác nhận | → CONFIRMED, CANCELLED, EXPIRED |
| `CONFIRMED` | Đã xác nhận | → COMPLETED, CANCELLED |
| `CANCELLED` | Đã huỷ | (Terminal) |
| `COMPLETED` | Hoàn thành | (Terminal) |
| `EXPIRED` | Đã hết hạn | (Terminal) |

### 3.3. Dish Order Status (`DishOrderStatusCodeEnum`)

| Code | Label | Ý Nghĩa |
|---|---|---|
| `PROCESSING` | Đang xử lý | Order đang được chuẩn bị / chờ phục vụ |
| `DONE` | Hoàn thành | Order đã xử lý xong (auto-set khi thanh toán) |
| `CANCELLED` | Đã huỷ | Order bị huỷ bỏ |

> **Business Rule:** Khi thanh toán thành công (Invoice `PAID`), backend tự động mark TẤT CẢ order chưa hoàn tất of bàn đó thành `DONE`.

### 3.4. Payment Status (`PaymentStatusEnum`)

| Code | Label |
|---|---|
| `PENDING` | Chờ thanh toán |
| `PAID` | Đã thanh toán |
| `CANCEL` | Đã huỷ |

### 3.5. Payment Method (`PaymentMethodEnum`)

| Code | Label | Display Order |
|---|---|---|
| `CASH` | Tiền mặt | 1 |
| `BANK_TRANSFER` | Chuyển khoản | 2 |

---

## 4. API CONTRACT REGISTRY — BẢNG ĐĂNG KÝ 20 API

### Authentication (4 API)
| # | Endpoint | Body Required | Mô Tả |
|---|---|---|---|
| 1 | `POST /api/v1/auth/register` | Yes | Đăng ký tài khoản |
| 2 | `POST /api/v1/auth/login` | Yes | Đăng nhập, nhận JWT |
| 3 | `POST /api/v1/auth/refresh` | Yes | Refresh access token |
| 4 | `POST /api/v1/auth/logout` | Yes | Đăng xuất, huỷ refresh token |

### Core Business (16 API)
| # | Endpoint | Body Required | @Valid | Mô Tả |
|---|---|---|---|---|
| 5 | `POST /api/v1/table/search` | Yes | Yes | Tìm kiếm danh sách bàn (phân trang, aggregate) |
| 6 | `POST /api/v1/table/detail` | Yes | Yes | Chi tiết 1 bàn |
| 7 | `POST /api/v1/table/update-status` | Yes | Yes | Cập nhật trạng thái bàn thủ công |
| 8 | `POST /api/v1/table-booking/search` | Yes | Yes | Lịch sử đặt bàn (phân trang, filter) |
| 9 | `POST /api/v1/table-booking/create` | Yes | Yes | Tạo booking mới |
| 10 | `POST /api/v1/table-booking/update` | Yes | Yes | Cập nhật booking |
| 11 | `POST /api/v1/table-booking/update-status` | Yes | Yes | Đổi trạng thái booking |
| 12 | `POST /api/v1/dish/list` | Optional | No | Lấy toàn bộ menu |
| 13 | `POST /api/v1/dish/search` | Yes | Yes | Tìm kiếm món (phân trang, Select2) |
| 14 | `POST /api/v1/dish-category/list` | Optional | No | Lấy danh sách danh mục món |
| 15 | `POST /api/v1/dish-order/list-by-table` | Yes | Yes | Lịch sử order theo bàn |
| 16 | `POST /api/v1/dish-order/create` | Yes | Yes | Tạo đơn gọi món |
| 17 | `POST /api/v1/dish-order/update` | Yes | Yes | Cập nhật đơn gọi món |
| 18 | `POST /api/v1/dish-order/update-status` | Yes | Yes | Đổi trạng thái đơn gọi món |
| 19 | `POST /api/v1/dish-order-detail/list-by-order` | Yes | Yes | Chi tiết các món trong 1 order |
| 20 | `POST /api/v1/dish-order-detail/list-by-table` | Yes | Yes | Gom nhóm món theo bàn cho checkout |
| 21 | `POST /api/v1/payment-method/list` | Optional | No | Lấy danh sách phương thức thanh toán |
| 22 | `POST /api/v1/invoice/count` | Optional | No | Đếm hoá đơn, sinh mã tiếp theo |
| 23 | `POST /api/v1/invoice/create` | Yes | Yes | Tạo hoá đơn thanh toán |
| 24 | `POST /api/v1/invoice/confirm-payment` | Yes | Yes | Xác nhận đã thanh toán |
| 25 | `POST /api/v1/invoice/detail` | Yes | Yes | Chi tiết hoá đơn (render biên lai) |

> **Lưu ý:** Các API có `Body Optional` chấp nhận body `null` hoặc `{}`. Controller tự tạo DTO mặc định.

---

## 5. MODULE 1: AUTHENTICATION

### `POST /api/v1/auth/login`
**Request:**
```json
{ "username": "admin", "password": "123456" }
```
**Response:**
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "dGhpcyBpcyBh...",
  "username": "admin",
  "fullName": "Admin",
  "roles": ["ADMIN"]
}
```

### `POST /api/v1/auth/refresh`
**Request:** `{ "refreshToken": "dGhpcyBpcyBh..." }`

### `POST /api/v1/auth/logout`
**Request:** `{ "refreshToken": "dGhpcyBpcyBh..." }`

> **FE cần:** Lưu `accessToken` vào header `Authorization: Bearer <token>`. Dùng refresh khi token hết hạn.

---

## 6. MODULE 2: TABLE — QUẢN LÝ BÀN

### 6.1. `POST /api/v1/table/search`

**Request (extends PageFilterRequest):**
```json
{
  "keyword": "Bàn VIP",
  "floor": 1,
  "slot": 4,
  "tableStatus": "AVAILABLE",
  "active": true,
  "page": 0,
  "limit": 20,
  "sortField": "tableName",
  "sortDir": "asc"
}
```
> Tất cả field đều optional. Gửi `{}` hoặc `null` để lấy toàn bộ.

**Response — `ApiResponse<PageResponse<List<TableSearchResponseDTO>>>`:**
```json
{
  "status": 200,
  "message": "SEARCH_TABLE_SUCCESS",
  "data": {
    "data": [
      {
        "tableId": 1,
        "tableCode": "TB-101",
        "tableName": "Bàn 1 - Tầng 1",
        "tableStatus": "AVAILABLE",
        "tableStatusName": "Bàn trống",
        "floor": 1,
        "slot": 4,
        "totalBooking": 3,
        "lastBookingTime": "2026-03-31T14:00:00",
        "active": true
      }
    ],
    "pageNo": 0,
    "pageSize": 20,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

**Business Logic:**
- Sử dụng **Native SQL aggregate** để đếm `totalBooking` và lấy `lastBookingTime` trực tiếp trong query.
- Hỗ trợ lọc theo `keyword` (match tableName/tableCode), `floor`, `slot`, `tableStatus`.

### 6.2. `POST /api/v1/table/detail`

**Request:**
```json
{ "tableId": 1 }
```

**Response — `ApiResponse<TableDetailResponseDTO>`:**
```json
{
  "status": 200,
  "message": "GET_TABLE_DETAIL_SUCCESS",
  "data": {
    "tableId": 1,
    "tableCode": "TB-101",
    "tableName": "Bàn 1 - Tầng 1",
    "tableStatus": "OCCUPIED",
    "tableStatusName": "Đang sử dụng",
    "floor": 1,
    "slot": 4,
    "active": true,
    "createdTime": "2026-01-01T00:00:00"
  }
}
```

### 6.3. `POST /api/v1/table/update-status`

**Request:**
```json
{
  "tableId": 1,
  "tableStatus": "AVAILABLE"
}
```
**Response:** `ApiResponse<Boolean>` — `data: true`

> **Lưu ý:** API này là cập nhật **thủ công**. Trong flow tự động, backend tự recompute qua `ServiceSupport.recomputeAndSyncTableStatus()`.

---

## 7. MODULE 3: TABLE BOOKING — ĐẶT BÀN

### 7.1. `POST /api/v1/table-booking/search`

**Request (extends PageFilterRequest):**
```json
{
  "tableId": 1,
  "bookingStatus": "CONFIRMED",
  "fromDate": "2026-03-01T00:00:00",
  "toDate": "2026-03-31T23:59:59",
  "keyword": "Nguyen",
  "page": 0,
  "limit": 10,
  "sortField": "bookingTime",
  "sortDir": "desc"
}
```

**Response — `ApiResponse<PageResponse<List<TableBookingResponseDTO>>>`:**
```json
{
  "data": [
    {
      "bookingId": 5,
      "tableId": 1,
      "tableCode": "TB-101",
      "tableName": "Bàn 1",
      "bookingTime": "2026-03-31T18:00:00",
      "checkInTime": null,
      "bookingStatus": "CONFIRMED",
      "bookingStatusName": "Đã xác nhận",
      "customerName": "Nguyen Van A",
      "phoneNumber": "0901234567",
      "deposit": 100000,
      "note": "Bàn cạnh cửa sổ",
      "accountId": 1,
      "accountUsername": "admin",
      "accountFullName": "Admin",
      "active": true,
      "createdTime": "2026-03-31T10:00:00"
    }
  ]
}
```

### 7.2. `POST /api/v1/table-booking/create`

**Request:**
```json
{
  "tableId": 1,
  "bookingTime": "2026-03-31T18:00:00",
  "customerName": "Nguyen Van A",
  "phoneNumber": "0901234567",
  "deposit": 100000,
  "bookingStatus": "PENDING_CONFIRMATION",
  "note": "2 người"
}
```
> `bookingStatus` optional — mặc định `PENDING_CONFIRMATION`. `bookingTime` **phải là tương lai**.

**Response:** `ApiResponse<TableBookingResponseDTO>`

**Side Effects:**
1. Tạo booking mới, gán `account` = user đang đăng nhập (từ JWT)
2. **Recompute trạng thái bàn** → có thể chuyển bàn sang `BOOKED`

### 7.3. `POST /api/v1/table-booking/update`

**Request:**
```json
{
  "bookingId": 5,
  "tableId": 2,
  "bookingTime": "2026-03-31T19:00:00",
  "customerName": "Nguyen Van B",
  "phoneNumber": "0909876543",
  "deposit": 200000,
  "bookingStatus": "CONFIRMED",
  "note": "Đổi sang bàn 2"
}
```

**Side Effects:**
1. Nếu **đổi bàn** (tableId khác) → recompute trạng thái cả bàn cũ + bàn mới
2. Nếu không đổi bàn → recompute bàn hiện tại

### 7.4. `POST /api/v1/table-booking/update-status`

**Request:**
```json
{
  "bookingId": 5,
  "bookingStatus": "CANCELLED"
}
```
**Response:** `ApiResponse<Boolean>` → `data: true`

**Side Effects:** Recompute trạng thái bàn (VD: huỷ booking cuối cùng → bàn chuyển `AVAILABLE`)

---

## 8. MODULE 4: DISH & DISH CATEGORY — MENU MÓN

### 8.1. `POST /api/v1/dish/list`

**Request (optional body):**
```json
{ "active": true }
```
> Gửi `{}` hoặc `null` → mặc định lấy active = true.

**Response — `ApiResponse<List<DishResponseDTO>>`:**
```json
{
  "data": [
    {
      "dishId": 1,
      "dishCode": "CF-001",
      "dishName": "Cà phê sữa đá",
      "price": 25000,
      "photo": "/images/caphe.jpg",
      "dishCategoryId": 1,
      "dishCategoryCode": "DRINK",
      "dishCategoryName": "Đồ uống",
      "active": true,
      "createdTime": "2026-01-01T00:00:00"
    }
  ]
}
```

### 8.2. `POST /api/v1/dish/search`

**Request (extends PageFilterRequest):**
```json
{
  "keyword": "cà phê",
  "dishCategoryId": 1,
  "active": true,
  "page": 0,
  "limit": 20
}
```

**Response:** `ApiResponse<PageResponse<List<DishResponseDTO>>>`

### 8.3. `POST /api/v1/dish-category/list`

**Request (optional):** `{ "active": true }` hoặc `null`

**Response — `ApiResponse<List<DishCategoryResponseDTO>>`:**
```json
{
  "data": [
    {
      "dishCategoryId": 1,
      "dishCategoryCode": "DRINK",
      "dishCategoryName": "Đồ uống",
      "active": true,
      "createdTime": "2026-01-01T00:00:00"
    }
  ]
}
```

---

## 9. MODULE 5: DISH ORDER — GỌI MÓN

### 9.1. `POST /api/v1/dish-order/list-by-table`

**Request:**
```json
{ "tableId": 1 }
```

**Response — `ApiResponse<List<DishOrderResponseDTO>>`:**
```json
{
  "data": [
    {
      "dishOrderId": 10,
      "tableId": 1,
      "tableCode": "TB-101",
      "tableName": "Bàn 1",
      "accountId": 1,
      "accountUsername": "admin",
      "accountFullName": "Admin",
      "dishOrderStatusId": 1,
      "dishOrderStatus": "PROCESSING",
      "dishOrderStatusName": "Đang xử lý",
      "note": null,
      "active": true,
      "createdTime": "2026-03-31T14:30:00"
    }
  ]
}
```

### 9.2. `POST /api/v1/dish-order/create` ⭐ CRITICAL

**Request:**
```json
{
  "tableId": 1,
  "dishOrderStatus": "PROCESSING",
  "dishOrderDetails": [
    { "dishId": 1, "quantity": 2, "note": "Ít đường" },
    { "dishId": 3, "quantity": 1, "note": "" }
  ]
}
```
> `dishOrderStatus` optional — mặc định `PROCESSING`.

**Response:** `ApiResponse<DishOrderResponseDTO>`

**Business Logic (step-by-step):**
1. Resolve default status → `PROCESSING`
2. Resolve `table` (active), `account` (từ JWT), `DishOrderStatus` entity
3. Tạo DishOrder header
4. **Replace detail lines:** hard delete detail cũ → tạo mới từ payload
5. Mỗi detail line: resolve Dish → **snapshot `price` từ `Dish.price`** tại thời điểm gọi
6. **Recompute trạng thái bàn** → bàn chuyển `OCCUPIED`

### 9.3. `POST /api/v1/dish-order/update`

**Request:**
```json
{
  "dishOrderId": 10,
  "tableId": 1,
  "dishOrderStatus": "PROCESSING",
  "dishOrderDetails": [
    { "dishId": 1, "quantity": 3, "note": "Nhiều đá" },
    { "dishId": 5, "quantity": 1, "note": "" }
  ]
}
```

**Side Effects:** Tương tự create. Nếu đổi bàn → recompute cả bàn cũ + mới.

### 9.4. `POST /api/v1/dish-order/update-status`

**Request:**
```json
{
  "dishOrderId": 10,
  "dishOrderStatus": "CANCELLED"
}
```

**Side Effects:** Recompute trạng thái bàn (VD: huỷ order cuối → bàn về `AVAILABLE` hoặc `BOOKED`)

---

## 10. MODULE 6: DISH ORDER DETAIL — CHI TIẾT GỌI MÓN

### 10.1. `POST /api/v1/dish-order-detail/list-by-order`

**Request:** `{ "dishOrderId": 10 }`

**Response — `ApiResponse<List<DishOrderDetailResponseDTO>>`:**
```json
{
  "data": [
    {
      "dishOrderDetailId": 20,
      "dishOrderId": 10,
      "dishId": 1,
      "dishCode": "CF-001",
      "dishName": "Cà phê sữa đá",
      "dishPhoto": "/images/caphe.jpg",
      "quantity": 2,
      "price": 25000,
      "totalPrice": 50000,
      "note": "Ít đường",
      "active": true,
      "createdTime": "2026-03-31T14:30:00"
    }
  ]
}
```

### 10.2. `POST /api/v1/dish-order-detail/list-by-table` ⭐ CRITICAL (Checkout Aggregation)

**Request:** `{ "tableId": 1 }`

**Response — `ApiResponse<List<DishGroupedByTableResponseDTO>>`:**
```json
{
  "data": [
    {
      "dishId": 1,
      "dishCode": "CF-001",
      "dishName": "Cà phê sữa đá",
      "dishPhoto": "/images/caphe.jpg",
      "totalQuantity": 5,
      "unitPrice": 25000,
      "totalPrice": 125000
    },
    {
      "dishId": 3,
      "dishCode": "CF-003",
      "dishName": "Trà đào",
      "dishPhoto": "/images/tradao.jpg",
      "totalQuantity": 2,
      "unitPrice": 30000,
      "totalPrice": 60000
    }
  ]
}
```

**Business Logic (RẤT QUAN TRỌNG):**
- **Native SQL GROUP BY** gom tất cả `DishOrderDetail` từ tất cả order **chưa hoàn tất** (loại `CANCELLED` và `DONE`) của bàn
- Group theo `dishId` + `price`, SUM `quantity`, SUM `quantity * price`
- Kết quả này chính là danh sách items cho modal thanh toán

---

## 11. MODULE 7: PAYMENT METHOD — PHƯƠNG THỨC THANH TOÁN

### `POST /api/v1/payment-method/list`

**Request (optional):** `{ "active": true }` hoặc `null`

**Response — `ApiResponse<List<PaymentMethodResponseDTO>>`:**
```json
{
  "data": [
    { "code": "CASH", "name": "Tiền mặt", "active": true, "displayOrder": 1 },
    { "code": "BANK_TRANSFER", "name": "Chuyển khoản", "active": true, "displayOrder": 2 }
  ]
}
```

> **Đặc biệt:** Dữ liệu load từ **Enum ảo** (không query DB), sắp xếp theo `displayOrder`.

---

## 12. MODULE 8: INVOICE — HOÁ ĐƠN & THANH TOÁN

### 12.1. `POST /api/v1/invoice/count`

**Request:** `{}` hoặc `null`

**Response — `ApiResponse<InvoiceCountResponseDTO>`:**
```json
{
  "data": {
    "totalInvoices": 12,
    "nextInvoiceCode": "HD-20260331-0013"
  }
}
```
> Đếm invoice trong ngày hiện tại. Sinh mã format: `HD-yyyyMMdd-xxxx` (zero-padded 4 chữ số).

### 12.2. `POST /api/v1/invoice/create` ⭐ CRITICAL

**Request:**
```json
{
  "tableId": 1,
  "paymentMethod": "CASH",
  "paymentStatus": "PENDING",
  "totalMoney": 185000,
  "guestCount": 2,
  "invoiceDetails": [
    { "dishId": 1, "quantity": 5, "unitPrice": 25000 },
    { "dishId": 3, "quantity": 2, "unitPrice": 30000 }
  ]
}
```

**Response — `ApiResponse<InvoiceResponseDTO>`:**
```json
{
  "data": {
    "invoiceId": 13,
    "invoiceCode": "HD-20260331-0013",
    "tableId": 1,
    "tableCode": "TB-101",
    "tableName": "Bàn 1",
    "accountId": 1,
    "accountUsername": "admin",
    "accountFullName": "Admin",
    "paymentStatus": "PENDING",
    "paymentStatusName": "Chờ thanh toán",
    "paymentMethod": "CASH",
    "paymentMethodName": "Tiền mặt",
    "guestCount": 2,
    "totalMoney": 185000,
    "active": true,
    "createdTime": "2026-03-31T15:00:00",
    "uriVnPay": null
  }
}
```

**Business Logic (step-by-step):**
1. Validate `paymentMethod` hợp lệ (CASH / BANK_TRANSFER)
2. `paymentStatus` mặc định `PENDING` nếu không truyền
3. **Verify `totalMoney`** phải KHỚP CHÍNH XÁC tổng grouped dishes đang checkout của bàn (query lại native SQL và so sánh). Nếu không khớp → throw error.
4. Sinh `invoiceCode` = `HD-yyyyMMdd-xxxx`
5. Save invoice + invoice detail lines
6. **Nếu `paymentStatus = PAID`** → gọi `finalizeSuccessfulPayment()`:
   - Mark TẤT CẢ order chưa hoàn tất của bàn → `DONE`
   - Recompute trạng thái bàn → `AVAILABLE` hoặc `BOOKED`
7. **Nếu `BANK_TRANSFER` + `PENDING`** → build `uriVnPay` URL redirect

### 12.3. `POST /api/v1/invoice/confirm-payment` ⭐ CRITICAL

**Request:**
```json
{
  "invoiceId": 13,
  "paymentStatus": "PAID"
}
```

**Response — `ApiResponse<InvoiceConfirmPaymentResponseDTO>`:**
```json
{
  "data": {
    "invoiceId": 13,
    "invoiceCode": "HD-20260331-0013",
    "paymentStatus": "PAID",
    "paymentStatusName": "Đã thanh toán"
  }
}
```

**Side Effects khi `PAID`:**
1. Tất cả order chưa hoàn tất của bàn → `DONE`
2. Recompute trạng thái bàn → `AVAILABLE` / `BOOKED`

### 12.4. `POST /api/v1/invoice/detail`

**Request:** `{ "invoiceId": 13 }`

**Response — `ApiResponse<InvoiceDetailResponseDTO>`:**
```json
{
  "data": {
    "invoiceId": 13,
    "invoiceCode": "HD-20260331-0013",
    "tableId": 1,
    "tableCode": "TB-101",
    "tableName": "Bàn 1",
    "accountId": 1,
    "accountUsername": "admin",
    "accountFullName": "Admin",
    "paymentStatus": "PAID",
    "paymentStatusName": "Đã thanh toán",
    "paymentMethod": "CASH",
    "paymentMethodName": "Tiền mặt",
    "guestCount": 2,
    "totalMoney": 185000,
    "active": true,
    "createdTime": "2026-03-31T15:00:00",
    "uriVnPay": null,
    "invoiceDetails": [
      {
        "invoiceDetailId": 1,
        "dishId": 1,
        "dishCode": "CF-001",
        "dishName": "Cà phê sữa đá",
        "quantity": 5,
        "unitPrice": 25000,
        "lineTotal": 125000
      },
      {
        "invoiceDetailId": 2,
        "dishId": 3,
        "dishCode": "CF-003",
        "dishName": "Trà đào",
        "quantity": 2,
        "unitPrice": 30000,
        "lineTotal": 60000
      }
    ]
  }
}
```

---

## 13. LUỒNG NGHIỆP VỤ END-TO-END

### Flow 1: Hiển Thị Bàn
```
[Page Load] 
  → POST /table/search (keyword, floor, tableStatus filters)
  → Render grid bàn với màu theo tableStatus:
      AVAILABLE (xanh) | OCCUPIED (đỏ/cam) | BOOKED (vàng)
  → Mỗi bàn hiển thị: tableName, tableCode, totalBooking, lastBookingTime
```

### Flow 2: Đặt Bàn
```
[Click "Đặt bàn" trên 1 bàn]
  → POST /table-booking/search (tableId) → hiện lịch sử booking
  → [User click "Đặt bàn mới"]
     → Chọn bàn, nhập ngày/giờ, tên khách, SĐT, cọc
     → POST /table-booking/create
     → Backend tự recompute trạng thái bàn
     → Reload danh sách bàn: POST /table/search

[Sửa booking]
  → POST /table-booking/update (bookingId, fields mới)
  → Reload

[Huỷ/Xác nhận booking]
  → POST /table-booking/update-status (bookingId, status mới)
  → Reload
```

### Flow 3: Gọi Món 
```
[Click "Gọi món" trên 1 bàn]
  → POST /dish-order/list-by-table (tableId) → hiện lịch sử order
  → [User click "Đặt món mới"]
     → POST /dish-category/list → render filter tabs
     → POST /dish/list → render menu grid
     → User chọn món, điều chỉnh số lượng ±, thêm ghi chú
     → POST /dish-order/create { tableId, dishOrderDetails[] }
     → Backend: snapshot giá, recompute bàn → OCCUPIED
     → Reload danh sách bàn + order history

[Sửa đơn đã có]
  → POST /dish-order-detail/list-by-order (dishOrderId) → load chi tiết
  → User chỉnh sửa trên UI
  → POST /dish-order/update { dishOrderId, tableId, dishOrderDetails[] }
  → Backend: hard delete details cũ → tạo mới → recompute

[Huỷ đơn]
  → POST /dish-order/update-status { dishOrderId, "CANCELLED" }
  → Backend: recompute bàn (nếu hết order → AVAILABLE/BOOKED)
```

### Flow 4: Thanh Toán 
```
[Click "Thanh toán" trên 1 bàn OCCUPIED]
  → POST /table/detail (tableId) → lấy tên bàn
  → POST /dish-order-detail/list-by-table (tableId) → lấy grouped dishes
  → POST /invoice/count → sinh mã hoá đơn
  → POST /payment-method/list → render dropdown PTTT
  → Hiển thị modal: bảng chi tiết món, tổng tiền, chọn PTTT

  → [User click "Xác nhận thanh toán"]
     → POST /invoice/create {
         tableId, paymentMethod, paymentStatus: "PENDING",
         totalMoney, guestCount, invoiceDetails[]
       }
     → Nhận invoiceId

     → [Nếu CASH (Tiền mặt)]
        → POST /invoice/detail (invoiceId) → render biên lai + QR VietQR
        → [User xác nhận "Đã thu tiền"]
           → POST /invoice/confirm-payment { invoiceId, paymentStatus: "PAID" }
           → Backend: mark orders → DONE, bàn → AVAILABLE/BOOKED
           → Reload bàn

     → [Nếu BANK_TRANSFER (Chuyển khoản)]
        → Dùng uriVnPay từ response → redirect/popup VNPay
        → Khi callback success:
           → POST /invoice/confirm-payment { invoiceId, paymentStatus: "PAID" }
           → Reload bàn
```

---

## 14. RESPONSE ENVELOPE & ERROR HANDLING

### Success Response Wrapper
```typescript
// TypeScript type reference cho FE
interface ApiResponse<T> {
  status: number;       // 200
  message: string;      // "ACTION_SUCCESS"
  data?: T;             // payload
}

interface PageResponse<T> {
  data: T;
  pageNo: number;       // 0-indexed
  pageSize: number;
  totalElements: number;
  totalPages: number;
}
```

### Error Response
```typescript
interface ErrorResponse {
  timestamp: string;    // ISO 8601 with timezone
  status: number;       // HTTP status code
  error: string;        // "Bad Request"
  code: string;         // "VALIDATION_ERROR"
  message: string;      // human-readable
  details?: object;     // field-level errors
  path: string;         // request URI
}
```

### PageFilterRequest Base (cho các search API)
```typescript
interface PageFilterRequest {
  page?: number;       // 0-indexed, default 0
  limit?: number;      // default 10, max varies
  sortField?: string;  // tên field entity
  sortDir?: string;    // "asc" | "desc"
}
```

---

## 15. BUSINESS RULES TỔNG HỢP

### Rule 1: Trạng thái bàn luôn tự động recompute
- Backend tự tính dựa trên: order chưa hoàn tất → `OCCUPIED` > booking 2h → `BOOKED` > `AVAILABLE`
- Frontend **KHÔNG** cần tự đổi trạng thái bàn, chỉ reload lại `/table/search`

### Rule 2: Giá món snapshot tại thời điểm gọi
- Khi tạo/cập nhật order, `DishOrderDetail.price` = `Dish.price` tại thời điểm đó
- Nếu quán đổi giá menu sau, các order cũ vẫn giữ giá cũ

### Rule 3: Grouped checkout loại CANCELLED và DONE
- API `/dish-order-detail/list-by-table` chỉ gom từ order có status ≠ CANCELLED và ≠ DONE
- Đảm bảo không thanh toán lại order đã xong hoặc đã huỷ

### Rule 4: Verify totalMoney khi tạo invoice
- Backend tính lại tổng từ grouped dishes và **so khớp** với `totalMoney` request
- Nếu không khớp → reject (400, INVALID_DATA)

### Rule 5: Thanh toán thành công → cascade update
- Khi `paymentStatus = PAID`: TẤT CẢ order chưa xong của bàn → `DONE`
- Sau đó recompute bàn (thường về AVAILABLE, trừ khi còn booking)

### Rule 6: Booking time phải ở tương lai
- `bookingTime` bắt buộc >= hiện tại, validated cả ở DTO (@FutureOrPresent) và service

### Rule 7: Account tự động từ JWT
- Mọi action tạo/cập nhật đều lấy `account` từ token JWT đang đăng nhập
- Frontend KHÔNG cần gửi `accountId`

### Rule 8: Mã hoá đơn tự sinh
- Format: `HD-yyyyMMdd-xxxx` (VD: HD-20260331-0001)
- Gọi `/invoice/count` trước để preview mã, backend tự sinh khi create

### Rule 9: VNPay URL chỉ có khi BANK_TRANSFER + PENDING
- Response invoice sẽ có `uriVnPay` khác null khi: paymentMethod = BANK_TRANSFER VÀ paymentStatus = PENDING
- Frontend dùng URL này để redirect/popup sang cổng thanh toán

### Rule 10: Hard delete cho DishOrderDetail
- Khi update order, toàn bộ detail lines cũ bị xoá vĩnh viễn và thay bằng list mới
- Frontend cần gửi lại TOÀN BỘ danh sách món khi update (không hỗ trợ update từng dòng riêng lẻ)
