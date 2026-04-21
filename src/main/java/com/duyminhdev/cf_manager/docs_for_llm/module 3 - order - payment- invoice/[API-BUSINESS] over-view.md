Dưới đây là **danh sách đầy đủ các API** được sử dụng trong hệ thống, được **nhóm theo nghiệp vụ** và trình bày chi tiết **API contract** cho từng endpoint.

---

## 📦 1. Nhóm API Quản lý món ăn (Dish)

### 1.1. Lấy danh sách món (phân trang server‑side)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `POST /Dish/api/list-server-side`                           |
| **Mô tả**           | Cung cấp dữ liệu cho DataTable quản lý món (có phân trang, tìm kiếm, sắp xếp). |
| **Request Body**    | `DishDTParameters` (kế thừa DataTables parameters)          |
| **Response**        | `DTResult<DishViewModel>` (không bọc `CoffeeManagementResponse`) |

<details>
<summary><b>Request Body Schema (JSON)</b></summary>

```json
{
  "draw": 1,
  "start": 0,
  "length": 10,
  "searchAll": "cà phê",
  "dishCategoryId": 2,
  "order": [{ "column": 0, "dir": "asc" }],
  "columns": [
    { "data": "id", "searchable": true, "search": { "value": "" } },
    { "data": "dishName", "searchable": true, "search": { "value": "" } }
  ]
}
```
</details>

<details>
<summary><b>Response Example</b></summary>

```json
{
  "draw": 1,
  "recordsTotal": 120,
  "recordsFiltered": 45,
  "data": [
    {
      "id": 1,
      "dishCode": "CF001",
      "dishName": "Cà phê đen",
      "price": 20000,
      "photo": "images/dish/cfden.jpg",
      "createdTime": "2025-01-01T10:00:00",
      "active": true,
      "dishCategoryId": 2,
      "dishCategoryName": "Cà phê"
    }
  ]
}
```
</details>

---

### 1.2. Tìm kiếm món cho Select2

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `POST /Dish/api/Search`                                     |
| **Mô tả**           | Tìm kiếm món theo tên để hiển thị dropdown chọn món khi đặt hàng. |
| **Request Body**    | `Select2VM`                                                 |
| **Response**        | `CoffeeManagementResponse` chứa `List<Dish>`                |

<details>
<summary><b>Request Body Schema (JSON)</b></summary>

```json
{
  "searchString": "Cà phê"
}
```
</details>

<details>
<summary><b>Response Example</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": [
    {
      "id": 1,
      "dishCode": "CF001",
      "dishName": "Cà phê sữa đá",
      "price": 25000,
      "photo": "images/dish/cafe.jpg",
      "createdTime": "2025-01-01T10:00:00",
      "active": true,
      "dishCategoryId": 2
    }
  ]
}
```
</details>

---

### 1.3. Thêm hoặc cập nhật món (có upload ảnh)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `POST /Dish/api/addOrUpdate`                                |
| **Mô tả**           | Thêm mới hoặc chỉnh sửa thông tin món, hỗ trợ upload file ảnh. |
| **Content-Type**    | `multipart/form-data`                                       |
| **Request Form**    | `DishDto` (dạng form fields)                                |
| **Response**        | `CoffeeManagementResponse` chứa `DishDto`                   |

<details>
<summary><b>Request Form Fields</b></summary>

| Field          | Type   | Required | Mô tả                                   |
|----------------|--------|----------|----------------------------------------|
| Id             | int    | Có       | 0 = thêm mới, >0 = cập nhật            |
| DishName       | string | Có       | Tên món                                |
| Price          | decimal| Có       | Giá                                    |
| DishCategoryId | int    | Có       | ID danh mục                            |
| Image          | file   | Không    | Ảnh upload (nếu có)                    |
| Photo          | string | Không    | Đường dẫn ảnh cũ (khi cập nhật không đổi) |
</details>

<details>
<summary><b>Response Example</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": {
    "id": 1,
    "dishName": "Cà phê sữa",
    "price": 25000,
    "photo": "images/dish/cafe.jpg",
    "dishCategoryId": 2,
    "dishCategoryName": "Cà phê"
  }
}
```
</details>

---

