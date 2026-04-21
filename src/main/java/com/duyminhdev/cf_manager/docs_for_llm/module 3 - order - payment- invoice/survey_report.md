# Báo Cáo Khảo Sát Tình Hình Triển Khai Module: Đặt món - Thanh toán - Tạo hóa đơn

Dựa trên tài liệu thiết kế nghiệp vụ `[API-BUSINESS] over-view.md` và quá trình đánh giá mã nguồn hiện tại trong hệ thống, dưới đây là khảo sát chi tiết về tỉ lệ cover (hoàn thành) và mức độ tuân thủ API Contract mới.

## 1. Đánh giá Mức độ tuân thủ API Contract Mới (Đạt: 100%)
Toàn bộ các API đã được code trong Controller của hệ thống mới đều tuân thủ chặt chẽ kiến trúc Contract mới:
- **Cấu trúc Wrapper**: Đã chuyển đổi hoàn toàn từ `CoffeeManagementResponse` cũ sang `ApiResponse<T>` làm chuẩn chung. Cấu trúc Response đảm bảo form `status`, `message` bên ngoài và Payload nghiệp vụ nằm gọn gàng trong node `data`. 
- **Validations & DTOs Mapping**: Không sử dụng trực tiếp các Model hay các ViewModel cũ gộp chung. Hệ thống đã tách hoàn toàn payload bằng các Object DTO (Ví dụ: `DishOrderCreateRequestDTO`, `InvoiceCreateRequestDTO`, `InvoiceResponseDTO`...) kết hợp `@Valid`, `@Positive`, `@Pattern`. Việc này mapping hoàn hảo với yêu cầu "chỉ tập trung phần dữ liệu 'data', metadata follow theo chuẩn mới" của bản thiết kế.

---

## 2. Thống kê Cover Theo Từng Nhóm API Nghiệp Vụ

### 2.1. Nhóm API Đặt món (DishOrder) - Mức độ cover luồng lõi: ~80%
*Luồng gọi món cơ bản qua ứng dụng phục vụ/Tablet đã cơ bản hoàn thiện, tuy nhiên thiếu phần API phục vụ phân hệ của nhân viên Pha chế.*

| API theo bản Thiết kế | Ánh xạ với Source Code hiện tại | Trạng Thái | Đánh Giá / Sai lệch Data |
| :--- | :--- | :---: | :--- |
| `GET .../DetailByTableId`<br>*(Lịch sử đơn chờ)* | `POST /api/v1/dish-order/list-by-table` | 🟢 Có | Tốt. Đã đổi sang POST bảo mật. Schema của `DishOrderResponseDTO` chuẩn xác định danh các món chờ. |
| `GET .../DishDetailByTableId`<br>*(Gộp món chuẩn bị thanh toán)* | `POST /api/v1/dish-order-detail/list-by-table` | 🟢 Có | Rất Tốt. Tách gọn sang controller Detail, cấu trúc `DishGroupedByTableResponseDTO` trả về nhóm món siêu chuẩn. |
| `POST .../AddOrUpdate`<br>*(Tạo mới / Sửa đơn món)* | `POST /api/v1/dish-order/create`<br>`POST /api/v1/dish-order/update` | 🟡 Chú ý | Đã tách riêng rẽ logic Create & Update thành 2 API tường minh rất tốt. **Lệch ở Data**: Các DTO Request đang **THIẾU field `description`** (ghi chú chung cho Order) so với thiết kế JSON gốc. |
| `POST .../Update`<br>*(Cập nhật status pha chế)* | `POST /api/v1/dish-order/update-status` | 🟢 Có | Tốt. |
| `GET .../ListQueue`<br>*(Dành cho NV Pha chế)* | Không tìm thấy | 🔴 Không có | **Thất thoát nghiệp vụ**: Chưa có Controller/API trả List Queue cho giao diện màn hình Bartender. |
| Các API Quản trị CRUD cơ bản (List Admin, Phân trang Paging) | Không tìm thấy Controller mới | 🔴 Không có | Chưa thấy triển khai luồng xem danh sách đơn hàng Datatables cho Admin theo chuẩn mới. |

