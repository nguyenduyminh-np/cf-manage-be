# INGREDIENT — API CHI TIẾT & DROPDOWN NGUỒN DỮ LIỆU

> **Mục đích:** Mô tả 4 API phục vụ màn hình **Thêm mới / Cập nhật nguyên liệu**: 1 API lấy chi tiết (pre-fill form), 3 API lấy danh sách dropdown (danh mục, đơn vị, nhà cung cấp).

> **Base URL:** `POST /api/v1/ingredient/...`  
> **Auth:** JWT — chỉ role `ADMIN` hoặc `MANAGER` (`@AdminOrManagerAccess`)

---

## MỤC LỤC

1. [Tổng Quan Luồng Nghiệp Vụ](#1-tổng-quan-luồng-nghiệp-vụ)
2. [API Contract — 4 Endpoint](#2-api-contract)
3. [Response DTO Reference](#3-response-dto-reference)
4. [Hướng Dẫn Tích Hợp FE](#4-hướng-dẫn-tích-hợp-fe)

---

## 1. TỔNG QUAN LUỒNG NGHIỆP VỤ

### Bối cảnh sử dụng

Khi người dùng mở form **Thêm mới** hoặc **Cập nhật** nguyên liệu, giao diện cần:

1. **3 dropdown** để chọn: Danh mục nguyên liệu, Đơn vị tính, Nhà cung cấp
2. **Dữ liệu chi tiết** nguyên liệu (khi update) để pre-fill toàn bộ form

### Luồng gọi API

```
[Mở form Thêm mới nguyên liệu]
  → Gọi song song 3 API dropdown:
      POST /ingredient/danh-sach-danh-muc
      POST /ingredient/danh-sach-don-vi
      POST /ingredient/danh-sach-nha-cung-cap
  → Render 3 <select> với dữ liệu trả về

[Mở form Cập nhật nguyên liệu]
  → Gọi song song 4 API (3 dropdown + 1 detail):
      POST /ingredient/danh-sach-danh-muc
      POST /ingredient/danh-sach-don-vi
      POST /ingredient/danh-sach-nha-cung-cap
      POST /ingredient/detail { "id": <ingredientId> }
  → Render 3 <select> + pre-fill form từ detail response
  → Dùng *Code trả về từ /detail để set giá trị selected cho dropdown
```

---

## 2. API CONTRACT

### Bảng Tổng Hợp

| # | Endpoint | Method | Body | Mô Tả |
|---|---|---|---|---|
| 1 | `/api/v1/ingredient/detail` | POST | `{ "id": Integer }` | Chi tiết nguyên liệu (JOIN lấy code + name từ 3 bảng liên kết) |
| 2 | `/api/v1/ingredient/danh-sach-danh-muc` | POST | Không cần body | Danh sách danh mục nguyên liệu (active) cho dropdown |
| 3 | `/api/v1/ingredient/danh-sach-don-vi` | POST | Không cần body | Danh sách đơn vị tính (active) cho dropdown |
| 4 | `/api/v1/ingredient/danh-sach-nha-cung-cap` | POST | Không cần body | Danh sách nhà cung cấp (active) cho dropdown |

---

### 2.1. `POST /api/v1/ingredient/detail`

**Chức năng:** Lấy toàn bộ thông tin chi tiết 1 nguyên liệu, bao gồm dữ liệu JOIN từ 3 bảng liên kết (`ingredient_category`, `supplier`, `unit`) và danh sách lô tồn kho còn hạn.

**Khi nào dùng:** Khi user click vào 1 nguyên liệu để xem chi tiết hoặc mở form cập nhật.

**Request — `IngredientIdRequest`:**
```json
{ "id": 5 }
```
| Field | Type | Required | Validation |
|---|---|---|---|
| `id` | Integer | ✅ | `@NotNull` — ID nguyên liệu |

**Response — `ApiResponse<IngredientDetailResponseDTO>`:**
```json
{
  "status": 200,
  "message": "GET_INGREDIENT_DETAIL_SUCCESS",
  "data": {
    "id": 5,
    "ingredientCode": "NL-005",
    "ingredientName": "Cà phê Robusta",
    "selfLife": 180,
    "averagePrice": 120000,
    "createdTime": "2026-03-15T10:30:00Z",
    "active": true,

    "ingredientCategoryId": 2,
    "ingredientCategoryCode": "DM-CF",
    "ingredientCategoryName": "Nguyên liệu cà phê",

    "supplierId": 3,
    "supplierCode": "NCC-003",
    "supplierName": "Công ty TNHH Cà phê Việt",

    "unitId": 1,
    "unitCode": "KG",
    "unitName": "Kilogram",

    "stockLevels": [
      {
        "id": 12,
        "warehouseName": "Kho chính",
        "quantity": 50,
        "expirationDate": "2026-09-15T00:00:00Z",
        "unitPrice": 115000
      }
    ]
  }
}
```

**Lưu ý quan trọng:**
- 6 field liên kết (`ingredientCategoryCode`, `ingredientCategoryName`, `supplierCode`, `supplierName`, `unitCode`, `unitName`) được lấy bằng **native SQL JOIN** — không phụ thuộc lazy loading.
- `stockLevels` chỉ chứa các lô **active = true** VÀ **chưa hết hạn** (`expirationDate > NOW`).
- Nếu ID không tồn tại → trả về lỗi `400 INVALID_DATA`, message: "Nguyên liệu không tồn tại".

**Backend implementation:**
- Service: `IngredientServiceImpl.buildDetail(id)` → dùng `NativeSqlIngredientRepository.findDetailById(id)` cho dữ liệu chính + `StockLevelRepository` cho lô tồn kho.
- Native SQL query JOIN 3 bảng: `ingredient ⟕ ingredient_category ⟕ supplier ⟕ unit`.

---

### 2.2. `POST /api/v1/ingredient/danh-sach-danh-muc`

**Chức năng:** Lấy toàn bộ danh mục nguyên liệu đang hoạt động (`active = true`), sắp xếp A→Z theo tên.

**Khi nào dùng:** Render dropdown "Danh mục nguyên liệu" trong form thêm/sửa.

**Request:** Không cần body (gửi `{}` hoặc không gửi body).

**Response — `ApiResponse<List<IngredientCategorySelectDTO>>`:**
```json
{
  "status": 200,
  "message": "GET_DANH_MUC_SUCCESS",
  "data": [
    {
      "ingredientCategoryCode": "DM-CF",
      "ingredientCategoryName": "Nguyên liệu cà phê"
    },
    {
      "ingredientCategoryCode": "DM-SUA",
      "ingredientCategoryName": "Nguyên liệu sữa"
    },
    {
      "ingredientCategoryCode": "DM-TRA",
      "ingredientCategoryName": "Nguyên liệu trà"
    }
  ]
}
```

| Response Field | Type | Mô Tả |
|---|---|---|
| `ingredientCategoryCode` | String | Mã danh mục (dùng làm value cho `<option>`) |
| `ingredientCategoryName` | String | Tên danh mục (hiển thị cho user) |

**Backend:** Dùng `IngredientCategoryRepository.findAllByActiveTrueOrderByIngredientCategoryNameAsc()`.

---

### 2.3. `POST /api/v1/ingredient/danh-sach-don-vi`

**Chức năng:** Lấy toàn bộ đơn vị tính đang hoạt động (`active = true`), sắp xếp A→Z theo tên.

**Khi nào dùng:** Render dropdown "Đơn vị tính" trong form thêm/sửa.

**Request:** Không cần body.

**Response — `ApiResponse<List<UnitSelectDTO>>`:**
```json
{
  "status": 200,
  "message": "GET_DON_VI_SUCCESS",
  "data": [
    { "unitCode": "G", "unitName": "Gram" },
    { "unitCode": "KG", "unitName": "Kilogram" },
    { "unitCode": "L", "unitName": "Lít" },
    { "unitCode": "ML", "unitName": "Mililit" }
  ]
}
```

| Response Field | Type | Mô Tả |
|---|---|---|
| `unitCode` | String | Mã đơn vị (dùng làm value cho `<option>`) |
| `unitName` | String | Tên đơn vị (hiển thị cho user) |

**Backend:** Dùng `UnitRepository.findAllByActiveTrueOrderByUnitNameAsc()`.

---

### 2.4. `POST /api/v1/ingredient/danh-sach-nha-cung-cap`

**Chức năng:** Lấy toàn bộ nhà cung cấp đang hoạt động (`active = true`), sắp xếp A→Z theo tên.

**Khi nào dùng:** Render dropdown "Nhà cung cấp" trong form thêm/sửa.

**Request:** Không cần body.

**Response — `ApiResponse<List<SupplierSelectDTO>>`:**
```json
{
  "status": 200,
  "message": "GET_NHA_CUNG_CAP_SUCCESS",
  "data": [
    {
      "supplierCode": "NCC-001",
      "supplierName": "Công ty ABC"
    },
    {
      "supplierCode": "NCC-003",
      "supplierName": "Công ty TNHH Cà phê Việt"
    }
  ]
}
```

| Response Field | Type | Mô Tả |
|---|---|---|
| `supplierCode` | String | Mã nhà cung cấp (dùng làm value cho `<option>`) |
| `supplierName` | String | Tên nhà cung cấp (hiển thị cho user) |

**Backend:** Dùng `SupplierRepository.findAllByActiveTrueOrderBySupplierNameAsc()`.

---

## 3. RESPONSE DTO REFERENCE

### 3.1. IngredientDetailResponseDTO (17 fields)

| Field | Type | Nullable | Nguồn dữ liệu |
|---|---|---|---|
| `id` | Integer | No | `ingredient.id` |
| `ingredientCode` | String | Yes | `ingredient.ingredient_code` |
| `ingredientName` | String | No | `ingredient.ingredient_name` |
| `selfLife` | Integer | No | `ingredient.shelf_life` (ngày) |
| `averagePrice` | BigDecimal | Yes | `ingredient.average_price` |
| `createdTime` | Instant | No | `ingredient.created_at` |
| `active` | Boolean | No | `ingredient.is_active` |
| `ingredientCategoryId` | Integer | No | `ingredient.ingredient_category_id` |
| `ingredientCategoryCode` | String | Yes | JOIN `ingredient_category.ingredient_category_code` |
| `ingredientCategoryName` | String | No | JOIN `ingredient_category.ingredient_category_name` |
| `supplierId` | Integer | No | `ingredient.supplier_id` |
| `supplierCode` | String | Yes | JOIN `supplier.supplier_code` |
| `supplierName` | String | No | JOIN `supplier.supplier_name` |
| `unitId` | Integer | No | `ingredient.unit_id` |
| `unitCode` | String | Yes | JOIN `unit.unit_code` |
| `unitName` | String | No | JOIN `unit.unit_name` |
| `stockLevels` | List | No | JPA query `StockLevelRepository` |

### 3.2. IngredientCategorySelectDTO (2 fields)

| Field | Type | Nguồn |
|---|---|---|
| `ingredientCategoryCode` | String | `ingredient_category.ingredient_category_code` |
| `ingredientCategoryName` | String | `ingredient_category.ingredient_category_name` |

### 3.3. UnitSelectDTO (2 fields)

| Field | Type | Nguồn |
|---|---|---|
| `unitCode` | String | `unit.unit_code` |
| `unitName` | String | `unit.unit_name` |

### 3.4. SupplierSelectDTO (2 fields)

| Field | Type | Nguồn |
|---|---|---|
| `supplierCode` | String | `supplier.supplier_code` |
| `supplierName` | String | `supplier.supplier_name` |

---

## 4. HƯỚNG DẪN TÍCH HỢP FE

### 4.1. Khởi tạo form (gọi song song)

```typescript
// Gọi 3 dropdown song song (form Thêm mới)
const [categories, units, suppliers] = await Promise.all([
  httpClient.post('/api/v1/ingredient/danh-sach-danh-muc'),
  httpClient.post('/api/v1/ingredient/danh-sach-don-vi'),
  httpClient.post('/api/v1/ingredient/danh-sach-nha-cung-cap')
]);

// Gọi thêm detail khi form Cập nhật
const detail = await httpClient.post('/api/v1/ingredient/detail', { id: ingredientId });
```

### 4.2. Mapping dropdown value

3 dropdown sử dụng field `*Code` làm **value** và `*Name` làm **display text**:

```html
<!-- Ví dụ: Dropdown danh mục -->
<select>
  <option *ngFor="let cat of categories"
          [value]="cat.ingredientCategoryCode">
    {{ cat.ingredientCategoryName }}
  </option>
</select>
```

Khi pre-fill form cập nhật, so khớp `*Code` từ `/detail` response với danh sách dropdown để set `selected`:
- `detail.ingredientCategoryCode` → match với dropdown danh mục
- `detail.unitCode` → match với dropdown đơn vị
- `detail.supplierCode` → match với dropdown nhà cung cấp

### 4.3. Business Rules

| Rule | Mô Tả |
|---|---|
| Chỉ active | 3 API dropdown chỉ trả về bản ghi `active = true` |
| Sắp xếp | Tất cả sắp xếp A→Z theo tên (alphabetical) |
| Không phân trang | Trả về toàn bộ danh sách (dữ liệu dropdown thường ít) |
| Không cần request body | 3 API dropdown không nhận filter — gửi `{}` hoặc empty |
| Error handling | `/detail` trả 400 nếu `id` null hoặc không tồn tại |