### 1.4. Lấy top món bán chạy

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /Dish/api/popular`                                     |
| **Mô tả**           | Trả về danh sách món bán chạy nhất trong khoảng thời gian.  |
| **Query Params**    | `count`, `startDate`, `endDate`                             |
| **Response**        | `CoffeeManagementResponse` chứa `List<PopularDishModel>`    |

<details>
<summary><b>Query Parameters</b></summary>

| Param     | Type     | Required | Default | Mô tả                   |
|-----------|----------|----------|---------|-------------------------|
| count     | int      | Không    | 5       | Số lượng món trả về     |
| startDate | DateTime | Không    | null    | Lọc từ ngày (yyyy-MM-dd)|
| endDate   | DateTime | Không    | null    | Lọc đến ngày (yyyy-MM-dd)|
</details>

<details>
<summary><b>Response Example</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": [
    {
      "id": 1,
      "dishCode": "CF001",
      "dishName": "Cà phê sữa đá",
      "price": 25000,
      "photo": "...",
      "categoryName": "Cà phê",
      "salesCount": 156,
      "createdTime": "2025-01-01T10:00:00",
      "active": true
    }
  ]
}
```
</details>

---

### 1.5. Thống kê tổng quan món

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /Dish/api/getDishStatistics`                           |
| **Mô tả**           | Thống kê tổng số món, số danh mục, giá trung bình.          |
| **Response**        | `CoffeeManagementResponse` chứa `DishStaticDto`             |

<details>
<summary><b>Response Example</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": {
    "totalDishCate": 5,
    "totalDish": 45,
    "avgDishPrice": 35000
  }
}
```
</details>

---

### 1.6. Các API cơ bản khác (CRUD)

| Method | Endpoint                           | Mô tả                                   | Request Body      | Response                               |
|--------|------------------------------------|-----------------------------------------|-------------------|----------------------------------------|
| GET    | `/Dish/api/List`                   | Lấy tất cả món đang active              | _none_            | `CoffeeManagementResponse<List<Dish>>` |
| GET    | `/Dish/api/Detail/{Id}`            | Chi tiết một món                        | Path `Id`         | `CoffeeManagementResponse<Dish>`       |
| GET    | `/Dish/api/ListPaging`             | Phân trang đơn giản                     | Query `pageIndex`, `pageSize` | `CoffeeManagementResponse<List<Dish>>` |
| POST   | `/Dish/api/Add`                    | Thêm món mới (không upload ảnh)         | `Dish`            | `CoffeeManagementResponse<Dish>` (201) |
| POST   | `/Dish/api/Update`                 | Cập nhật món                            | `Dish`            | `CoffeeManagementResponse<Dish>`       |
| POST   | `/Dish/api/Delete`                 | Xóa mềm (active = false)                | `Dish`            | `CoffeeManagementResponse<Dish>`       |
| POST   | `/Dish/api/DeletePermanently`      | Xóa cứng khỏi DB                        | `Dish`            | `CoffeeManagementResponse<Dish>`       |
| GET    | `/Dish/api/Count`                  | Đếm số món active                       | _none_            | `int` (không wrapper)                  |
| POST   | `/Dish/api/list-popular-server-side`| Phân trang server‑side món bán chạy    | `DishDTParameters`| `DTResult<PopularDishModel>`           |

---

## 📦 2. Nhóm API Đặt món (DishOrder)

### 2.1. Lấy lịch sử đơn của bàn (chưa thanh toán)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /DishOrder/api/DetailByTableId/{tableId}`              |
| **Mô tả**           | Lấy danh sách các đơn đặt món chưa thanh toán của một bàn.  |
| **Path Param**      | `tableId` (int)                                             |
| **Response**        | `CoffeeManagementResponse` chứa `List<DishOrderViewModel>` (không bao gồm `DishOrderDetails`) |

<details>
<summary><b>Response Example</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": [
    {
      "id": 101,
      "tableId": 5,
      "accountId": 12,
      "ordererName": "Nguyễn Văn A",
      "description": "Ít đá",
      "dishOrderStatusId": 1,
      "dishOrderStatusName": "Đang xử lý",
      "createdTime": "2025-03-01T14:30:00"
    }
  ]
}
```
</details>

---

### 2.2. Lấy danh sách món đã đặt của bàn (gộp theo món – dùng cho thanh toán)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /DishOrder/api/DishDetailByTableId/{tableId}`          |
| **Mô tả**           | Lấy tất cả món đã order của bàn, gộp theo `DishId` và tính tổng số lượng. Dùng để tạo hóa đơn. |
| **Path Param**      | `tableId` (int)                                             |
| **Response**        | `CoffeeManagementResponse` chứa `List<DishOrderViewModel>` (có `DishOrderDetails` đã gộp) |

