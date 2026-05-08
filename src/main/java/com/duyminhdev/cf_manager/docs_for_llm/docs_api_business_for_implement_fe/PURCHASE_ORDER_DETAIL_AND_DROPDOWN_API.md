# PURCHASE ORDER - API DETAIL VA DROPDOWN NGUON DU LIEU

> **Muc dich:** Mo ta 4 API phuc vu man hinh **Them moi / Cap nhat don nhap hang**:
> - 1 API lay chi tiet don nhap hang de pre-fill form update
> - 3 API cap nguon du lieu cho dropdown/select: kho, nha cung cap, nguyen lieu theo nha cung cap

> **Base URL:** `POST /api/v1/purchase-order/...`

---

## MUC LUC

1. [Tong Quan Luong Nghiep Vu](#1-tong-quan-luong-nghiep-vu)
2. [API Contract](#2-api-contract)
3. [Response DTO Reference](#3-response-dto-reference)
4. [Huong Dan Tich Hop FE](#4-huong-dan-tich-hop-fe)

---

## 1. TONG QUAN LUONG NGHIEP VU

### Boi canh su dung

Khi user mo form **Them moi** hoac **Cap nhat** don nhap hang, FE can:

1. Lay danh sach **kho nhan hang**
2. Lay danh sach **nha cung cap**
3. Khi da biet nha cung cap, lay danh sach **nguyen lieu thuoc nha cung cap do**
4. Neu la man hinh update, lay **chi tiet don nhap hang** de pre-fill toan bo form

### Diem quan trong ve business

- Dropdown **nguyen lieu** phu thuoc vao `supplierId`.
- FE phai dung cac field `id` de submit lai cho backend:
  - `warehouseId`
  - `supplierId`
  - `ingredientId`
- API `/detail` da duoc mo rong de tra them:
  - Header: `supplierId`, `warehouseId`
  - Tung dong chi tiet: `ingredientCode`, `supplierId`

### Luong goi API khuyen nghi

#### Truong hop 1: Mo form Them moi

```text
Mo form
  -> Goi song song:
     POST /purchase-order/danh-sach-nha-kho
     POST /purchase-order/danh-sach-nha-cung-cap
  -> Render 2 dropdown: kho + nha cung cap
  -> Khi user chon nha cung cap:
     POST /purchase-order/danh-sach-nguyen-lieu-theo-ncc { "supplierId": ... }
  -> Render dropdown / bang chon nguyen lieu theo NCC da chon
```

#### Truong hop 2: Mo form Cap nhat

```text
Mo form update
  -> Goi song song:
     POST /purchase-order/detail { "id": purchaseOrderId }
     POST /purchase-order/danh-sach-nha-kho
     POST /purchase-order/danh-sach-nha-cung-cap
  -> Lay supplierId tu detail response
  -> Goi:
     POST /purchase-order/danh-sach-nguyen-lieu-theo-ncc { "supplierId": detail.supplierId }
  -> Render form + pre-fill du lieu
```

---

## 2. API CONTRACT

### Bang Tong Hop

| # | Endpoint | Method | Body | Mo ta |
|---|---|---|---|---|
| 1 | `/api/v1/purchase-order/detail` | POST | `{ "id": Integer }` | Lay chi tiet don nhap hang de pre-fill form update |
| 2 | `/api/v1/purchase-order/danh-sach-nha-kho` | POST | Khong can body | Lay danh sach kho active cho dropdown |
| 3 | `/api/v1/purchase-order/danh-sach-nha-cung-cap` | POST | Khong can body | Lay danh sach nha cung cap active cho dropdown |
| 4 | `/api/v1/purchase-order/danh-sach-nguyen-lieu-theo-ncc` | POST | `{ "supplierId": Integer }` | Lay danh sach nguyen lieu active thuoc 1 nha cung cap |

---

### 2.1. `POST /api/v1/purchase-order/detail`

**Chuc nang:** Lay chi tiet 1 don nhap hang, bao gom:

- Thong tin header cua don
- Nha cung cap va kho dang duoc gan cho don
- Danh sach dong nguyen lieu trong don
- Thong tin mo rong phuc vu update form:
  - `supplierId`
  - `warehouseId`
  - `ingredientCode`
  - `supplierId` cua tung dong detail

**Khi nao dung:** Khi user mo man hinh xem chi tiet hoac cap nhat don nhap hang.

**Request body:**

```json
{
  "id": 15
}
```

| Field | Type | Required | Mo ta |
|---|---|---|---|
| `id` | Integer | Yes | ID don nhap hang |

**Response - `ApiResponse<PurchaseOrderDetailResponseDTO>`:**

```json
{
  "status": 200,
  "message": "DETAIL_SUCCESS",
  "data": {
    "id": 15,
    "purchaseOrderCode": "PO-000015",
    "totalPrice": 245000,
    "paymentStatus": "DRAFT",
    "accountFullName": "Nguyen Van A",

    "supplierId": 3,
    "supplierName": "Cong ty Nguyen Lieu ABC",

    "warehouseId": 2,
    "warehouseName": "Kho chinh",

    "createdTime": "2026-05-06T08:30:00Z",
    "orderDate": "2026-05-06T08:00:00Z",

    "details": [
      {
        "id": 101,
        "ingredientId": 11,
        "ingredientCode": "NL-011",
        "ingredientName": "Ca phe Robusta",
        "supplierId": 3,
        "quantity": 5,
        "unitPrice": 35000,
        "lineTotal": 175000
      },
      {
        "id": 102,
        "ingredientId": 12,
        "ingredientCode": "NL-012",
        "ingredientName": "Sua dac",
        "supplierId": 3,
        "quantity": 2,
        "unitPrice": 35000,
        "lineTotal": 70000
      }
    ]
  }
}
```

**Luu y quan trong:**

- FE phai dung `supplierId` va `warehouseId` de set selected value cho dropdown header.
- FE phai dung `ingredientId` cho submit create/update; `ingredientCode` chi de hien thi/phu tro mapping.
- `details[].supplierId` thuong se trung voi `data.supplierId`, nhung backend van tra tung dong de FE/LLM khong can suy luan.
- API nay khong thay doi request contract cu, chi bo sung field vao response.

**Backend implementation note:**

- Header va danh sach item duoc lay bang native query join de tra du lieu enrich on dinh cho FE.

---

### 2.2. `POST /api/v1/purchase-order/danh-sach-nha-kho`

**Chuc nang:** Lay toan bo kho dang hoat dong (`active = true`), sap xep tang dan theo `warehouseName`.

**Khi nao dung:** Render dropdown "Kho nhan hang" trong form them/sua don nhap hang.

**Request:** Khong can body. FE co the gui `{}` hoac body rong.

**Response - `ApiResponse<List<PurchaseOrderWarehouseSelectDTO>>`:**

```json
{
  "status": 200,
  "message": "GET_DANH_SACH_NHA_KHO_SUCCESS",
  "data": [
    {
      "id": 1,
      "warehouseCode": "KHO-001",
      "warehouseName": "Kho chinh"
    },
    {
      "id": 2,
      "warehouseCode": "KHO-002",
      "warehouseName": "Kho du phong"
    }
  ]
}
```

| Field | Type | Mo ta |
|---|---|---|
| `id` | Integer | Gia tri FE phai submit lai vao `warehouseId` |
| `warehouseCode` | String | Ma kho de hien thi / doi soat |
| `warehouseName` | String | Ten kho hien thi cho user |

**Backend:** `WarehouseRepository.findAllByActiveTrueOrderByWarehouseNameAsc()`

---

### 2.3. `POST /api/v1/purchase-order/danh-sach-nha-cung-cap`

**Chuc nang:** Lay toan bo nha cung cap dang hoat dong (`active = true`), sap xep tang dan theo `supplierName`.

**Khi nao dung:** Render dropdown "Nha cung cap" trong form them/sua don nhap hang.

**Request:** Khong can body.

**Response - `ApiResponse<List<PurchaseOrderSupplierSelectDTO>>`:**

```json
{
  "status": 200,
  "message": "GET_DANH_SACH_NHA_CUNG_CAP_SUCCESS",
  "data": [
    {
      "id": 3,
      "supplierCode": "NCC-003",
      "supplierName": "Cong ty Nguyen Lieu ABC"
    },
    {
      "id": 4,
      "supplierCode": "NCC-004",
      "supplierName": "Cong ty Sua XYZ"
    }
  ]
}
```

| Field | Type | Mo ta |
|---|---|---|
| `id` | Integer | Gia tri FE phai submit lai vao `supplierId` |
| `supplierCode` | String | Ma NCC de hien thi / doi soat |
| `supplierName` | String | Ten NCC hien thi cho user |

**Backend:** `SupplierRepository.findAllByActiveTrueOrderBySupplierNameAsc()`

---

### 2.4. `POST /api/v1/purchase-order/danh-sach-nguyen-lieu-theo-ncc`

**Chuc nang:** Lay danh sach nguyen lieu dang hoat dong thuoc 1 nha cung cap cu the.

**Khi nao dung:**

- Sau khi user chon `supplierId` trong form them moi
- Sau khi load xong `/detail` trong form update de lay dropdown nguyen lieu theo NCC cua don
- Moi khi user doi nha cung cap, FE phai goi lai API nay

**Request - `PurchaseOrderIngredientBySupplierRequestDTO`:**

```json
{
  "supplierId": 3
}
```

| Field | Type | Required | Validation | Mo ta |
|---|---|---|---|---|
| `supplierId` | Integer | Yes | `@NotNull` | ID nha cung cap can loc |

**Response - `ApiResponse<List<PurchaseOrderIngredientSelectDTO>>`:**

```json
{
  "status": 200,
  "message": "GET_DANH_SACH_NGUYEN_LIEU_THEO_NCC_SUCCESS",
  "data": [
    {
      "ingredientId": 11,
      "ingredientCode": "NL-011",
      "ingredientName": "Ca phe Robusta",
      "supplierId": 3
    },
    {
      "ingredientId": 12,
      "ingredientCode": "NL-012",
      "ingredientName": "Sua dac",
      "supplierId": 3
    }
  ]
}
```

| Field | Type | Mo ta |
|---|---|---|
| `ingredientId` | Integer | Gia tri FE phai submit lai vao `details[].ingredientId` |
| `ingredientCode` | String | Ma nguyen lieu de hien thi / doi soat |
| `ingredientName` | String | Ten nguyen lieu hien thi cho user |
| `supplierId` | Integer | NCC ma nguyen lieu dang thuoc ve |

**Luu y quan trong:**

- API nay loc theo `supplierId`, khong loc theo `supplierCode`.
- Chi tra ve nguyen lieu `active = true`.
- Backend co kiem tra `supplierId` ton tai va NCC dang active truoc khi query.
- Query duoc thuc hien bang native SQL join `ingredient` va `supplier`.

**Neu supplier khong hop le:**

- Tra loi loi business do backend throw `InvalidDataException`
- FE nen show message va disable dropdown nguyen lieu

---

## 3. RESPONSE DTO REFERENCE

### 3.1. PurchaseOrderDetailResponseDTO

| Field | Type | Nullable | Nguon |
|---|---|---|---|
| `id` | Integer | No | `purchase_order.id` |
| `purchaseOrderCode` | String | Yes | `purchase_order.purchase_order_code` |
| `totalPrice` | BigDecimal | No | `purchase_order.total_amount` |
| `paymentStatus` | String | No | `purchase_order.payment_status` |
| `accountFullName` | String | Yes | JOIN `account.full_name` |
| `supplierId` | Integer | Yes | `purchase_order.supplier_id` |
| `supplierName` | String | Yes | JOIN `supplier.supplier_name` |
| `warehouseId` | Integer | Yes | `purchase_order.warehouse_id` |
| `warehouseName` | String | Yes | JOIN `warehouse.warehouse_name` |
| `createdTime` | Instant | Yes | `purchase_order.created_at` |
| `orderDate` | Instant | Yes | `purchase_order.ordered_at` |
| `details` | List<DetailItem> | No | Chi tiet don nhap hang |

### 3.2. PurchaseOrderDetailResponseDTO.DetailItem

| Field | Type | Nullable | Nguon |
|---|---|---|---|
| `id` | Integer | Yes | `purchase_order_detail.id` |
| `ingredientId` | Integer | No | `purchase_order_detail.ingredient_id` |
| `ingredientCode` | String | Yes | JOIN `ingredient.ingredient_code` |
| `ingredientName` | String | Yes | JOIN `ingredient.ingredient_name` |
| `supplierId` | Integer | Yes | JOIN `ingredient.supplier_id` |
| `quantity` | Integer | No | `purchase_order_detail.quantity` |
| `unitPrice` | BigDecimal | No | `purchase_order_detail.unit_price` |
| `lineTotal` | BigDecimal | No | `quantity * unitPrice` |

### 3.3. PurchaseOrderWarehouseSelectDTO

| Field | Type | Nguon |
|---|---|---|
| `id` | Integer | `warehouse.id` |
| `warehouseCode` | String | `warehouse.warehouse_code` |
| `warehouseName` | String | `warehouse.warehouse_name` |

### 3.4. PurchaseOrderSupplierSelectDTO

| Field | Type | Nguon |
|---|---|---|
| `id` | Integer | `supplier.id` |
| `supplierCode` | String | `supplier.supplier_code` |
| `supplierName` | String | `supplier.supplier_name` |

### 3.5. PurchaseOrderIngredientSelectDTO

| Field | Type | Nguon |
|---|---|---|
| `ingredientId` | Integer | `ingredient.id` |
| `ingredientCode` | String | `ingredient.ingredient_code` |
| `ingredientName` | String | `ingredient.ingredient_name` |
| `supplierId` | Integer | `ingredient.supplier_id` |

---

## 4. HUONG DAN TICH HOP FE

### 4.1. Goi API khi khoi tao form

```typescript
const [warehousesRes, suppliersRes] = await Promise.all([
  httpClient.post('/api/v1/purchase-order/danh-sach-nha-kho', {}),
  httpClient.post('/api/v1/purchase-order/danh-sach-nha-cung-cap', {})
]);

const warehouses = warehousesRes.data.data;
const suppliers = suppliersRes.data.data;
```

### 4.2. Goi API phu thuoc theo supplier

```typescript
async function loadIngredientsBySupplier(supplierId: number) {
  const res = await httpClient.post(
    '/api/v1/purchase-order/danh-sach-nguyen-lieu-theo-ncc',
    { supplierId }
  );

  return res.data.data;
}
```

### 4.3. Luong update form

```typescript
const [detailRes, warehousesRes, suppliersRes] = await Promise.all([
  httpClient.post('/api/v1/purchase-order/detail', { id: purchaseOrderId }),
  httpClient.post('/api/v1/purchase-order/danh-sach-nha-kho', {}),
  httpClient.post('/api/v1/purchase-order/danh-sach-nha-cung-cap', {})
]);

const detail = detailRes.data.data;
const ingredients = await loadIngredientsBySupplier(detail.supplierId);
```

### 4.4. Mapping value cho dropdown

- Dropdown kho:
  - `value = warehouse.id`
  - `label = warehouse.warehouseName` (co the hien them `warehouseCode`)
- Dropdown nha cung cap:
  - `value = supplier.id`
  - `label = supplier.supplierName`
- Dropdown nguyen lieu:
  - `value = ingredient.ingredientId`
  - `label = ingredient.ingredientName`

### 4.5. Mapping pre-fill tu `/detail`

- Header:
  - `detail.warehouseId` -> selected warehouse
  - `detail.supplierId` -> selected supplier
- Tung dong chi tiet:
  - `detailItem.ingredientId` -> selected ingredient
  - `detailItem.quantity` -> input so luong
  - `detailItem.unitPrice` -> input don gia

### 4.6. Business rules FE can tuan thu

| Rule | Mo ta |
|---|---|
| Dropdown kho/NCC | Khong phan trang, load toan bo danh sach active |
| Dropdown nguyen lieu | Luon phu thuoc vao `supplierId` |
| Doi nha cung cap | Phai reload danh sach nguyen lieu |
| Doi nha cung cap khi da co details | FE nen reset danh sach dong chi tiet hoac buoc user chon lai nguyen lieu |
| Submit create/update | Phai gui `warehouseId`, `supplierId`, `details[].ingredientId` theo ID |
| `ingredientCode` / `supplierCode` | Chi dung de hien thi, khong phai field submit |

### 4.7. Tom tat cho LLM implement FE

Neu LLM can implement man hinh them/sua don nhap hang, hay coi cac API nay theo logic sau:

1. Load 2 dropdown co ban: kho + nha cung cap
2. Nha cung cap duoc chon xong moi load dropdown/bang chon nguyen lieu
3. Neu la update, lay `/detail` truoc de biet `supplierId` hien tai
4. Sau do load danh sach nguyen lieu theo `detail.supplierId`
5. Toan bo selected value de submit ve backend deu la **ID**, khong phai code

