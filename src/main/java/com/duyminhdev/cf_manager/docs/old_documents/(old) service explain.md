# Báo Cáo Phân Tích Thiết Kế Lớp Service (Nghiệp Vụ) - Màn Hình TableList

Dựa trên báo cáo phân tích API trước đó (16 API endpoints cốt lõi), dưới đây là bản thiết kế chi tiết danh sách các **Service Methods** (Business Logic Layer) tương ứng cần thực thi trong hệ thống backend (vd: Spring Boot). Phân tích bao gồm Input, Output, và Flow logic cụ thể của từng nghiệp vụ.

---

## 1. `TableService` - Dịch vụ Quản lý Bàn

### 1.1. Lấy danh sách trạng thái tổng quan bàn
- **Method:** `getAllTableListWithStatus()`
- **Input:** Khởi tạo không tham số (có thể thêm `String keyword`, `Long floorId` để lọc).
- **Output:** `List<TableListDTO>`
  - _Cấu trúc DTO:_ `tableId`, `tableName`, `tableCode`, `status` (`AVAILABLE`, `OCCUPIED`, `BOOKED`), `totalBookingToday`, `latestBookingTime`.
- **Flow nghiệp vụ:**
  1. Truy vấn `findAll` từ bảng `Table`.
  2. Lấy dữ liệu từ bảng `TableBooking` của ngày hôm nay để đếm `totalBookingToday` cho từng bàn.
  3. Kiểm tra trạng thái thời gian thực: Nếu bàn có người ngồi (`OCCUPIED`), ưu tiên trạng thái này. Nếu trống nhưng có booking sắp tới trong vòng 2 giờ, đánh dấu `BOOKED`.

### 1.2. Xem thông tin chi tiết một bàn
- **Method:** `getTableDetailById(Long tableId)`
- **Input:** `Long tableId`
- **Output:** `TableDetailDTO` (thông tin định danh bàn).
- **Flow nghiệp vụ:** Tìm trong DB. Nếu không có `throw ResourceNotFoundException`.

---

## 2. `TableBookingService` - Dịch vụ Đặt Bàn

### 2.1. Lịch sử đặt bàn theo bàn (Phân trang)
- **Method:** `getBookingHistoryByTable(DataTableReqDTO req, Long tableId)`
- **Input:** `Long tableId`, thông tin phân trang `page, size`, và thông tin bộ lọc.
- **Output:** `Page<TableBookingResDTO>` (Kèm `customerName`, `bookingTime`, `deposit`, `status`, tên nhân viên).
- **Flow nghiệp vụ:** 
  1. Build query truy vấn `TableBooking` theo `tableId`.
  2. JOIN với `Account` để lấy tên user lập phiếu.
  3. Trả về kết quả phân trang.

### 2.2. Tạo mới hoặc Cập nhật Lịch Đặt Bàn
- **Method:** `saveOrUpdateBooking(TableBookingReqDTO request)`
- **Input:** `TableBookingReqDTO` (`id`, `tableId`, `accountId`, `bookingTime`, `customerName`, `phoneNumber`, `deposit`, `status`).
- **Output:** `TableBookingResDTO` (hoặc `Boolean` báo thành công).
- **Flow nghiệp vụ:**
  1. Kiểm tra validation: `tableId` có tồn tại không, `bookingTime` có hợp lệ không (vd: không được trong quá khứ).
  2. **Trường hợp Tạo mới (`id = 0`):** Khởi tạo Entity, gán thông tin khách, tiền cọc. Save vào DB.
  3. **Trường hợp Cập nhật (`id > 0`):** Tìm kiếm booking cũ, cập nhật các trường được phép thay đổi.
  4. Trả về kết quả.

---

## 3. `DishOrderService` - Dịch vụ Đặt Món

### 3.1. Danh sách đơn Order theo Bàn
- **Method:** `getOrderHistoryByTableId(Long tableId)`
- **Input:** `Long tableId`
- **Output:** `List<DishOrderHistoryDTO>`
  - _Cấu trúc:_ Gồm mã order, thời gian, tên nhân viên, trạng thái order (`PROCESSING`, `COMPLETED`, `CANCELLED`).
- **Flow nghiệp vụ:** Query bảng `DishOrder` với `tableId` đang truyền vào. Đặc biệt ưu tiên lấy các Order đang chưa thanh toán.

### 3.2. Tạo mới / Cập nhật Đơn Gọi Món (Order)
- **Method:** `createOrUpdateOrder(DishOrderReqDTO req)`
- **Input:** `DishOrderReqDTO` 
  - Gồm: `id`, `tableId`, `accountId`, `statusId`, danh sách chi tiết: `List<DishOrderDetailReqDTO>`.
- **Output:** `Long` (Trả về `orderId`).
- **Flow nghiệp vụ:**
  1. **Nếu `id == 0` (Tạo order mới):**
     - Lưu thông tin hóa đơn tạm `DishOrder` với giờ hiện tại.
     - Cập nhật thông tin trạng thái bảng `Table` sang `OCCUPIED`.
  2. Xử lý danh sách chi tiết món (`DishOrderDetail`):
     - Lặp qua mảng `DishOrderDetails` gửi từ Client. 
     - Mapping giá bán ngay tại thời điểm gọi dựa theo Master `Dish`. 
     - Insert hoặc Update toàn bộ chi tiết lưu vào DB.
  3. Bắn event websocket nếu có tích hợp màn hình nhà bếp (Bếp/Pha chế).

