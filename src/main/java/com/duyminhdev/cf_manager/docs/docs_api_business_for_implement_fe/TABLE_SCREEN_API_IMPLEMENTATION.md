# Tài liệu triển khai API Table cho màn hình Đặt bàn

## 1. Mục tiêu màn hình

Màn hình Đặt bàn cần:

- 1 thanh filter tìm kiếm bàn theo: từ khóa, số chỗ, trạng thái, tầng.
- Nút Tìm kiếm để gọi API theo tiêu chí filter.
- 1 danh sách card hiển thị bàn theo dạng 4 cột x 3 hàng mỗi trang (12 bàn/trang).
- Nếu filter rỗng thì lấy toàn bộ danh sách bàn (phân trang).

## 2. API sử dụng

### 2.1. API chính cho danh sách card

- Method: POST
- URL: /api/v1/table/search
- Auth: Bắt buộc Bearer Token (JWT)
- Content-Type: application/json

Request DTO backend: TableSearchRequestDTO (kế thừa PageFilterRequest)

```json
{
  "page": 0,
  "limit": 12,
  "sortField": "lastBookingTime",
  "sortDir": "DESC",
  "keyword": "ban",
  "floor": 1,
  "slot": 4,
  "tableStatus": "AVAILABLE",
  "active": true
}
```

Ý nghĩa field request:

- page: trang hiện tại, bắt đầu từ 0.
- limit: số bản ghi mỗi trang. Để đúng layout 4x3 thì truyền 12.
- sortField: cột sort. Hỗ trợ: id, tableCode, tableName, tableStatus, floor, slot, totalBooking, lastBookingTime, active.
- sortDir: ASC hoặc DESC.
- keyword: tìm theo tiền tố của tableCode hoặc tableName, không phân biệt hoa thường.
- floor: lọc theo tầng.
- slot: lọc theo số chỗ.
- tableStatus: AVAILABLE | OCCUPIED | BOOKED.
- active: lọc trạng thái bản ghi còn hiệu lực.

Lưu ý xử lý mặc định:

- Nếu page null hoặc < 0 thì backend tự gán page = 0.
- Nếu limit null hoặc < 1 thì backend tự gán limit = 20.
- limit tối đa 100.
- Nếu không truyền sortField thì backend mặc định sort theo lastBookingTime DESC, sau đó dt.id DESC.

### 2.2. API phụ khi mở chi tiết một bàn (nếu UI cần)

- Method: POST
- URL: /api/v1/table/detail
- Auth: Bắt buộc Bearer Token (JWT)
- Content-Type: application/json

Request:

```json
{
  "tableId": 10
}
```

## 3. Mapping filter UI -> request API

### 3.1. Tìm kiếm bàn theo bộ lọc

- Từ khóa: map vào keyword.
- Số chỗ: map vào slot.
- Trạng thái: map vào tableStatus.
- Tầng: map vào floor.
- Nút Tìm kiếm: gọi POST /api/v1/table/search với page reset về 0.

Quy ước giá trị Tất cả:

- Nếu user chọn Tất cả thì gửi null hoặc không gửi field tương ứng.

Ví dụ request khi tất cả filter đang để Tất cả:

```json
{
  "page": 0,
  "limit": 12,
  "sortField": "lastBookingTime",
  "sortDir": "DESC",
  "active": true
}
```

Ví dụ request khi user lọc theo từ khóa + số chỗ + trạng thái + tầng:

```json
{
  "page": 0,
  "limit": 12,
  "sortField": "lastBookingTime",
  "sortDir": "DESC",
  "keyword": "ban 0",
  "slot": 4,
  "tableStatus": "OCCUPIED",
  "floor": 2,
  "active": true
}
```

## 4. Mapping response API -> card bàn

Response chuẩn:

```json
{
  "status": 200,
  "message": "SEARCH_TABLE_SUCCESS",
  "data": {
    "data": [
      {
        "tableId": 10,
        "tableCode": "T10",
        "tableName": "Bàn 10",
        "tableStatus": "OCCUPIED",
        "tableStatusName": "Đang sử dụng",
        "floor": 2,
        "slot": 8,
        "totalBooking": 12,
        "lastBookingTime": "2026-04-02T10:05:00",
        "active": true
      }
    ],
    "pageNo": 0,
    "pageSize": 12,
    "totalElements": 48,
    "totalPages": 4
  }
}
```

Mapping field cho card:

- Tiêu đề card: tableName.
- Mã bàn (nếu cần hiển thị): tableCode.
- Badge trạng thái: tableStatusName.
- Số chỗ: slot.
- Tầng: floor.
- Thông tin lịch đặt gần nhất (nếu cần): lastBookingTime.
- Thống kê số lượt đặt (nếu cần): totalBooking.

Mapping phân trang dưới danh sách card:

- Dữ liệu card: data.data.
- Trang hiện tại: data.pageNo.
- Tổng số phần tử: data.totalElements.
- Tổng số trang: data.totalPages.

Text footer gợi ý:

- Hiển thị X trên Y bàn
- X = số phần tử thực tế của data.data
- Y = data.totalElements

## 5. Quy trình gọi API đề xuất cho frontend

### 5.1. Khi mở màn hình lần đầu

1. Gọi API search với filter rỗng, page = 0, limit = 12, active = true.
2. Render 12 card đầu tiên theo layout 4x3.
3. Render thanh phân trang theo totalPages.

### 5.2. Khi bấm nút Tìm kiếm

1. Đọc giá trị các control filter.
2. Build request body.
3. Reset page = 0.
4. Gọi lại POST /api/v1/table/search.
5. Cập nhật danh sách card và phân trang.

### 5.3. Khi đổi trang

1. Giữ nguyên filter hiện tại.
2. Chỉ thay page theo trang user chọn.
3. Gọi lại API search.

## 6. Danh sách mã trạng thái bàn cho filter Trạng thái

- AVAILABLE: Bàn trống
- OCCUPIED: Đang sử dụng
- BOOKED: Đã đặt

## 7. Lỗi thường gặp và cách xử lý FE

- 401 Unauthorized: thiếu hoặc hết hạn JWT.
- 400 Bad Request: sai dữ liệu validate (ví dụ floor < 1, slot < 1).
- 500 Internal Server Error: lỗi hệ thống, hiển thị thông báo chung và cho phép retry.

## 8. Curl mẫu

```bash
curl --location 'http://localhost:8080/api/v1/table/search' \
--header 'Authorization: Bearer <access_token>' \
--header 'Content-Type: application/json' \
--data '{
  "page": 0,
  "limit": 12,
  "sortField": "lastBookingTime",
  "sortDir": "DESC",
  "keyword": "ban",
  "slot": 4,
  "tableStatus": "AVAILABLE",
  "floor": 1,
  "active": true
}'
```
