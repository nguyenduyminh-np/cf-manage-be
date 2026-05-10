Chúng ta sẽ triển khai đầy đủ code backend cho chức năng Quản lý Đơn nhập hàng (Purchase Order) dựa trên thiết kế business flow đã cho. Các package, class được xây dựng theo đúng kiến trúc hiện tại của dự án Spring Boot.

## 1. Entity

### 1.1. PurchaseOrder
```java
package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_order")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "purchase_order_code", length = 255)
    private String purchaseOrderCode;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 0)
    private BigDecimal totalAmount;

    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus;   // DRAFT, PENDING, APPROVED, COMPLETED, CANCELLED

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) DEFAULT 1")
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "ordered_at")
    private Instant orderedAt;   // ngày dự kiến nhận

    // Quan hệ 1-n với PurchaseOrderDetail
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderDetail> details = new ArrayList<>();
}
```

### 1.2. PurchaseOrderDetail
```java
package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_order_detail")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PurchaseOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 0)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;
}
```

### 1.3. Ingredient (đã có, nhưng cần bổ sung quan hệ nếu cần)
Giả sử entity có sẵn, chỉ cần thêm mapping nếu cần. Trong code này ta không cần sửa Ingredient.

### 1.4. StockTransaction
```java
package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_transaction")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "stock_transaction_code", length = 255)
    private String stockTransactionCode;

    @Column(columnDefinition = "longtext")
    private String note;

    @Column(name = "transaction_type", nullable = false, length = 100)
    private String transactionType;   // IMPORT, EXPORT, ADJUSTMENT_IN, ADJUSTMENT_OUT

    @Column(name = "total_amount", nullable = false, precision = 18)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 100)
    private String status;   // completed, pending, cancelled...

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "transaction_at")
    private Instant transactionAt;

    // Các trường khác nếu cần
    // ...
}
```

### 1.5. StockLevel
```java
package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_level")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "expiration_at", nullable = false)
    private Instant expirationAt;

    @Column(name = "unit_price", nullable = false, precision = 18)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
```

### 1.6. StockTransactionDetail
```java
package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "stock_transaction_detail")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockTransactionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_level_id", nullable = false)
    private StockLevel stockLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_transaction_id", nullable = false)
    private StockTransaction stockTransaction;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;
}
```

### 1.7. Debt
```java
package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "debt")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Debt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "debt_code", length = 255)
    private String debtCode;

    @Column(name = "debt_name", nullable = false, length = 255)
    private String debtName;

    @Column(name = "total_amount", nullable = false, precision = 18)
    private BigDecimal totalAmount;

    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(columnDefinition = "longtext")
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;
}
```

### 1.8. CashFlow
```java
package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cash_flow")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CashFlow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "total_amount", nullable = false, precision = 18)
    private BigDecimal totalAmount;

    @Column(name = "flow_type", nullable = false, length = 255)
    private String flowType;

    @Column(name = "note", nullable = false, length = 255)
    private String note;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
```

### 1.9. AccountActivity
```java
package com.duyminhdev.cf_manager.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "account_activity")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "activity_code", length = 255)
    private String activityCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdTime;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "activity_description", nullable = false, length = 500)
    private String activityDescription;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;
}
```

## 2. DTOs

### 2.1. Request DTOs

```java
// PurchaseOrderSearchRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.purchase_order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class PurchaseOrderSearchRequestDTO {
    private String purchaseOrderCode;
    private String paymentStatus;
    private BigDecimal totalAmountFrom;
    private BigDecimal totalAmountTo;
    private Instant fromDate;
    private Instant toDate;
    private Integer page;
    private Integer limit;
    private String sortField;
    private String sortDir;
}
```

