# Dashboard API – Tài liệu tích hợp FE

> **Base URL:** `/api/v1/dashboard`  
> **Auth:** Tất cả endpoint yêu cầu JWT Bearer token với role `ADMIN` hoặc `MANAGER`.  
> **Method:** Tất cả endpoint dùng `POST` (không có request body).  
> **Content-Type:** `application/json`

---

## Cấu trúc Response chung

Mọi API đều trả về envelope:

```json
{
  "status": 200,
  "message": "..._SUCCESS",
  "data": { ... }
}
```

| Field     | Type     | Mô tả                               |
|-----------|----------|-------------------------------------|
| `status`  | `number` | HTTP status code                    |
| `message` | `string` | Mã thông báo kết quả                |
| `data`    | `any`    | Payload thực tế (object hoặc array) |

---

## Tổng quan 12 Endpoints

| #  | Nhóm  | Endpoint                                    | Mô tả ngắn                         |
|----|-------|---------------------------------------------|-------------------------------------|
| 1  | KPI   | `POST /kpi`                                 | 8 chỉ số tổng quan (1 call)         |
| 2  | Chart | `POST /chart/revenue-last-7-days`           | Doanh thu 7 ngày gần nhất           |
| 3  | Chart | `POST /chart/orders-by-hour`                | Số đơn theo giờ hôm nay             |
| 4  | Chart | `POST /chart/order-status-distribution`     | Phân bổ trạng thái đơn hôm nay      |
| 5  | Chart | `POST /chart/top-dishes`                    | Top 5 món bán chạy hôm nay          |
| 6  | Chart | `POST /chart/table-status-by-floor`         | Trạng thái bàn theo tầng            |
| 7  | Chart | `POST /chart/debt-by-supplier`              | Cơ cấu công nợ nhà cung cấp         |
| 8  | Table | `POST /table/processing-orders`             | Đơn đang chế biến hôm nay           |
| 9  | Table | `POST /table/upcoming-bookings`             | Booking sắp đến (2 giờ tới)         |
| 10 | Table | `POST /table/stock-alerts`                  | Cảnh báo tồn kho                    |
| 11 | Table | `POST /table/pending-invoices`              | Hóa đơn chưa thanh toán             |
| 12 | Table | `POST /table/draft-purchase-orders`         | Đơn nhập hàng đang soạn (DRAFT)     |

---

## Nhóm 1 – KPI Cards

### API 1: Lấy toàn bộ 8 KPI

```
POST /api/v1/dashboard/kpi
```

> Gom tất cả 8 chỉ số vào **một lần gọi duy nhất**, tránh gọi 8 request riêng lẻ khi tải trang.

**Request body:** _(không có)_

**Response `data`:**

```json
{
  "revenueToday": 4500000.00,
  "totalOrdersToday": 38,
  "paidOrdersToday": 30,
  "occupiedTables": 7,
  "activeStaff": 5,
  "processingOrders": 4,
  "totalSupplierDebt": 12000000.00,
  "expiringSoonStock": 3,
  "upcomingBookings": 2
}
```

| Field              | Type           | Mô tả                                                    |
|--------------------|----------------|----------------------------------------------------------|
| `revenueToday`     | `number`       | Tổng doanh thu hóa đơn đã thanh toán (PAID) hôm nay     |
| `totalOrdersToday` | `number`       | Tổng số đơn hàng tạo trong ngày                          |
| `paidOrdersToday`  | `number`       | Số đơn hàng đã thanh toán hôm nay                        |
| `occupiedTables`   | `number`       | Số bàn đang có khách (`OCCUPIED`)                        |
| `activeStaff`      | `number`       | Nhân viên / thu ngân đang hoạt động                      |
| `processingOrders` | `number`       | Đơn đang chờ bếp chế biến (`PROCESSING`) hôm nay        |
| `totalSupplierDebt`| `number`       | Tổng nợ nhà cung cấp chưa thanh toán                     |
| `expiringSoonStock`| `number`       | Số lô tồn kho hết hạn trong 30 ngày tới                 |
| `upcomingBookings` | `number`       | Booking sắp đến trong 2 giờ tới                          |

---

## Nhóm 2 – Biểu đồ (Charts)

### API 2: Doanh thu 7 ngày gần nhất

```
POST /api/v1/dashboard/chart/revenue-last-7-days
```

