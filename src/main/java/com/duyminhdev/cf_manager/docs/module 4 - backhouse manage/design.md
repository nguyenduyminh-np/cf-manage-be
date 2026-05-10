Dưới đây là thiết kế chi tiết business flow cho chức năng **Quản lý đơn nhập hàng (Purchase Order)** theo yêu cầu mới: **tách riêng Create và Update**, đồng thời **chỉ rõ các field thao tác CRUD** trên từng entity trong từng bước nghiệp vụ.

---

## 1. Entity tham gia và quan hệ

### 1.1. Entity chính
- **`PurchaseOrder`** (PO): Đơn nhập hàng, trung tâm của nghiệp vụ.

### 1.2. Entity phụ (liên quan trực tiếp)
- **`PurchaseOrderDetail`**: Chi tiết từng mặt hàng trong PO (FK tới `PurchaseOrder` và `Ingredient`).
- **`Ingredient`**: Nguyên liệu được đặt mua (để lấy thông tin hạn dùng, giá...).
- **`StockTransaction`**: Phiếu nhập kho tự động sinh khi PO hoàn thành.
- **`StockTransactionDetail`**: Chi tiết nhập kho, ánh xạ tới lô (`StockLevel`).
- **`StockLevel`**: Lô tồn kho được tạo/cập nhật khi nhập hàng.
- **`Debt`**: Công nợ nhà cung cấp.
- **`CashFlow`**: Dòng tiền (ghi nhận nợ).
- **`AccountActivity`**: Nhật ký thao tác người dùng.

### 1.3. Entity tham chiếu (Read-only)
- **`Supplier`**: Nhà cung cấp (truyền ID khi hoàn thành PO).
- **`Warehouse`**: Kho nhận hàng (truyền ID).
- **`Account`**: Người dùng thao tác.

### 1.4. Sơ đồ quan hệ
```
PurchaseOrder 1 ──< * PurchaseOrderDetail * ── 1 Ingredient
PurchaseOrder * ── 1 Account
PurchaseOrder (1) ── (1) StockTransaction   (sinh tự động khi Complete)
StockTransaction 1 ──< * StockTransactionDetail * ── 1 StockLevel
PurchaseOrder (1) ── (1) Debt (liên kết qua note, nên có FK cứng)
PurchaseOrder (1) ── (1) CashFlow (DEBT)
```

---

## 2. Business Flow tổng quát

```mermaid
graph TD
    A[Tạo PO mới<br/>(draft/pending/approved)] -->|Admin tạo| B[Approved]
    A -->|Nhân viên tạo| C[Pending]
    C -->|Duyệt| B
    A -->|Lưu nháp| D[Draft]
    D -->|Gửi duyệt| C
    B -->|Hoàn thành| E[Completed]
    B -->|Hủy| F[Cancelled]
    C -->|Hủy| F
    E -->[Tự động nhập kho, tạo nợ, cashflow]
```

**Chỉ thao tác sửa/xóa khi PO ở trạng thái `Draft`, `Pending` hoặc `Approved` (chưa Completed, Cancelled).**

---

## 3. Thiết kế chi tiết các Service

### 3.1. `CreatePurchaseOrder(PurchaseOrderCreateDto dto)`

**Chức năng:** Tạo mới đơn nhập hàng cùng danh sách chi tiết.

**Entity & Field thao tác:**

| Entity | CRUD | Field(s) | Mô tả |
|--------|------|----------|-------|
| **PurchaseOrder** | **C** | `PurchaseOrderCode` | Sinh tự động `PO-XXXXXX` (tăng dần) |
| | | `TotalPrice` | Tổng từ DTO (tính từ detail) |
| | | `PaymentStatus` | `DRAFT` / `PENDING` / `APPROVED` (theo vai trò người tạo) |
| | | `CreatedTime` | `DateTime.Now` |
| | | `Active` | `true` |
| | | `AccountId` | ID người tạo |
| | | `OrderDate` | Ngày dự kiến nhận (từ DTO) |
| **PurchaseOrderDetail** | **C** (nhiều dòng) | `PurchaseOrderId` | ID của PO vừa tạo |
| | | `IngredientId` | Nguyên liệu được chọn |
| | | `Quantity` | Số lượng |
| | | `UnitPrice` | Đơn giá nhập |
| | | `Active` | `true` |
| | | `CreatedTime` | `DateTime.Now` |
| **AccountActivity** | **C** | `ActivityCode` | ID của PO (chuỗi) |
| | | `AccountId` | Người tạo |
| | | `ActivityType` | `"purchase_order"` |
| | | `ActivityDescription` | `"Tạo đơn nhập hàng"` |
| | | `CreatedTime` | `DateTime.Now` |
| | | `Active` | `true` |