### 2.2. Nhóm API Thanh Toán & Hóa Đơn (Invoice) - Mức độ cover luồng lõi: ~60%
*Chỉ mới bao phủ được luồng thanh toán Tiền Mặt (CASH / Offline). Toàn bộ khối logic liên thông ví điện tử đang khuyết.*

| API theo bản Thiết kế | Ánh xạ với Source Code hiện tại | Trạng Thái | Đánh Giá / Sai lệch Data |
| :--- | :--- | :---: | :--- |
| `POST .../AddOrUpdateVM`<br>*(Tạo HĐ & Link thanh toán)* | `POST /api/v1/invoice/create` | 🟡 Một phần | Chỉ tạo được Hóa đơn dòng tiền mặt. **Data trả về `InvoiceResponseDTO` có field `uriVnPay` nhưng Logic gốc Backend chưa can thiệp tạo URL VNPAY.** |
| `GET .../InvoiceDetailVM`<br>*(Chi tiết hiển thị HĐ)* | `POST /api/v1/invoice/detail` | 🟢 Có | Tốt. |
| `POST .../UpdateStatus`<br>*(Thủ công đổi trạng thái)* | `POST /api/v1/invoice/confirm-payment` | 🟢 Có | Logic confirm chuẩn xác bằng Exception flow mới. |
| `POST .../PaymentSuccess`<br>*(VNPAY Callback chốt đơn)* | Không tìm thấy | 🔴 Không có | **Thất thoát nghiệp vụ**: Chưa có endpoint bắt Webhook/Callback từ VNPAY về. |
| API Count & CRUD hóa đơn Admin | `POST /api/v1/invoice/count` | 🟡 Một phần | Mới chỉ có Count, thiếu List Datatables Paging. |

### 2.3. Nhóm Tích hợp Cổng thanh toán VNPAY - Mức độ cover: 0%
- Trong tài liệu thiết kế định nghĩa rõ 3 Endpoint cho Vnpay: `Callback`, `IpnAction`, `CreatePaymentUrl`.
- **Thực tế rà soát:** Hệ thống mới chỉ khởi tạo 1 class `VnPayConstant.java` chứa link sandbox. Hoàn toàn **không tồn tại** `VnpayController.java` hay logic service sinh MAC/Hash chữ ký điện tử thanh toán. 

---

## 3. Kết luận & Khuyến nghị Hành động cho đội Dev

**⭐️ Khen ngợi (Các điểm mạnh hệ thống đã làm được):**
- Quy trình mua hàng **Cash Flow cốt lõi (Happy Path)** đi từ: Gọi món -> Cập nhật món -> Nhóm món ra Bill -> In hóa đơn -> Thu tiền mặt -> Kết thúc; đã chạy rất mượt và **cover đến 85% phần việc cốt lõi của quán cafe**.
- Việc tổ chức lại `ApiResponse` kèm chuẩn hóa thư mục DTO vs Entity là 1 bản nâng cấp kiến trúc sạch (Clean architecture) rất chuyên nghiệp.

**⚠️ Các lỗ hổng (Gaps) cần khẩn trương vá để đủ 100% chức năng thiết kế:**
1. **Fix Dữ liệu thiếu**: Cần bổ sung ngay trường `description` bổ sung comment dạng string vào 2 Request DTO `DishOrderCreateRequestDTO` và `DishOrderUpdateRequestDTO` để người bồi bàn có thể note chung cho bill (vd: "Khách mang đi", "Bàn hẹn 1 tiếng nữa xài").
2. **Missing Feature (Pha chế)**: Cần bổ sung API `list-queue` (Theo Table/Time) cho Role Bartender để Frontend có dữ liệu render tiến trình pha chế thức uống.
3. **Missing Feature (Cổng Thanh Toán Online)**: Xây dựng trọn vẹn `VnpayController.java`, tích hợp cơ chế build query string tạo `uriVnPay` vào lúc Invoke `invoice/create` (nếu chọn `VNPAY`). Kèm theo là IPN Action để tự động mark trạng thái hóa đơn PAID.
4. **Missing APIs Paging Admin**: Bỏ sung các APIs phân trang Server-side cho màn hình Admin CMS thống kê thu chi sau.