> Dùng cho **biểu đồ đường (Line Chart)**. Bao gồm ngày hiện tại và 6 ngày trước.

**Response `data`:** `Array`

```json
[
  { "date": "2026-05-01", "dailyRevenue": 3200000.00 },
  { "date": "2026-05-02", "dailyRevenue": 4100000.00 },
  { "date": "2026-05-07", "dailyRevenue": 4500000.00 }
]
```

| Field          | Type     | Mô tả                                      |
|----------------|----------|--------------------------------------------|
| `date`         | `string` | Ngày định dạng `yyyy-MM-dd`                |
| `dailyRevenue` | `number` | Tổng doanh thu hóa đơn PAID trong ngày đó |

> ⚠️ Nếu một ngày không có doanh thu, ngày đó **sẽ không xuất hiện** trong mảng. FE cần tự fill giá trị `0` cho các ngày thiếu khi vẽ biểu đồ.

---

### API 3: Số đơn theo giờ trong ngày hôm nay

```
POST /api/v1/dashboard/chart/orders-by-hour
```

> Dùng cho **biểu đồ cột (Bar Chart)**, trục X là giờ (0–23), trục Y là số đơn.

**Response `data`:** `Array`

```json
[
  { "hour": 8,  "orderCount": 5 },
  { "hour": 9,  "orderCount": 12 },
  { "hour": 12, "orderCount": 20 },
  { "hour": 14, "orderCount": 8 }
]
```

| Field        | Type     | Mô tả                                   |
|--------------|----------|-----------------------------------------|
| `hour`       | `number` | Giờ trong ngày (0 – 23)                 |
| `orderCount` | `number` | Số đơn hàng được tạo trong giờ đó       |

> ⚠️ Tương tự API 2, các giờ không có đơn sẽ không xuất hiện. FE fill `0` cho các giờ thiếu.

---

### API 4: Phân bổ trạng thái đơn hàng hôm nay

```
POST /api/v1/dashboard/chart/order-status-distribution
```

> Dùng cho **biểu đồ bánh (Pie / Donut Chart)**.

**Response `data`:** `Array`

```json
[
  { "status": "Đang chế biến", "orderCount": 4 },
  { "status": "Hoàn thành",    "orderCount": 22 },
  { "status": "Đã thanh toán", "orderCount": 10 },
  { "status": "Đã hủy",        "orderCount": 2 }
]
```

| Field        | Type     | Mô tả                                            |
|--------------|----------|--------------------------------------------------|
| `status`     | `string` | Tên trạng thái lấy từ bảng `dish_order_status`   |
| `orderCount` | `number` | Số đơn ở trạng thái này trong ngày hôm nay       |

---

### API 5: Top 5 món bán chạy hôm nay

```
POST /api/v1/dashboard/chart/top-dishes
```

> Dùng cho **biểu đồ ngang (Horizontal Bar Chart)**. Chỉ tính đơn không bị hủy.

**Response `data`:** `Array` (tối đa 5 items, sắp xếp giảm dần theo số lượng)

```json
[
  { "dishName": "Cà phê sữa đá", "totalQuantity": 45 },
  { "dishName": "Trà tắc",       "totalQuantity": 38 },
  { "dishName": "Bánh mì",       "totalQuantity": 27 },
  { "dishName": "Sinh tố bơ",    "totalQuantity": 20 },
  { "dishName": "Nước cam",      "totalQuantity": 15 }
]
```

| Field           | Type     | Mô tả                                      |
|-----------------|----------|--------------------------------------------|
| `dishName`      | `string` | Tên món ăn                                 |
| `totalQuantity` | `number` | Tổng số lượng bán ra trong ngày hôm nay    |

---

### API 6: Trạng thái bàn theo tầng

```
POST /api/v1/dashboard/chart/table-status-by-floor
```

> Dùng cho **biểu đồ cột chồng (Stacked Bar Chart)**, trục X là tầng.

**Response `data`:** `Array`

```json
[
  { "floor": 1, "occupied": 5, "available": 3 },
  { "floor": 2, "occupied": 2, "available": 6 }
]
```

| Field       | Type     | Mô tả                             |
|-------------|----------|-----------------------------------|
| `floor`     | `number` | Số tầng                           |
| `occupied`  | `number` | Số bàn đang có khách (`OCCUPIED`) |
| `available` | `number` | Số bàn đang trống (`AVAILABLE`)   |

---

### API 7: Cơ cấu công nợ nhà cung cấp