**Quy tắc:**
- Nếu `RoleId == ADMIN`, tự động set `PaymentStatus = "APPROVED"`, ngược lại `"PENDING"`.
- Nếu người dùng chọn lưu nháp, set `"DRAFT"`.

---

### 3.2. `UpdatePurchaseOrder(PurchaseOrderUpdateDto dto)`

**Chức năng:** Cập nhật thông tin PO (tiêu đề, ngày nhận, tổng tiền) và đồng bộ danh sách chi tiết (thêm/sửa/xóa mềm).

**Điều kiện:** PO chưa `Completed` hoặc `Cancelled`.

**Entity & Field thao tác:**

| Entity | CRUD | Field(s) | Mô tả |
|--------|------|----------|-------|
| **PurchaseOrder** | **R** | `Id`, `PaymentStatus`, `Active` | Đọc để kiểm tra trạng thái |
| **PurchaseOrder** | **U** | `PurchaseOrderCode` | Cập nhật nếu có thay đổi (thường không đổi) |
| | | `TotalPrice` | Tổng lại từ detail |
| | | `OrderDate` | Ngày dự kiến nhận |
| | | `PaymentStatus` | Có thể đổi (ví dụ DRAFT -> PENDING) |
| **PurchaseOrderDetail** | **R** | Toàn bộ | So sánh với danh sách mới |
| | **C** (detail mới) | `PurchaseOrderId`, `IngredientId`, `Quantity`, `UnitPrice`, `Active`, `CreatedTime` | Thêm dòng mới |
| | **U** (detail cũ) | `Quantity`, `UnitPrice` | Cập nhật số lượng/đơn giá |
| | **D (soft)** (detail bị xóa) | `Active` = `false` | Xóa mềm |
| **AccountActivity** | **C** | `ActivityCode`, `AccountId`, `ActivityType`, `ActivityDescription`, `CreatedTime`, `Active` | `"Cập nhật đơn nhập hàng"` |

**Lưu ý:** Không thay đổi `AccountId` gốc của PO.

---

### 3.3. `UpdateStatus(PurchaseOrderStatusDto dto)` – Duyệt / Hoàn thành / Hủy

Đây là service quan trọng nhất, xử lý chuyển trạng thái và các tác động phụ.

#### 3.3.1. Chuyển sang `APPROVED` hoặc `PENDING` (duyệt / gửi duyệt)

| Entity | CRUD | Field(s) |
|--------|------|----------|
| **PurchaseOrder** | **U** | `PaymentStatus` |
| **AccountActivity** | **C** | `ActivityDescription = "Đơn hàng được gửi"` hoặc `"Đơn nhập hàng đã được duyệt"` |

#### 3.3.2. Chuyển sang `CANCELLED`

| Entity | CRUD | Field(s) |
|--------|------|----------|
| **PurchaseOrder** | **U** | `PaymentStatus` |
| **AccountActivity** | **C** | `ActivityDescription = "Đơn nhập hàng đã bị hủy"` |

#### 3.3.3. Chuyển sang `COMPLETED` (Hoàn thành) – **Tự động hóa hàng loạt**

Khi hoàn thành PO, trong **một transaction**, thực hiện:

| Bước | Entity | CRUD | Field(s) chi tiết |
|------|--------|------|-------------------|
| 1 | **PurchaseOrder** | **R** | `Id`, `TotalPrice`, `PurchaseOrderCode`, `AccountId` |
| 1 | **PurchaseOrder** | **U** | `PaymentStatus = "COMPLETED"` |
| 2 | **PurchaseOrderDetail** | **R** | Lấy tất cả dòng có `Active = true` (đọc `IngredientId`, `Quantity`, `UnitPrice`) |
| 3 | **Ingredient** | **R** | Với mỗi dòng, đọc `SelfLife`, `AveragePrice` |
| 3 | **Ingredient** | **U** | `AveragePrice` = (giá cũ + giá mới)/2 (logic hiện tại) |
| 4 | **StockTransaction** | **C** | `StockTransactionCode = "TX-IN-AUTO-" + PO.Code` |
| | | | `TransactionType = "IMPORT"` |
| | | | `Status = "COMPLETED"` |
| | | | `TotalMoney = PO.TotalPrice` |
| | | | `WarehouseId = dto.WarehouseId` |
| | | | `AccountId = dto.AccountId` |
| | | | `TransactionDate = DateTime.Now` |
| | | | `Note = "Hệ thống nhập kho tự động cho đơn nhập hàng ..."` |
| | | | `Active = true`, `CreatedTime = Now` |
| 5 | **StockLevel** | **C** (mỗi dòng) | `IngredientId`, `WarehouseId`, `Quantity`, `UnitPrice`, `ExpirationDate = Now + ingredient.SelfLife`, `Active = true`, `CreatedTime/LastUpdatedTime = Now` |
| 6 | **StockTransactionDetail** | **C** | `StockTransactionId`, `StockLevelId` (vừa tạo), `Quantity`, `Active = true`, `CreatedTime = Now` |
| 7 | **Debt** | **C** | `DebtCode = "DEBT_" + PO.Code` |
| | | | `DebtName = "Nợ nhập hàng " + PO.Code + " - " + DateTime.Now` |
| | | | `TotalMoney = PO.TotalPrice` |
| | | | `IsPaid = false`, `PaidAt = null` |
| | | | `Note = "Nợ nhập hàng " + DateTime.Now` |
| | | | `CreatedTime = Now`, `Active = true` |
| | | | `SupplierId = dto.SupplierId` |
| 8 | **CashFlow** | **C** | `TotalMoney = PO.TotalPrice` |
| | | | `FlowType = "DEBT"` |
| | | | `Note = "Nợ nhập hàng " + PO.Code + " - " + DateTime.Now` |
| | | | `AccountId = dto.AccountId` |
| | | | `CreatedTime = Now`, `Active = true` |
| 9 | **AccountActivity** | **C** | `ActivityDescription = "Đơn nhập hàng đã hoàn thành"` |

**Lưu ý:** `dto.WarehouseId` và `dto.SupplierId` là bắt buộc khi hoàn thành.

---

## 4. Mô tả quan hệ giữa Entity chính (PurchaseOrder) và Entity phụ

- **PurchaseOrder – PurchaseOrderDetail**: Một PO chứa nhiều dòng chi tiết; mỗi dòng chỉ thuộc về một PO. Chi tiết được quản lý vòng đời cùng PO: thêm khi tạo, cập nhật/xóa khi sửa PO. Chỉ những dòng `Active = true` mới có hiệu lực.
- **PurchaseOrder – Debt**: Mỗi PO khi hoàn thành sinh ra **một** khoản nợ tương ứng với tổng tiền và một nhà cung cấp (giả định toàn bộ PO từ một supplier). Quan hệ ngầm qua `DebtCode` và `Note` tham chiếu PO Code; nên tạo FK `PurchaseOrderId` trong tương lai.
- **PurchaseOrder – StockTransaction**: Khi hoàn thành PO, hệ thống tạo ra **một** phiếu nhập kho (`IMPORT`) đại diện cho toàn bộ việc nhập hàng. Mã phiếu chứa `TX-IN-AUTO-PO-...` để truy xuất nguồn gốc.
- **PurchaseOrder – CashFlow**: Sinh một bút toán dòng tiền loại `DEBT` để ghi nhận nợ phải trả.
- **PurchaseOrder – AccountActivity**: Ghi nhận mọi hành động thay đổi PO (tạo, sửa, duyệt, hoàn thành, hủy) với `ActivityCode = PO.Id`.
- **PurchaseOrder – Supplier/Warehouse**: Không có FK trực tiếp; thông tin được cung cấp khi hoàn thành để xác định nơi nhập và ai là chủ nợ.

---

## 5. Tổng kết

Thiết kế mới tách biệt rõ ràng `Create` và `Update` cho PO, đảm bảo mỗi service có trách nhiệm duy nhất (Single Responsibility). Các field CRUD được chỉ rõ theo từng bước, giúp lập trình viên dễ dàng hiện thực và kiểm thử. Việc kiểm soát trạng thái PO khi cập nhật cũng như quá trình tự động hóa khi hoàn thành PO được giữ nguyên từ phiên bản trước, đảm bảo toàn vẹn dữ liệu xuyên suốt các entity liên quan.