<details>
<summary><b>Response Example</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": [
    {
      "id": 101,
      "tableId": 5,
      "accountId": 12,
      "ordererName": "Nguyễn Văn A",
      "tableName": "Bàn 5",
      "description": "Ít đá",
      "dishOrderStatusId": 2,
      "dishOrderStatusName": "Hoàn thành",
      "createdTime": "2025-03-01T14:30:00",
      "dishOrderDetails": [
        {
          "dishId": 1,
          "dishName": "Cà phê sữa đá",
          "quantity": 2,
          "price": 25000,
          "totalPrice": 50000
        },
        {
          "dishId": 3,
          "dishName": "Trà đào",
          "quantity": 1,
          "price": 30000,
          "totalPrice": 30000
        }
      ]
    }
  ]
}
```
> *Lưu ý*: Trường `price` và `totalPrice` được tính toán động trong repository, không có trong class `DishOrderDetailVM` gốc.

</details>

---

### 2.3. Tạo mới / Cập nhật đơn đặt món (API chính)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `POST /DishOrder/api/AddOrUpdate`                           |
| **Mô tả**           | Tạo đơn mới hoặc cập nhật đơn hiện có (thêm/xóa/sửa chi tiết). Kèm cập nhật trạng thái bàn và gửi thông báo SignalR. |
| **Request Body**    | `DishOrderViewModel`                                        |
| **Response**        | `CoffeeManagementResponse` chứa `DishOrderViewModel` (status 201) |

<details>
<summary><b>Request Body Schema (JSON)</b></summary>

```json
{
  "id": 0,
  "tableId": 5,
  "accountId": 12,
  "description": "Ít đá, ít đường",
  "dishOrderDetails": [
    {
      "id": 0,
      "dishId": 1,
      "quantity": 2,
      "note": "Ít đá"
    },
    {
      "id": 0,
      "dishId": 3,
      "quantity": 1,
      "note": "Thêm trân châu"
    }
  ]
}
```
| Field            | Type   | Required | Mô tả                                                       |
|------------------|--------|----------|-------------------------------------------------------------|
| id               | int    | Có       | 0 = đơn mới, >0 = cập nhật đơn hiện tại                     |
| tableId          | int    | Có       | ID bàn                                                      |
| accountId        | int    | Có       | ID nhân viên (backend tự gán từ token nếu thiếu)            |
| description      | string | Không    | Ghi chú chung cho đơn                                       |
| dishOrderDetails | array  | Có       | Danh sách món đặt                                           |
| └─ id            | int    | Có       | 0 = thêm chi tiết mới, >0 = cập nhật chi tiết có sẵn        |
| └─ dishId        | int    | Có       | ID món                                                      |
| └─ quantity      | int    | Có       | Số lượng                                                    |
| └─ note          | string | Không    | Ghi chú riêng cho món                                       |
</details>

<details>
<summary><b>Response Example (201 Created)</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": {
    "id": 101,
    "tableId": 5,
    "accountId": 12,
    "description": "Ít đá, ít đường",
    "dishOrderStatusId": 1,
    "dishOrderStatusName": "Đang xử lý",
    "createdTime": "2025-03-01T15:00:00",
    "dishOrderDetails": [...]
  }
}
```
</details>

---

### 2.4. Cập nhật trạng thái đơn (dùng cho pha chế hoàn thành / hủy)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `POST /DishOrder/api/Update`                                |
| **Mô tả**           | Cập nhật trạng thái đơn (Đang xử lý → Hoàn thành / Hủy). Khi hoàn thành sẽ gửi thông báo. |
| **Request Body**    | `DishOrder` (chỉ cần `Id` và `DishOrderStatusId`)           |
| **Response**        | `CoffeeManagementResponse` chứa `DishOrder`                 |

<details>
<summary><b>Request Body Example</b></summary>

```json
{
  "id": 101,
  "dishOrderStatusId": 2
}
```
</details>

---