```
POST /api/v1/dashboard/chart/debt-by-supplier
```

> Dùng cho **biểu đồ bánh (Pie Chart)**. Chỉ tính các khoản nợ chưa thanh toán (`is_paid = 0`).

**Response `data`:** `Array` (sắp xếp giảm dần theo `debtAmount`)

```json
[
  { "supplierName": "Công ty Vinamilk",  "debtAmount": 5000000.00 },
  { "supplierName": "NCC Hương Cafe",    "debtAmount": 3200000.00 },
  { "supplierName": "Đại lý Trà Thái",  "debtAmount": 1800000.00 }
]
```

| Field          | Type     | Mô tả                                       |
|----------------|----------|---------------------------------------------|
| `supplierName` | `string` | Tên nhà cung cấp                            |
| `debtAmount`   | `number` | Tổng số tiền nợ chưa trả của nhà cung cấp   |

---

## Nhóm 3 – Bảng danh sách nhanh (Quick Tables)

### API 8: Đơn hàng đang chế biến hôm nay

```
POST /api/v1/dashboard/table/processing-orders
```

> Hiển thị danh sách đơn có status `PROCESSING` trong ngày, sắp xếp mới nhất lên đầu.

**Response `data`:** `Array`

```json
[
  {
    "orderId": 42,
    "tableName": "Bàn 05",
    "createdAt": "2026-05-07T13:15:00Z",
    "itemsSummary": "Cà phê đen x2, Bánh mì x1, Trà đào x1"
  }
]
```

| Field          | Type     | Mô tả                                                          |
|----------------|----------|----------------------------------------------------------------|
| `orderId`      | `number` | ID đơn hàng                                                    |
| `tableName`    | `string` | Tên bàn đang phục vụ                                           |
| `createdAt`    | `string` | Thời điểm tạo đơn (ISO 8601 UTC)                               |
| `itemsSummary` | `string` | Tóm tắt món ăn: `"Tên món xSố lượng, ..."` (do SQL ghép lại) |

---

### API 9: Booking sắp đến trong 2 giờ tới

```
POST /api/v1/dashboard/table/upcoming-bookings
```

> Lọc booking có `expected_arrive_time` trong khoảng `[NOW, NOW + 2h]`, bỏ qua `CANCELLED` và `EXPIRED`. Sắp xếp theo giờ đến tăng dần.

**Response `data`:** `Array`

```json
[
  {
    "expectedArriveTime": "2026-05-07T14:30:00Z",
    "customerName": "Nguyễn Văn A",
    "phoneNumber": "0901234567",
    "tableName": "Bàn VIP 01",
    "note": "Sinh nhật, cần chuẩn bị trước bánh kem"
  }
]
```

| Field                | Type     | Mô tả                                  |
|----------------------|----------|----------------------------------------|
| `expectedArriveTime` | `string` | Giờ đến dự kiến (ISO 8601 UTC)         |
| `customerName`       | `string` | Tên khách hàng                         |
| `phoneNumber`        | `string` | Số điện thoại liên hệ                  |
| `tableName`          | `string` | Tên bàn đã đặt                         |
| `note`               | `string` | Ghi chú của khách (có thể `null`)      |

---

### API 10: Cảnh báo tồn kho

```
POST /api/v1/dashboard/table/stock-alerts
```

> Trả về các lô hàng **hết hạn trong 7 ngày tới** HOẶC **số lượng ≤ 5**. Sắp xếp theo ngày hết hạn tăng dần (gần hết hạn nhất lên đầu).

**Response `data`:** `Array`

```json
[
  {
    "ingredientName": "Sữa tươi Vinamilk",
    "batchId": 15,
    "quantity": 3.00,
    "expirationAt": "2026-05-10T00:00:00Z",
    "warehouseName": "Kho chính"
  },
  {
    "ingredientName": "Trà Ô Long",
    "batchId": 22,
    "quantity": 2.00,
    "expirationAt": "2026-05-14T00:00:00Z",
    "warehouseName": "Kho phụ"
  }
]
```

| Field            | Type     | Mô tả                                                       |
|------------------|----------|-------------------------------------------------------------|
| `ingredientName` | `string` | Tên nguyên liệu                                             |
| `batchId`        | `number` | ID lô hàng (`stock_level.id`)                               |
| `quantity`       | `number` | Số lượng còn lại trong lô                                   |
| `expirationAt`   | `string` | Ngày hết hạn của lô (ISO 8601 UTC)                          |
| `warehouseName`  | `string` | Tên kho chứa lô hàng                                        |