```java
// PurchaseOrderCreateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.purchase_order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class PurchaseOrderCreateRequestDTO {
    private String purchaseOrderCode;   // optional, tự sinh nếu null
    @NotNull(message = "Tổng tiền không được trống")
    private BigDecimal totalAmount;
    @NotNull(message = "Trạng thái thanh toán không được trống")
    private String paymentStatus;   // DRAFT, PENDING, APPROVED
    private Instant orderedAt;
    @NotEmpty(message = "Chi tiết đơn hàng không được trống")
    private List<Detail> details;

    @Data
    public static class Detail {
        @NotNull(message = "Mã nguyên liệu không được trống")
        private Integer ingredientId;
        @NotNull(message = "Số lượng không được trống")
        private Integer quantity;
        @NotNull(message = "Đơn giá không được trống")
        private BigDecimal unitPrice;
    }
}
```

```java
// PurchaseOrderUpdateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.purchase_order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class PurchaseOrderUpdateRequestDTO {
    @NotNull(message = "Id không được trống")
    private Integer id;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private Instant orderedAt;
    private List<Detail> details;   // danh sách mới (thay thế hoàn toàn)

    @Data
    public static class Detail {
        private Integer id;   // null -> thêm mới, != null -> cập nhật
        @NotNull(message = "Mã nguyên liệu không được trống")
        private Integer ingredientId;
        @NotNull(message = "Số lượng không được trống")
        private Integer quantity;
        @NotNull(message = "Đơn giá không được trống")
        private BigDecimal unitPrice;
    }
}
```

```java
// PurchaseOrderStatusUpdateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.purchase_order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseOrderStatusUpdateRequestDTO {
    @NotNull(message = "Id đơn hàng không được trống")
    private Integer id;
    @NotNull(message = "Trạng thái mới không được trống")
    private String newStatus; // APPROVED, COMPLETED, CANCELLED
    // Required only when newStatus = COMPLETED
    private Integer warehouseId;
    private Integer supplierId;
}
```

### 2.2. Response DTOs

```java
// PurchaseOrderListItemDTO.java
package com.duyminhdev.cf_manager.dto.response.purchase_order;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class PurchaseOrderListItemDTO {
    private Integer id;
    private String purchaseOrderCode;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String accountFullName;
    private Instant createdTime;
    private Instant orderedAt;
}
```

```java
// PurchaseOrderDetailResponseDTO.java
package com.duyminhdev.cf_manager.dto.response.purchase_order;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PurchaseOrderDetailResponseDTO {
    private Integer id;
    private String purchaseOrderCode;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String accountFullName;
    private Instant createdTime;
    private Instant orderedAt;
    private List<DetailItem> details;

    @Data
    @Builder
    public static class DetailItem {
        private Integer id;
        private Integer ingredientId;
        private String ingredientName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }
}
```

## 3. Repositories

```java
// PurchaseOrderRepository.java
package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer>,
        JpaSpecificationExecutor<PurchaseOrder> {
}
```

Tương tự cho các repository khác: `PurchaseOrderDetailRepository`, `StockTransactionRepository`, `StockLevelRepository`, `StockTransactionDetailRepository`, `DebtRepository`, `CashFlowRepository`, `AccountActivityRepository`. Tất cả extends `JpaRepository`.

```java
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Integer> {}
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Integer> {}
public interface StockLevelRepository extends JpaRepository<StockLevel, Integer> {}
public interface StockTransactionDetailRepository extends JpaRepository<StockTransactionDetail, Integer> {}
public interface DebtRepository extends JpaRepository<Debt, Integer> {}
public interface CashFlowRepository extends JpaRepository<CashFlow, Integer> {}
public interface AccountActivityRepository extends JpaRepository<AccountActivity, Integer> {}
```

### SupplierRepository, WarehouseRepository, IngredientRepository, AccountRepository đã có.

## 4. Specification cho Search