### 3.3. Huỷ / Đổi Trạng Thái Đơn Bàn
- **Method:** `updateOrderStatus(Long orderId, String newStatus)`
- **Input:** `Long orderId`, `String newStatus` (Vd: "CANCELLED").
- **Output:** `Boolean`
- **Flow nghiệp vụ:** 
  1. Find `DishOrder` theo `id`. Đổi `status`.
  2. Hook kiểm tra: Nếu tất cả các Order của bàn `tableId` đều đã `CANCELLED` hoặc `COMPLETED`, revert bảng `Table` thành `AVAILABLE`. 

---

## 4. `DishOrderDetailService` - Dịch vụ Chi tiết Gọi món

### 4.1. Xem chi tiết từng món của một Order cụ thể
- **Method:** `getDetailsByOrderId(Long orderId)`
- **Input:** `Long orderId`
- **Output:** `List<DishOrderDetailResDTO>`
- **Flow nghiệp vụ:** Query `DishOrderDetail` JOIN `Dish` để lấy tên món, hình ảnh và giá cả trả về View.

### 4.2. **Gom Nhóm** Món ăn theo bàn phục vụ Thanh Toán
- **Method:** `getAggregatedDishesByTableId(Long tableId)`
- **Input:** `Long tableId`
- **Output:** `List<AggregatedDishDTO>` (`dishName`, `quantity`, `unitPrice`, `totalPrice`).
- **Flow nghiệp vụ (RẤT QUAN TRỌNG):** 
  1. Lấy TẤT CẢ `DishOrder` có trạng thái chưa thanh toán của bàn `tableId`.
  2. Truy vấn danh sách chi tiết `DishOrderDetail` của các order liên quan.
  3. Dùng stream API hoặc GROUP BY (trong SQL) để nhóm các bản ghi cùng `dishId`. Tính SUM(`quantity`) và SUM(`quantity * price`).
  4. Trả kết quả cuối lên màn hình checkout.

---

## 5. `DishService` & Khác - Dịch vụ Danh mục / Menu

### 5.1. Lấy Menu món ăn & Phân loại
- **Method 1:** `DishService.getAllDishes()` / `searchDishes(keyword)`
- **Method 2:** `DishCategoryService.getAllCategories()`
- **Flow nghiệp vụ:** Chỉ thực hiện `Select *` các món và danh mục có trạng thái Active. Hỗ trợ autocomplete (Tên, Mã món).

### 5.2. Lấy phương thức thanh toán
- **Method:** `PaymentMethodService.getAllMethods()`
- **Flow nghiệp vụ:** Lấy ra danh sách các cổng/thanh toán (Cash, Transfer, Card, VNPay...).

---

## 6. `InvoiceService` - Dịch vụ Hóa Đơn và Thanh Toán

### 6.1. Sinh mã Hóa đơn
- **Method:** `getInvoiceCount()` / `generateNextInvoiceCode()`
- **Output:** `String` (VD: `HD-20260329-0012`)
- **Flow nghiệp vụ:** Đếm tổng số invoice hôm nay trong DB, dùng thuật toán padZero sinh ra chuỗi định dạng hóa đơn duy nhất.

### 6.2. Tạo và Thanh toán Hóa đơn (Chốt Bill)
- **Method:** `createOrUpdateInvoice(InvoiceCheckoutReqDTO req)`
- **Input:** `id`, `accountId`, `tableId`, `paymentMethod`, `totalMoney`, `paymentStatus` (Pending/Paid), Danh sách `InvoiceDetailReqDTO`.
- **Output:** `InvoiceResDTO` (bao gồm `invoiceId` và `vnPayUrl` - nếu có).
- **Flow nghiệp vụ:**
  1. Ràng buộc: Kiểm tra tổng tiền xem có khớp với các `DishOrder` của bàn hay không.
  2. Tạo bản ghi `Invoice` (Kèm discount/Voucher nếu có).
  3. Lặp và lưu thông tin vào `InvoiceDetail`.
  4. **Xử lý trạng thái (`paymentStatus`):**
     - Nếu "Chờ thanh toán" (Chuyển khoản VNPay): Sinh URL chuyển hướng.
     - Nếu "Đã thanh toán" (Tiền mặt/Hoàn tất CK): 
        a. Cập nhật `paymentStatus = DONE`.
        b. Đánh dấu TẤT CẢ các `DishOrder` chưa thanh toán của `tableId` thành `COMPLETED` (hoặc `PAID`).
        c. Đổi trạng thái bảng `Table` -> `AVAILABLE` (Làm trống bàn).

### 6.3. Chi tiết biên lai thanh toán (In hóa đơn/ QR VietQR)
- **Method:** `getInvoiceDetailFull(Long invoiceId)`
- **Input:** `Long invoiceId`
- **Output:** `InvoiceFullDTO` (Toàn bộ dữ liệu Header, List Items, Thu ngân, Table info).
- **Flow nghiệp vụ:** 
  1. JOIN 4 bảng (`Invoice`, `InvoiceDetail`, `Account`, `Table`).
  2. Trả về cấu trúc JSON phẳng phục vụ render HTML Biên lai (SweetAlert) hoặc fetch tạo chuỗi nạp vào hàm tạo mã QRCode VietQR.