> 💡 **Gợi ý UI:** Tô màu đỏ nếu `expirationAt <= NOW + 3 ngày` hoặc `quantity <= 2`; màu vàng cho các trường hợp còn lại.

---

### API 11: Hóa đơn chưa thanh toán

```
POST /api/v1/dashboard/table/pending-invoices
```

> Lấy tất cả hóa đơn chưa ở trạng thái `PAID` hoặc `ĐÃ HỦY`. Sắp xếp theo thời gian tạo **tăng dần** (hóa đơn chờ lâu nhất lên đầu).

**Response `data`:** `Array`

```json
[
  {
    "invoiceCode": "HD-20260507-001",
    "tableName": "Bàn 03",
    "totalAmount": 125000.00,
    "createdAt": "2026-05-07T11:00:00Z",
    "customerName": "Trần Thị B"
  }
]
```

| Field          | Type     | Mô tả                                         |
|----------------|----------|-----------------------------------------------|
| `invoiceCode`  | `string` | Mã hóa đơn                                    |
| `tableName`    | `string` | Tên bàn tương ứng                             |
| `totalAmount`  | `number` | Tổng tiền hóa đơn                             |
| `createdAt`    | `string` | Thời điểm tạo hóa đơn (ISO 8601 UTC)          |
| `customerName` | `string` | Tên khách hàng (có thể `null`)                |

---

### API 12: Đơn nhập hàng đang soạn (DRAFT)

```
POST /api/v1/dashboard/table/draft-purchase-orders
```

> Lấy các đơn nhập hàng có `payment_status = 'DRAFT'` và `is_active = 1`. Sắp xếp theo thời gian tạo **giảm dần**.

**Response `data`:** `Array`

```json
[
  {
    "purchaseOrderCode": "PO-20260507-003",
    "totalAmount": 3500000.00,
    "createdAt": "2026-05-07T09:00:00Z",
    "supplierName": "Công ty Vinamilk"
  }
]
```

| Field               | Type     | Mô tả                                    |
|---------------------|----------|------------------------------------------|
| `purchaseOrderCode` | `string` | Mã đơn nhập hàng                         |
| `totalAmount`       | `number` | Tổng giá trị đơn (có thể `null` nếu chưa có) |
| `createdAt`         | `string` | Thời điểm tạo đơn (ISO 8601 UTC)         |
| `supplierName`      | `string` | Tên nhà cung cấp                         |

---

## Ghi chú cho FE team

### Xử lý kiểu dữ liệu

| BE type       | JSON type  | Ghi chú FE                                                              |
|---------------|------------|-------------------------------------------------------------------------|
| `BigDecimal`  | `number`   | Dùng để hiển thị tiền – format với `toLocaleString('vi-VN')` hoặc thư viện |
| `Instant`     | `string`   | ISO 8601 UTC (kết thúc bằng `Z`). Dùng `new Date(value)` hoặc dayjs/moment để parse |
| `Integer`     | `number`   | Số nguyên thông thường                                                  |
| `null`        | `null`     | Trường có thể `null` – FE cần xử lý trường hợp này, đặc biệt `note`, `customerName` |

### Gợi ý chiến lược tải dữ liệu

```
Khi vào trang Dashboard:
  1. Gọi POST /kpi                            → Hiển thị 8 KPI Card ngay
  2. Gọi song song (Promise.all) 6 chart API  → Render biểu đồ
  3. Gọi song song (Promise.all) 5 table API  → Render bảng danh sách

Refresh định kỳ (optional):
  - KPI + Table: mỗi 30–60 giây (real-time)
  - Chart: mỗi 5 phút (ít thay đổi hơn)
```

### Xử lý lỗi

Nếu server trả về HTTP `4xx` hoặc `5xx`, cấu trúc lỗi như sau:

```json
{
  "timestamp": "2026-05-07T13:00:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "code": "INTERNAL_SERVER_ERROR",
  "message": "Lỗi hệ thống nội bộ, vui lòng thử lại sau",
  "path": "/api/v1/dashboard/kpi"
}
```

FE nên bắt lỗi và hiển thị thông báo phù hợp, đồng thời không để widget bị vỡ layout khi data là `null` hoặc mảng rỗng `[]`.
