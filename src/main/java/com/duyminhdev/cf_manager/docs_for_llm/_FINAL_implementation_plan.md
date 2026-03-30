# 16 API Backend — TableList Business Module

Implementation plan for the 16 APIs spanning 4 business flows: Table display, Table booking, Dish ordering, Payment processing.

## User Review Required

> [!IMPORTANT]
> Please review and approve this plan. This is adapted directly from the provided `implementation_plan_table_list_booking_order.md`. Once approved, I will begin implementing the ~55 files (including entities, repositories, specifications, DTOs, services, and controllers).

## Proposed Changes

### Entities

#### [MODIFY] [TableEntity.java](file:///d:/.DATN/cf_manager/BE/cf-manager/cf-manager/src/main/java/com/duyminhdev/cf_manager/entity/TableEntity.java)
- Add `floor` (Integer), map `@Column(name = "floor")`

#### [MODIFY] [TableBooking.java](file:///d:/.DATN/cf_manager/BE/cf-manager/cf-manager/src/main/java/com/duyminhdev/cf_manager/entity/TableBooking.java)
- Add `note` (String), map `@Column(name = "note", length = 255)`

---

### Constants

#### [NEW] [VnPayConstant.java](file:///d:/.DATN/cf_manager/BE/cf-manager/cf-manager/src/main/java/com/duyminhdev/cf_manager/constant/VnPayConstant.java)
- Create constant `VNPAY_URL`

---

### Repositories

#### [NEW] `TableRepository.java`, `TableBookingRepository.java`, `DishRepository.java`
- Extend `JpaRepository` and `JpaSpecificationExecutor`

#### [NEW] `DishOrderRepository.java`, `DishOrderDetailRepository.java`, `DishOrderStatusRepository.java`, `DishCategoryRepository.java`, `InvoiceRepository.java`, `InvoiceDetailRepository.java`
- Extend `JpaRepository` with custom finder methods as needed.

#### [NEW] `impl/NativeSqlTableRepository.java`, `impl/NativeSqlDishOrderDetailRepository.java`
- Native SQL Repositories for complex joins.

---

### Specifications

#### [NEW] `spec/TableBookingSpec.java`
- Dynamic filtering for tableId, bookingStatus, customerName, phoneNumber.

#### [NEW] `spec/DishSpec.java`
- Dynamic filtering for dishName, dishCategoryId, active.

---

### Request DTOs

#### [NEW] DTOs under `dto/request/`
- `TableBookingSearchRequest.java` (extends PageFilterRequest)
- `TableBookingRequest.java`
- `DishOrderRequest.java`
- `DishOrderDetailRequest.java`
- `DishOrderStatusUpdateRequest.java`
- `DishSearchRequest.java` (extends PageFilterRequest)
- `InvoiceRequest.java`
- `InvoiceDetailRequest.java`
- `TableStatusUpdateRequest.java`

---

### Response DTOs

#### [NEW] DTOs under `dto/response/`
- `TableListDto.java`
- `TableDetailDto.java`
- `TableBookingDto.java`
- `DishOrderDto.java`
- `DishOrderDetailDto.java`
- `DishDto.java`
- `DishCategoryDto.java`
- `InvoiceDto.java`
- `InvoiceDetailDto.java`

---

### Services

#### [NEW] Service Interfaces and Implementations under `service/` and `service/impl/`
- `TableService`: GET All (Native SQL stats), GET By Id, Update Status
- `TableBookingService`: Search (Spec), AddOrUpdate
- `DishOrderService`: GetByTable, AddOrUpdate, UpdateStatus
- `DishOrderDetailService`: GetByOrder, GetByTable (Native SQL)
- `DishService`: GetAll, Search (Spec)
- `DishCategoryService`: GetAll
- `InvoiceService`: Count, AddOrUpdate, GetDetailById

---

### Controllers

#### [NEW] Controllers under `controller/`
- `TableController`
- `TableBookingController`
- `DishOrderController`
- `DishOrderDetailController`
- `DishController`
- `DishCategoryController`
- `InvoiceController`

*(All APIs secured by JWT, returning `ApiResponse<T>`)*

## Open Questions

- No open questions as the plan was explicitly provided via `implementation_plan_table_list_booking_order.md`. You just need to approve it so I can start execution!

## Verification Plan

### Automated Tests
- Since the plan specifies "*Không tự chạy test. Chỉ báo cáo task + giải thích flow*", I will not run automated tests but will provide detailed task tracking and flow explanations.

### Manual Verification
- Review the generated files for RESTful correctness, JPA specification syntax, DTO mapping, and Service transactional logic.