### 2.5. Danh sách đơn chờ xử lý (cho nhân viên pha chế)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /DishOrder/api/ListQueue`                              |
| **Mô tả**           | Nếu role là `BARTENDER`: trả về tất cả đơn đang `PROCESSING`. Nếu role khác: trả về thông báo của user (phân trang). |
| **Query Params**    | `pageIndex`, `pageSize`                                     |
| **Response**        | `CoffeeManagementResponse` chứa `List<DishOrderViewModel>` hoặc `List<Notification>` |

---

### 2.6. Các API khác

| Method | Endpoint                               | Mô tả                                 | Request              | Response                                    |
|--------|----------------------------------------|---------------------------------------|----------------------|---------------------------------------------|
| GET    | `/DishOrder/api/List`                  | Lấy tất cả đơn                        | _none_               | `CoffeeManagementResponse<List<DishOrder>>` |
| GET    | `/DishOrder/api/Detail/{Id}`           | Chi tiết đơn                          | Path `Id`            | `CoffeeManagementResponse<DishOrder>`       |
| GET    | `/DishOrder/api/ListPaging`            | Phân trang đơn giản                   | Query `pageIndex`, `pageSize` | `CoffeeManagementResponse<List<DishOrder>>` |
| POST   | `/DishOrder/api/Add`                   | Thêm đơn (ít dùng)                    | `DishOrder`          | `CoffeeManagementResponse<DishOrder>` (201) |
| POST   | `/DishOrder/api/Delete`                | Xóa mềm đơn                           | `DishOrder`          | `CoffeeManagementResponse<DishOrder>`       |
| POST   | `/DishOrder/api/DeletePermanently`     | Xóa cứng đơn                          | `DishOrder`          | `CoffeeManagementResponse<DishOrder>`       |
| GET    | `/DishOrder/api/Count`                 | Đếm số đơn active                     | _none_               | `int`                                       |
| POST   | `/DishOrder/api/list-server-side`      | Phân trang server‑side cho DataTable  | `DishOrderDTParameters` | `DTResult<DishOrder>`                    |

---

## 📦 3. Nhóm API Thanh toán & Hóa đơn (Invoice)

### 3.1. Tạo / Cập nhật hóa đơn (API chính thanh toán)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `POST /Invoice/api/AddOrUpdateVM`                           |
| **Mô tả**           | Tạo hóa đơn mới (hoặc cập nhật), xử lý thanh toán tiền mặt hoặc tạo URL VNPAY. Cập nhật trạng thái bàn và gửi thông báo. |
| **Request Body**    | `InvoiceViewModel`                                          |
| **Response**        | `CoffeeManagementResponse` chứa `InvoiceViewModel` (có thể kèm `uriVnPay`) |

<details>
<summary><b>Request Body Schema (JSON)</b></summary>

```json
{
  "id": 0,
  "totalMoney": 110000,
  "paymentMethod": "VNPAY",
  "accountId": 12,
  "tableId": 5,
  "invoiceDetails": [
    {
      "id": 0,
      "dishId": 1,
      "quantity": 2,
      "unitPrice": 25000
    },
    {
      "id": 0,
      "dishId": 3,
      "quantity": 1,
      "unitPrice": 30000
    }
  ]
}
```
| Field          | Type    | Required | Mô tả                                                        |
|----------------|---------|----------|--------------------------------------------------------------|
| id             | int     | Có       | 0 = hóa đơn mới, >0 = cập nhật                               |
| totalMoney     | decimal | Có       | Tổng tiền sau VAT                                            |
| paymentMethod  | string  | Có       | `"CASH"` hoặc `"VNPAY"`                                      |
| accountId      | int     | Có       | ID nhân viên tạo hóa đơn                                     |
| tableId        | int     | Có       | ID bàn                                                       |
| invoiceDetails | array   | Có       | Danh sách món                                                |
| └─ dishId      | int     | Có       | ID món                                                       |
| └─ quantity    | int     | Có       | Số lượng                                                     |
| └─ unitPrice   | decimal | Có       | Đơn giá                                                      |
</details>

<details>
<summary><b>Response Example (VNPAY)</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": {
    "id": 501,
    "invoiceCode": "HD-2025-045",
    "totalMoney": 110000,
    "paymentStatus": "PENDING",
    "paymentMethod": "VNPAY",
    "uriVnPay": "https://sandbox.vnpayment.vn/paymentv2/...",
    "createdTime": "2025-03-01T15:30:00"
  }
}
```
</details>

<details>
<summary><b>Response Example (Tiền mặt)</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": {
    "id": 501,
    "invoiceCode": "HD-2025-045",
    "totalMoney": 110000,
    "paymentStatus": "PAID",
    "paymentMethod": "CASH",
    "createdTime": "2025-03-01T15:30:00"
  }
}
```
</details>

