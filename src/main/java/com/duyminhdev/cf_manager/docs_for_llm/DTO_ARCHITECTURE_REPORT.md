# Technical Document: DTO Architecture & Responsibility Separation
> **Objective**: Analyze the DTO (Data Transfer Object) architecture, design rules, and segregation of duties within the project to ensure structural consistency for LLM parsing and ongoing development.

---

## 1. DTO Directory Architecture
Cấu trúc package `dto` được phân chia cực kỳ nghiêm ngặt theo **Domain Context** và **Responsibility**. Việc này ngăn chặn sự xuất hiện của các "God DTOs" (những class phình to, dùng chung cho cả Input/Output hoặc Database/Excel).

```text
com.duyminhdev.dynamic_query.dto
├── base/          # Common HTTP Payload Wrappers (ApiResponse, PageResponse)
├── crud/          # Payload cho các nghiệp vụ chuẩn C.R.U.D
│   ├── request/   # Input (Create, Update, Delete, Criteria)
│   └── response/  # Output (Detail View, Options dropdown)
├── db_result/     # Model mapping trực tiếp từ Raw Database Queries (Không phải màn hình)
│   ├── native_sql/  # Map các trường trả về (Tuple) từ NativeQuery
│   └── procedure/   # Map kết quả (JSON/String) từ Oracle Procedures
├── excel/         # DTO chuyên biệt cho File I/O (Import/Export/Template)
├── filter/        # Các bộ lọc không phân trang (VD: Export Excel Filter)
└── approval/      # Action DTOs cho các nghiệp vụ đặc thù (VD: Duyệt Para Status)
```

---

## 2. Segregation of Duties (Phân tách Trách nhiệm)

### 2.1. Request vs. Response Boundaries (Input vs Output)
DTO vốn dĩ sinh ra để cô lập Entity nội bộ khỏi API Contract bên ngoài. Tại project này, quy tắc được đẩy mạnh hơn: Input và Output tuyệt đối **không dùng chung class**.

- **Request DTOs** (`ParaPaymentChannelCreateDTO`, `ParaPaymentChannelUpdateDTO`): 
  - Tập trung chủ yếu vào **Validation an ninh** (`@NotBlank`, `@Pattern`, `@Min`, `@Max`).
  - **Sự phân tách**: `UpdateDTO` bắt buộc phải có `id` (`@NotNull`), trong khi `CreateDTO` tuyệt đối không được phép nhận `id` từ client (tránh tấn công ID Injection). Dùng chung DTO sẽ phá vỡ ràng buộc này.
- **Response DTOs** (`PaymentChannelResponseDTO`): 
  - Hoàn toàn **không chứa validation**.
  - Nó chỉ phục vụ định dạng dữ liệu trả về cho Frontend (VD: ẩn các cột nhạy cảm, transform format ngày tháng, map enum thành chuỗi dễ đọc).

```java
// Snippet: Ràng buộc an ninh dữ liệu tại Create DTO (Chặn đứng bad request ở rìa Controller)
@NotBlank(message = "Xin mời nhập tên kênh thanh toán")
@Size(max = 200, message = "Tên kênh thanh toán quá dài")
private String paymentChannel;

@Pattern(regexp = ValidateValueConstants.CONNECTION_NAME) // Regex tập trung
private String connectionName;
```

### 2.2. Standard API vs. File I/O (Excel DTOs)
Một chuỗi JSON HTTP request có tính chất hoàn toàn khác với việc đọc/ghi một hàng (row) trong file `.xlsx`. Khai báo chung DTO sẽ làm lộn xộn các field và metadata annotation.

- **`ParaPaymentChannelExcelImportDTO`**: Tất cả các thuộc tính thường được đọc dưới dạng `String` (kể cả số/enum) để hệ thống tự parse và bắt lỗi định dạng (Throw Exception vào file Error) thay vì crash Spring Boot.
- **`RowError`**: Một DTO sinh ra chỉ để đánh cờ dòng bị lỗi (`rowIndex`) trả về cho người dùng.

### 2.3. Data-Access Level DTOs (`db_result`)
Lớp này tách biệt hoàn toàn khỏi `ResponseDTO` của Frontend. 

- **Lý do**: Khi chạy Native/Procedure, truy vấn thường lấy thêm các cột phát sinh không nằm trong JPA Entity (VD: `currencyName` từ phép join bảng `PARA_CURRENCY_RATE`). Entity không chứa cột này, nên framework cần một `NativePaymentChannelResult` trung gian để hứng đúng Data Type từ Tuple/JSON.

```java
// Snippet: Sự tiến hóa DTO qua các tầng (Layered Transformation)

// Tầng Data-Access -> Tầng Result DTO trung gian
NativePaymentChannelResult resultDto = new NativePaymentChannelResult();
resultDto.setCurrencyName((String) tuple.get("currencyName")); // Map tên db alias

// Tầng Result DTO trung gian -> Tầng API (Thông qua Mapper)
PaymentChannelDataTableDTO apiResponseDto = mapper.toDataTableDto(resultDto); 
```

---

## 3. Recommended Prompts/Rules cho AI/Developer khi phát triển

Khi được yêu cầu xây dựng API/Module mới, LLM (hoặc Developer) bắt buộc tuân theo bộ quy chuẩn sau:

1. **Tuyệt đối không tái sử dụng DTO khác Context**: Nếu làm tính năng "Duyệt trạng thái qua file Excel", quy tắc đòi hỏi sinh ra DTO mới (VD: `ExcelApprovalDTO`), không được phép thêm config gượng ép vào `ParaStatusApprovalRequestDTO` sẵn có.
2. **Push Validation To The Edge (Thẩm định dữ liệu tại rìa)**: Đặt toàn bộ ràng buộc `jakarta.validation` tại tầng `crud/request`. Không được đưa các code kiểm tra độ dài chuỗi hay check `null` vào trong `ServiceImpl`. Sử dụng bộ config chuẩn `ValidateValueConstants`.
3. **Sử dụng Base Wrapper Constraints**: Tất cả các Endpoints trong Controller bắt buộc gói data DTO trả về vào Generic Container là `ApiResponse<T>` hoặc `PageResponse<T>`. Không trả thẳng Object thô.
4. **Tránh Mapster Code (MapStruct)**: Không được phép viết các block code thủ công như `dto.setName(entity.getName())` trong Service. Hãy định nghĩa interface MapStruct (`ParaPaymentChannelMapper`) tương ứng.

> [!CAUTION] 
> Kiến trúc này đề cao nguyên tắc Single Responsibility Principle (SRP - Đơn trách nhiệm), đổi lại số lượng class sẽ tăng nhanh. Khi LLM khởi tạo tính năng, hệ thống yêu cầu LLM phải hình dung và tạo ra chùm file DTO đi theo cụm (Create/Update/Delete/Response) thay vì nhét chung mọi thứ lại.
