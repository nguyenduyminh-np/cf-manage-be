# API Completion Grouping - Core Business Booking Manage State

## 1) Booking Domain APIs (Hoan thien)
Controller: TableBookingController
Base path: /api/v1/table-booking

- POST /search
- POST /create
- POST /update
- POST /update-status
- POST /{id}/confirm
- POST /{id}/check-in
- POST /{id}/check-out
- POST /{id}/cancel
- POST /{id}/deposit
- POST /{id}/extend
- POST /walk-in

So luong: 11 endpoint

## 2) Table Domain APIs (Hoan thien)
Controller: TableController
Base path: /api/v1/table

- POST /search
- POST /detail
- POST /update-status

So luong: 3 endpoint

## 3) Dish Order Domain APIs (Hoan thien)
Controller: DishOrderController
Base path: /api/v1/dish-order

- POST /list-by-table
- POST /create
- POST /update
- POST /update-status

So luong: 4 endpoint

## 4) Dish Order Detail Domain APIs (Hoan thien)
Controller: DishOrderDetailController
Base path: /api/v1/dish-order-detail

- POST /list-by-order
- POST /list-by-table

So luong: 2 endpoint

## 5) Invoice Domain APIs (Hoan thien)
Controller: InvoiceController
Base path: /api/v1/invoice

- POST /count
- POST /create
- POST /confirm-payment
- POST /detail

So luong: 4 endpoint

## 6) Menu Reference APIs (Hoan thien)
### Dish
Controller: DishController
Base path: /api/v1/dish

- POST /list
- POST /search

So luong: 2 endpoint

### Dish Category
Controller: DishCategoryController
Base path: /api/v1/dish-category

- POST /list

So luong: 1 endpoint

## 7) Payment Method APIs (Hoan thien)
Controller: PaymentMethodController
Base path: /api/v1/payment-method

- POST /list

So luong: 1 endpoint

## 8) Auth APIs (Ho tro luong bao mat/JWT)
Controller: AuthController
Base path: /api/v1/auth

- POST /register
- POST /login
- POST /refresh
- POST /logout
- POST /register-admin

So luong: 5 endpoint

## 9) Endpoint trong design nhung chua thay trong controller hien tai
Theo tai lieu thiet ke co de cap endpoint:
- GET /api/v1/table-booking/tables/{tableId}/available-slots

Trang thai hien tai:
- Chua tim thay endpoint nay trong cac controller dang ton tai.

## 10) Tong ket nhanh
- Tong endpoint dang co trong code: 33
- Trong do booking + table + order + invoice (core flow): 24 endpoint
- Endpoint pending so voi design tai lieu: 1 endpoint (available-slots)

## 11) Ghi chu ve phan quyen hien tai
Tat ca API nghiep vu da duoc gan gate method security theo rule:
- ROLE_ADMIN hoac ROLE_QL-*

Auth public whitelist:
- /api/v1/auth/login
- /api/v1/auth/register
- /api/v1/auth/refresh
- /api/v1/auth/logout

register-admin duoc bao ve boi method security.