---

### 3.2. Lấy chi tiết hóa đơn (dùng hiển thị sau khi thanh toán)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /Invoice/api/InvoiceDetailVM`                          |
| **Mô tả**           | Trả về thông tin đầy đủ của hóa đơn, bao gồm tên thu ngân và chi tiết từng món. |
| **Query Param**     | `invoiceId` (int)                                           |
| **Response**        | `CoffeeManagementResponse` chứa `InvoiceVM`                 |

<details>
<summary><b>Response Example</b></summary>

```json
{
  "status": "200",
  "message": "SUCCESS",
  "data": {
    "id": 501,
    "invoiceCode": "HD-2025-045",
    "totalMoney": 110000,
    "paymentStatus": "PAID",
    "paymentMethod": "VNPAY",
    "createdTime": "2025-03-01T15:30:00",
    "cashierName": "Nguyễn Văn A",
    "invoiceDetails": [
      {
        "id": 1,
        "quantity": 2,
        "unitPrice": 25000,
        "dishName": "Cà phê sữa đá"
      },
      {
        "id": 2,
        "quantity": 1,
        "unitPrice": 30000,
        "dishName": "Trà đào"
      }
    ]
  }
}
```
</details>

---

### 3.3. Cập nhật trạng thái thanh toán (thủ công)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `POST /Invoice/api/UpdateStatus`                            |
| **Mô tả**           | Cập nhật trạng thái thanh toán của hóa đơn (ví dụ: từ PENDING sang PAID). |
| **Request Body**    | `InvoiceViewModel` (chỉ cần `Id`, `PaymentStatus`, `PaymentMethod`) |
| **Response**        | `CoffeeManagementResponse` chứa `InvoiceViewModel`          |

<details>
<summary><b>Request Body Example</b></summary>

```json
{
  "id": 501,
  "paymentStatus": "PAID",
  "paymentMethod": "CASH"
}
```
</details>

---

### 3.4. Xác nhận thanh toán thành công từ VNPAY (gọi nội bộ)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `POST /Invoice/api/PaymentSuccess`                          |
| **Mô tả**           | Được gọi sau khi VNPAY callback thành công để cập nhật trạng thái hóa đơn và ghi nhận dòng tiền. |
| **Query Params**    | `id` (int) – ID hóa đơn<br>`invoiceCode` (string) – Mã giao dịch VNPAY |
| **Response**        | `CoffeeManagementResponse` SUCCESS                          |

---

### 3.5. Các API CRUD cơ bản

| Method | Endpoint                           | Mô tả                                 | Request Body        | Response                                 |
|--------|------------------------------------|---------------------------------------|---------------------|------------------------------------------|
| GET    | `/Invoice/api/List`                | Lấy tất cả hóa đơn                    | _none_              | `CoffeeManagementResponse<List<Invoice>>`|
| GET    | `/Invoice/api/Detail/{Id}`         | Chi tiết hóa đơn (chỉ bảng Invoice)   | Path `Id`           | `CoffeeManagementResponse<Invoice>`      |
| GET    | `/Invoice/api/ListPaging`          | Phân trang đơn giản                   | Query `pageIndex`, `pageSize` | `CoffeeManagementResponse<List<Invoice>>`|
| POST   | `/Invoice/api/Add`                 | Thêm hóa đơn                          | `Invoice`           | `CoffeeManagementResponse<Invoice>` (201)|
| POST   | `/Invoice/api/Update`              | Cập nhật hóa đơn                      | `Invoice`           | `CoffeeManagementResponse<Invoice>`      |
| POST   | `/Invoice/api/Delete`              | Xóa mềm                               | `Invoice`           | `CoffeeManagementResponse<Invoice>`      |
| POST   | `/Invoice/api/DeletePermanently`   | Xóa cứng                              | `Invoice`           | `CoffeeManagementResponse<Invoice>`      |
| GET    | `/Invoice/api/Count`               | Đếm số hóa đơn active                 | _none_              | `int`                                    |
| POST   | `/Invoice/api/list-server-side`    | Phân trang server‑side DataTable      | `InvoiceDTParameters`| `DTResult<Invoice>`                      |