```java
package com.duyminhdev.cf_manager.repository.spec;

import com.duyminhdev.cf_manager.dto.request.purchase_order.PurchaseOrderSearchRequestDTO;
import com.duyminhdev.cf_manager.entity.PurchaseOrder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderSpec {

    public static Specification<PurchaseOrder> byCriteria(PurchaseOrderSearchRequestDTO request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(request.getPurchaseOrderCode())) {
                predicates.add(cb.like(root.get("purchaseOrderCode"), "%" + request.getPurchaseOrderCode().trim() + "%"));
            }
            if (StringUtils.hasText(request.getPaymentStatus())) {
                predicates.add(cb.equal(root.get("paymentStatus"), request.getPaymentStatus().trim()));
            }
            if (request.getTotalAmountFrom() != null) {
                predicates.add(cb.ge(root.get("totalAmount"), request.getTotalAmountFrom()));
            }
            if (request.getTotalAmountTo() != null) {
                predicates.add(cb.le(root.get("totalAmount"), request.getTotalAmountTo()));
            }
            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), request.getToDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<PurchaseOrder> sortByField(String sortField, String sortDir) {
        return (root, query, cb) -> {
            String field = (sortField != null) ? sortField : "createdTime";
            boolean asc = !"desc".equalsIgnoreCase(sortDir);
            switch (field) {
                case "totalAmount":
                    query.orderBy(asc ? cb.asc(root.get("totalAmount")) : cb.desc(root.get("totalAmount")));
                    break;
                case "paymentStatus":
                    query.orderBy(asc ? cb.asc(root.get("paymentStatus")) : cb.desc(root.get("paymentStatus")));
                    break;
                default:
                    query.orderBy(asc ? cb.asc(root.get("createdTime")) : cb.desc(root.get("createdTime")));
            }
            return cb.conjunction();
        };
    }
}
```

## 5. Service

### 5.1. Interface

```java
package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.purchase_order.*;
import com.duyminhdev.cf_manager.dto.response.purchase_order.*;

import java.util.List;

public interface PurchaseOrderService {
    PageResponse<List<PurchaseOrderListItemDTO>> search(PurchaseOrderSearchRequestDTO request);
    PurchaseOrderDetailResponseDTO create(PurchaseOrderCreateRequestDTO request);
    PurchaseOrderDetailResponseDTO update(PurchaseOrderUpdateRequestDTO request);
    void updateStatus(PurchaseOrderStatusUpdateRequestDTO request);
    PurchaseOrderDetailResponseDTO getDetail(Integer id);
}
```

### 5.2. Implementation

```java
package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.purchase_order.*;
import com.duyminhdev.cf_manager.dto.response.purchase_order.*;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.*;
import com.duyminhdev.cf_manager.repository.spec.PurchaseOrderSpec;
import com.duyminhdev.cf_manager.service.PurchaseOrderService;
import com.duyminhdev.cf_manager.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository poRepository;
    private final PurchaseOrderDetailRepository detailRepository;
    private final IngredientRepository ingredientRepository;
    private final StockTransactionRepository stockTxRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockTransactionDetailRepository stockTxDetailRepository;
    private final DebtRepository debtRepository;
    private final CashFlowRepository cashFlowRepository;
    private final AccountActivityRepository activityRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final AccountRepository accountRepository; // để lấy current user

    @Override
    public PageResponse<List<PurchaseOrderListItemDTO>> search(PurchaseOrderSearchRequestDTO request) {
        int page = PageUtils.normalizePage(request.getPage());
        int limit = PageUtils.normalizeLimit(request.getLimit());
        Pageable pageable = PageRequest.of(page, limit);

        Specification<PurchaseOrder> spec = Specification
                .where(PurchaseOrderSpec.byCriteria(request))
                .and(PurchaseOrderSpec.sortByField(request.getSortField(), request.getSortDir()));

        Page<PurchaseOrder> poPage = poRepository.findAll(spec, pageable);
        List<PurchaseOrderListItemDTO> rows = poPage.getContent().stream()
                .map(po -> PurchaseOrderListItemDTO.builder()
                        .id(po.getId())
                        .purchaseOrderCode(po.getPurchaseOrderCode())
                        .totalAmount(po.getTotalAmount())
                        .paymentStatus(po.getPaymentStatus())
                        .accountFullName(po.getAccount().getFullName())
                        .createdTime(po.getCreatedTime())
                        .orderedAt(po.getOrderedAt())
                        .build())
                .collect(Collectors.toList());

        PageResponse<List<PurchaseOrderListItemDTO>> resp = new PageResponse<>();
        resp.setRows(rows);
        resp.setPageNo(poPage.getNumber());
        resp.setPageSize(poPage.getSize());
        resp.setTotalElements((int) poPage.getTotalElements());
        resp.setTotalPages(poPage.getTotalPages());
        return resp;
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponseDTO create(PurchaseOrderCreateRequestDTO request) {
        Account currentUser = getCurrentAccount();
        PurchaseOrder po = new PurchaseOrder();
        po.setPurchaseOrderCode(generateNextCode());
        po.setTotalAmount(request.getTotalAmount());
        po.setPaymentStatus(request.getPaymentStatus());
        po.setCreatedTime(Instant.now());
        po.setActive(true);
        po.setAccount(currentUser);
        po.setOrderedAt(request.getOrderedAt());

        po = poRepository.save(po);

        // Save details
        List<PurchaseOrderDetail> details = new ArrayList<>();
        for (var dto : request.getDetails()) {
            Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
                    .orElseThrow(() -> new InvalidDataException("Nguyên liệu không tồn tại"));
            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .purchaseOrder(po)
                    .ingredient(ingredient)
                    .quantity(dto.getQuantity())
                    .unitPrice(dto.getUnitPrice())
                    .createdTime(Instant.now())
                    .active(true)
                    .build();
            details.add(detail);
        }
        detailRepository.saveAll(details);

        // AccountActivity
        createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Tạo đơn nhập hàng");

        return mapToDetail(po);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponseDTO update(PurchaseOrderUpdateRequestDTO request) {
        PurchaseOrder po = poRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Đơn nhập hàng không tồn tại"));
        if (List.of("COMPLETED", "CANCELLED").contains(po.getPaymentStatus())) {
            throw new InvalidDataException("Không thể sửa đơn hàng đã hoàn thành hoặc đã hủy");
        }

        if (request.getTotalAmount() != null) po.setTotalAmount(request.getTotalAmount());
        if (request.getPaymentStatus() != null) po.setPaymentStatus(request.getPaymentStatus());
        if (request.getOrderedAt() != null) po.setOrderedAt(request.getOrderedAt());

        // Xử lý details: xóa mềm tất cả chi tiết cũ, rồi thêm mới (hoặc logic merge)
        List<PurchaseOrderDetail> existingDetails = detailRepository.findByPurchaseOrderId(po.getId());
        // Đánh dấu tất cả cũ inactive
        existingDetails.forEach(d -> d.setActive(false));
        detailRepository.saveAll(existingDetails);

        // Thêm mới từ danh sách request
        List<PurchaseOrderDetail> newDetails = new ArrayList<>();
        for (var dto : request.getDetails()) {
            Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
                    .orElseThrow(() -> new InvalidDataException("Nguyên liệu không tồn tại"));
            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .purchaseOrder(po)
                    .ingredient(ingredient)
                    .quantity(dto.getQuantity())
                    .unitPrice(dto.getUnitPrice())
                    .createdTime(Instant.now())
                    .active(true)
                    .build();
            newDetails.add(detail);
        }
        detailRepository.saveAll(newDetails);

        createActivity(getCurrentAccount(), String.valueOf(po.getId()), "purchase_order", "Cập nhật đơn nhập hàng");

        return mapToDetail(poRepository.findById(po.getId()).get()); // refresh
    }

    @Override
    @Transactional
    public void updateStatus(PurchaseOrderStatusUpdateRequestDTO request) {
        PurchaseOrder po = poRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Đơn nhập hàng không tồn tại"));
        String newStatus = request.getNewStatus();
        if (po.getPaymentStatus().equals(newStatus)) return;

        switch (newStatus) {
            case "APPROVED":
            case "PENDING":
                po.setPaymentStatus(newStatus);
                createActivity(getCurrentAccount(), String.valueOf(po.getId()), "purchase_order",
                        "Đơn hàng được gửi/duyệt");
                break;
            case "CANCELLED":
                po.setPaymentStatus(newStatus);
                createActivity(getCurrentAccount(), String.valueOf(po.getId()), "purchase_order",
                        "Đơn nhập hàng đã bị hủy");
                break;
            case "COMPLETED":
                completeOrder(po, request);
                break;
            default:
                throw new InvalidDataException("Trạng thái không hợp lệ");
        }
        poRepository.save(po);
    }

    private void completeOrder(PurchaseOrder po, PurchaseOrderStatusUpdateRequestDTO request) {
        if (request.getWarehouseId() == null || request.getSupplierId() == null) {
            throw new InvalidDataException("Cần chỉ định kho và nhà cung cấp khi hoàn thành");
        }
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new InvalidDataException("Kho không tồn tại"));
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new InvalidDataException("Nhà cung cấp không tồn tại"));
        Account currentUser = getCurrentAccount();

        // 1. Cập nhật trạng thái PO
        po.setPaymentStatus("COMPLETED");

        // 2. Tạo StockTransaction IMPORT
        StockTransaction st = StockTransaction.builder()
                .stockTransactionCode("TX-IN-AUTO-" + po.getPurchaseOrderCode())
                .transactionType("IMPORT")
                .status("COMPLETED")
                .totalAmount(po.getTotalAmount())
                .warehouse(warehouse)
                .account(currentUser)
                .createdTime(Instant.now())
                .transactionAt(Instant.now())
                .note("Hệ thống nhập kho tự động cho đơn nhập hàng " + po.getPurchaseOrderCode())
                .active(true)
                .build();
        stockTxRepository.save(st);

        // 3. Với mỗi detail, tạo StockLevel và StockTransactionDetail
        List<PurchaseOrderDetail> details = detailRepository.findByPurchaseOrderIdAndActiveTrue(po.getId());
        for (PurchaseOrderDetail detail : details) {
            Ingredient ingredient = detail.getIngredient();
            // Cập nhật average price của nguyên liệu (logic hiện tại)
            ingredient.setAveragePrice(
                    (ingredient.getAveragePrice() != null ?
                            ingredient.getAveragePrice().add(detail.getUnitPrice()).divide(BigDecimal.valueOf(2)) :
                            detail.getUnitPrice())
            );
            ingredientRepository.save(ingredient);

            // Tạo StockLevel mới
            StockLevel sl = StockLevel.builder()
                    .ingredient(ingredient)
                    .warehouse(warehouse)
                    .quantity(detail.getQuantity())
                    .unitPrice(detail.getUnitPrice())
                    .expirationAt(Instant.now().plusSeconds(ingredient.getShelfLife() * 86400L)) // shelfLife days
                    .createdTime(Instant.now())
                    .updatedAt(Instant.now())
                    .active(true)
                    .build();
            stockLevelRepository.save(sl);

            // Tạo StockTransactionDetail
            StockTransactionDetail std = StockTransactionDetail.builder()
                    .stockTransaction(st)
                    .stockLevel(sl)
                    .quantity(detail.getQuantity())
                    .createdTime(Instant.now())
                    .active(true)
                    .build();
            stockTxDetailRepository.save(std);
        }

        // 4. Tạo Debt
        Debt debt = Debt.builder()
                .debtCode("DEBT_" + po.getPurchaseOrderCode())
                .debtName("Nợ nhập hàng " + po.getPurchaseOrderCode() + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .totalAmount(po.getTotalAmount())
                .isPaid(false)
                .note("Nợ nhập hàng " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .createdTime(Instant.now())
                .active(true)
                .supplier(supplier)
                .build();
        debtRepository.save(debt);

        // 5. Tạo CashFlow DEBT
        CashFlow cf = CashFlow.builder()
                .totalAmount(po.getTotalAmount())
                .flowType("DEBT")
                .note("Nợ nhập hàng " + po.getPurchaseOrderCode() + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .account(currentUser)
                .createdTime(Instant.now())
                .active(true)
                .build();
        cashFlowRepository.save(cf);

        // 6. Ghi activity
        createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Đơn nhập hàng đã hoàn thành");
    }

    @Override
    public PurchaseOrderDetailResponseDTO getDetail(Integer id) {
        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Đơn nhập hàng không tồn tại"));
        return mapToDetail(po);
    }

    private PurchaseOrderDetailResponseDTO mapToDetail(PurchaseOrder po) {
        List<PurchaseOrderDetailResponseDTO.DetailItem> items = po.getDetails().stream()
                .filter(d -> d.getActive())
                .map(d -> PurchaseOrderDetailResponseDTO.DetailItem.builder()
                        .id(d.getId())
                        .ingredientId(d.getIngredient().getId())
                        .ingredientName(d.getIngredient().getIngredientName())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .lineTotal(d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                        .build())
                .collect(Collectors.toList());
        return PurchaseOrderDetailResponseDTO.builder()
                .id(po.getId())
                .purchaseOrderCode(po.getPurchaseOrderCode())
                .totalAmount(po.getTotalAmount())
                .paymentStatus(po.getPaymentStatus())
                .accountFullName(po.getAccount().getFullName())
                .createdTime(po.getCreatedTime())
                .orderedAt(po.getOrderedAt())
                .details(items)
                .build();
    }

    private Account getCurrentAccount() {
        // Lấy từ SecurityContext, giả định method có sẵn
        return ServiceSupport.getCurrentAccount();
    }

    private void createActivity(Account account, String code, String type, String desc) {
        AccountActivity act = AccountActivity.builder()
                .activityCode(code)
                .account(account)
                .activityType(type)
                .activityDescription(desc)
                .createdTime(Instant.now())
                .active(true)
                .build();
        activityRepository.save(act);
    }

    private String generateNextCode() {
        long count = poRepository.count() + 1;
        return "PO-" + String.format("%06d", count);
    }
}
```