---

## 📦 4. Nhóm API Tích hợp VNPAY

### 4.1. Callback từ VNPAY (Return URL)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /Vnpay/api/Callback`                                   |
| **Mô tả**           | URL được VNPAY chuyển hướng về sau khi khách thanh toán xong. |
| **Query Params**    | Các tham số do VNPAY trả về (`vnp_Amount`, `vnp_ResponseCode`, `vnp_SecureHash`...) |
| **Response**        | Redirect đến trang thành công hoặc thất bại                 |

---

### 4.2. IPN (Instant Payment Notification)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /Vnpay/api/IpnAction`                                  |
| **Mô tả**           | Server‑to‑server notification từ VNPAY (đảm bảo giao dịch đã hoàn tất). |
| **Response**        | Redirect đến trang lịch sử đơn hàng (trong code hiện tại)   |

---

### 4.3. Tạo URL thanh toán (dùng test)

| Thuộc tính          | Giá trị                                                     |
|---------------------|-------------------------------------------------------------|
| **Endpoint**        | `GET /Vnpay/api/CreatePaymentUrl`                           |
| **Mô tả**           | Tạo URL thanh toán VNPAY (chủ yếu để test).                 |
| **Query Params**    | `money` (double), `description` (string)                    |
| **Response**        | `string` – URL thanh toán                                   |

---

## 📦 5. Nhóm API Quản lý bàn (Table)

| Method | Endpoint                   | Mô tả                                     | Request / Response                                      |
|--------|----------------------------|-------------------------------------------|---------------------------------------------------------|
| GET    | `/Table/api/Listdto`       | Lấy danh sách bàn kèm thông tin đặt       | Response: `CoffeeManagementResponse<List<TableDto>>`    |
| GET    | `/api/Table/{id}`          | Lấy chi tiết một bàn                      | Path `id` → `{ id, tableCode, tableName, tableStatus }` |
| PUT    | `/api/Table/{id}`          | Cập nhật trạng thái bàn                   | Body: `Table` object                                    |

---

## 📦 6. Các API hỗ trợ khác

### 6.1. Phương thức thanh toán

| Method | Endpoint                     | Mô tả                                   | Response                                      |
|--------|------------------------------|-----------------------------------------|-----------------------------------------------|
| GET    | `/paymentmethod/api/list`    | Lấy danh sách phương thức thanh toán    | `CoffeeManagementResponse<List<PaymentMethod>>` |

### 6.2. Tài khoản

| Method | Endpoint              | Mô tả                        | Response                      |
|--------|-----------------------|------------------------------|-------------------------------|
| GET    | `/api/account/{id}`   | Lấy thông tin tài khoản      | `{ id, fullName, email, ... }`|

### 6.3. Đặt bàn (TableBooking) – tóm tắt

| Method | Endpoint                                       | Mô tả                                        |
|--------|------------------------------------------------|----------------------------------------------|
| POST   | `/TableBooking/listTableBooking/{tableId}`     | Lấy lịch sử đặt bàn (server‑side)            |
| GET    | `/TableBooking/{id}`                           | Chi tiết đặt bàn                             |
| POST   | `/tableBooking/api/addOrUpdate`                | Thêm / cập nhật đặt bàn                      |
| PUT    | `/tableBooking/api/updateTableBooking/{id}`    | Cập nhật đặt bàn (đổi trạng thái)            |
| GET    | `/TableBooking/getAvailableTimes`              | Lấy giờ khả dụng đặt bàn                     |
| GET    | `/TableBooking/getByDate`                      | Lấy danh sách đặt bàn theo ngày và bàn       |

---

## 📌 Ghi chú chung

- **Wrapper Response**: Hầu hết API (trừ server‑side DataTables) trả về `CoffeeManagementResponse` với cấu trúc `{ "status": "...", "message": "...", "data": [...] }`.
- **Phân trang server‑side**: Các endpoint `list-server-side` trả về trực tiếp `DTResult<T>`.
- **Xác thực**: Một số API yêu cầu token Bearer trong header `Authorization`.

Trên đây là toàn bộ API contract được trích xuất từ mã nguồn, sắp xếp theo nghiệp vụ thực tế.