### 5.3. Các Repository bổ sung cần định nghĩa query method `findByPurchaseOrderIdAndActiveTrue` (cho detail)

```java
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Integer> {
    List<PurchaseOrderDetail> findByPurchaseOrderId(Integer purchaseOrderId);
    List<PurchaseOrderDetail> findByPurchaseOrderIdAndActiveTrue(Integer purchaseOrderId);
}
```

## 6. Controller

```java
package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.purchase_order.*;
import com.duyminhdev.cf_manager.dto.response.purchase_order.*;
import com.duyminhdev.cf_manager.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/purchase-order")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<PurchaseOrderListItemDTO>>> search(@Valid @RequestBody PurchaseOrderSearchRequestDTO request) {
        return new ApiResponse<>(200, "SEARCH_SUCCESS", service.search(request));
    }

    @PostMapping("/create")
    public ApiResponse<PurchaseOrderDetailResponseDTO> create(@Valid @RequestBody PurchaseOrderCreateRequestDTO request) {
        return new ApiResponse<>(201, "CREATE_SUCCESS", service.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<PurchaseOrderDetailResponseDTO> update(@Valid @RequestBody PurchaseOrderUpdateRequestDTO request) {
        return new ApiResponse<>(200, "UPDATE_SUCCESS", service.update(request));
    }

    @PostMapping("/status")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody PurchaseOrderStatusUpdateRequestDTO request) {
        service.updateStatus(request);
        return new ApiResponse<>(200, "STATUS_UPDATED");
    }

    @PostMapping("/detail")
    public ApiResponse<PurchaseOrderDetailResponseDTO> detail(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        return new ApiResponse<>(200, "DETAIL_SUCCESS", service.getDetail(id));
    }
}
```

## 7. Các lớp hỗ trợ giả định

- `ServiceSupport.getCurrentAccount()`: lấy Account từ SecurityContextHolder.
- `Ingredient` có field `averagePrice` và `shelfLife` (int days).
- `Warehouse`, `Supplier` entity đã có.

Vậy là chúng ta đã hoàn thành triển khai backend cho Quản lý Đơn nhập hàng, bám sát thiết kế business flow, sử dụng lại các pattern hiện có của dự án.