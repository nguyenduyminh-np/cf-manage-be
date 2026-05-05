package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderDetailNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderItemNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderSearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.purchase_order.*;
import com.duyminhdev.cf_manager.dto.response.purchase_order.*;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlPurchaseOrderRepository;
import com.duyminhdev.cf_manager.service.PurchaseOrderService;
import com.duyminhdev.cf_manager.utils.PageUtils;
import com.duyminhdev.cf_manager.utils.ServiceSupport;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    // ───────────────────────────────────────── Fix #7: Bảng chuyển đổi trạng thái hợp lệ
    private static final Map<String, Set<String>> VALID_TRANSITIONS = Map.of(
            "DRAFT",     Set.of("PENDING", "APPROVED"),
            "PENDING",   Set.of("APPROVED", "CANCELLED"),
            "APPROVED",  Set.of("COMPLETED", "CANCELLED"),
            "COMPLETED", Set.of(),
            "CANCELLED", Set.of()
    );

    private final PurchaseOrderRepository        poRepository;
    private final PurchaseOrderDetailRepository  detailRepository;
    private final IngredientRepository           ingredientRepository;
    private final StockTransactionRepository     stockTxRepository;
    private final StockLevelRepository           stockLevelRepository;
    private final StockTransactionDetailRepository stockTxDetailRepository;
    private final DebtRepository                 debtRepository;
    private final CashFlowRepository             cashFlowRepository;
    private final AccountActivityRepository      activityRepository;
    private final SupplierRepository             supplierRepository;
    private final WarehouseRepository            warehouseRepository;
    private final NativeSqlPurchaseOrderRepository nativeSqlPurchaseOrderRepository;
    private final ServiceSupport                 serviceSupport;

    // ═══════════════════════════════════════════════════════════════════
    //  SEARCH
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public PageResponse<List<PurchaseOrderListItemDTO>> search(PurchaseOrderSearchRequestDTO request) {
        int page   = PageUtils.normalizePage(request.getPage());
        int limit  = PageUtils.normalizeLimit(request.getLimit());
        int offset = page * limit;

        List<PurchaseOrderSearchNativeResult> rows = nativeSqlPurchaseOrderRepository.search(request, offset, limit);
        long total = nativeSqlPurchaseOrderRepository.count(request);

        List<PurchaseOrderListItemDTO> list = rows.stream()
                .map(r -> PurchaseOrderListItemDTO.builder()
                        .id(r.getId())
                        .purchaseOrderCode(r.getPurchaseOrderCode())
                        .totalPrice(r.getTotalPrice())
                        .paymentStatus(r.getPaymentStatus())
                        .accountFullName(r.getFullName())
                        .supplierName(r.getSupplierName())       // Fix #1
                        .orderDate(r.getOrderDate())
                        .createdTime(r.getCreatedTime())
                        .build())
                .toList();

        PageResponse<List<PurchaseOrderListItemDTO>> resp = new PageResponse<>();
        resp.setRows(list);
        resp.setPageNo(page);
        resp.setPageSize(limit);
        resp.setTotalElements((int) total);
        resp.setTotalPages((int) Math.ceil((double) total / limit));
        return resp;
    }

    @Override
    public List<PurchaseOrderExportDTO> exportData(PurchaseOrderSearchRequestDTO request) {
        long total = nativeSqlPurchaseOrderRepository.count(request);
        if (total <= 0) {
            return List.of();
        }

        List<PurchaseOrderSearchNativeResult> rows = nativeSqlPurchaseOrderRepository.search(request, 0, (int) total);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return rows.stream()
                .map(row -> PurchaseOrderExportDTO.builder()
                        .purchaseOrderCode(row.getPurchaseOrderCode())
                        .totalPrice(row.getTotalPrice())
                        .paymentStatus(row.getPaymentStatus())
                        .accountFullName(row.getFullName())
                        .supplierName(row.getSupplierName())
                        .orderDate(row.getOrderDate() != null ? formatter.format(row.getOrderDate()) : null)
                        .createdTime(row.getCreatedTime() != null ? formatter.format(row.getCreatedTime()) : null)
                        .build())
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  GET DETAIL
    // ═══════════════════════════════════════════════════════════════════
    @Override
    public PurchaseOrderDetailResponseDTO getDetail(Integer id) {
        PurchaseOrderDetailNativeResult header = nativeSqlPurchaseOrderRepository.findDetailById(id)
                .orElseThrow(() -> new InvalidDataException("Đơn nhập hàng không tồn tại"));
        List<PurchaseOrderItemNativeResult> items = nativeSqlPurchaseOrderRepository.findItemsByOrderId(id);

        List<PurchaseOrderDetailResponseDTO.DetailItem> detailItems = items.stream()
                .map(item -> PurchaseOrderDetailResponseDTO.DetailItem.builder()
                        .id(item.getDetailId())
                        .ingredientId(item.getIngredientId())
                        .ingredientName(item.getIngredientName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return PurchaseOrderDetailResponseDTO.builder()
                .id(header.getId())
                .purchaseOrderCode(header.getPurchaseOrderCode())
                .totalPrice(header.getTotalPrice())
                .paymentStatus(header.getPaymentStatus())
                .accountFullName(header.getFullName())
                .supplierName(header.getSupplierName())          // Fix #1
                .warehouseName(header.getWarehouseName())        // Fix #1
                .orderDate(header.getOrderDate())
                .createdTime(header.getCreatedTime())
                .details(detailItems)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public PurchaseOrderDetailResponseDTO create(PurchaseOrderCreateRequestDTO request) {
        Account currentUser = getCurrentAccount();

        // Fix #1: Resolve supplier + warehouse
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new InvalidDataException("Nhà cung cấp không tồn tại"));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new InvalidDataException("Kho nhận hàng không tồn tại"));

        // Fix #8: Phân quyền – nhân viên chỉ được tạo đơn ở DRAFT/PENDING
        String effectiveStatus = resolveInitialStatus(currentUser, request.getPaymentStatus());

        // Fix #3: Sinh mã PO an toàn (dùng MAX id thay vì count)
        PurchaseOrder po = PurchaseOrder.builder()
                .purchaseOrderCode(generateNextCode())
                .totalPrice(BigDecimal.ZERO)    // sẽ tính lại từ details bên dưới
                .paymentStatus(effectiveStatus)
                .createdTime(Instant.now())
                .active(true)
                .account(currentUser)
                .supplier(supplier)             // Fix #1
                .warehouse(warehouse)           // Fix #1
                .orderDate(request.getOrderDate())
                .build();

        po = poRepository.save(po);

        // Fix #10: Validate + xây dựng details, tính tổng thực tế
        List<PurchaseOrderDetail> details = buildDetails(po, request.getDetails());
        detailRepository.saveAll(details);

        // Fix #10: Tính lại totalPrice từ detail (bỏ qua giá trị client gửi)
        BigDecimal computedTotal = computeTotal(details);
        po.setTotalPrice(computedTotal);
        po.setDetails(details);
        po = poRepository.save(po);

        createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Tạo đơn nhập hàng");
        return mapToDetail(po);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public PurchaseOrderDetailResponseDTO update(PurchaseOrderUpdateRequestDTO request) {
        PurchaseOrder po = poRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Đơn nhập hàng không tồn tại"));

        if (Set.of("COMPLETED", "CANCELLED").contains(po.getPaymentStatus())) {
            throw new InvalidDataException("Không thể sửa đơn hàng đã hoàn thành hoặc đã hủy");
        }

        // Fix #1: Cập nhật supplier/warehouse nếu client gửi
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new InvalidDataException("Nhà cung cấp không tồn tại"));
            po.setSupplier(supplier);
        }
        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new InvalidDataException("Kho nhận hàng không tồn tại"));
            po.setWarehouse(warehouse);
        }
        if (request.getPaymentStatus() != null) po.setPaymentStatus(request.getPaymentStatus());
        if (request.getOrderDate() != null)      po.setOrderDate(request.getOrderDate());

        // Fix #5: Merge details theo id (thay vì xóa hết rồi tạo mới)
        if (request.getDetails() != null) {
            List<PurchaseOrderDetail> existingDetails =
                    detailRepository.findByPurchaseOrderId(po.getId());

            Map<Integer, PurchaseOrderDetail> existingMap = existingDetails.stream()
                    .filter(d -> d.getId() != null)
                    .collect(Collectors.toMap(PurchaseOrderDetail::getId, Function.identity()));

            List<PurchaseOrderDetail> updatedDetails = new ArrayList<>();
            for (var dto : request.getDetails()) {
                // Fix #10: Validate
                validateDetailItem(dto.getQuantity(), dto.getUnitPrice());

                if (dto.getId() != null && existingMap.containsKey(dto.getId())) {
                    // Cập nhật dòng đã có
                    PurchaseOrderDetail d = existingMap.remove(dto.getId());
                    Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
                            .filter(i -> Boolean.TRUE.equals(i.getActive()))
                            .orElseThrow(() -> new InvalidDataException("Nguyên liệu không hợp lệ hoặc đã vô hiệu hóa"));
                    d.setIngredient(ingredient);
                    d.setQuantity(dto.getQuantity());
                    d.setUnitPrice(dto.getUnitPrice());
                    d.setActive(true);
                    updatedDetails.add(d);
                } else {
                    // Tạo dòng mới
                    Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
                            .filter(i -> Boolean.TRUE.equals(i.getActive()))
                            .orElseThrow(() -> new InvalidDataException("Nguyên liệu không hợp lệ hoặc đã vô hiệu hóa"));
                    updatedDetails.add(PurchaseOrderDetail.builder()
                            .purchaseOrder(po)
                            .ingredient(ingredient)
                            .quantity(dto.getQuantity())
                            .unitPrice(dto.getUnitPrice())
                            .createdTime(Instant.now())
                            .active(true)
                            .build());
                }
            }
            // Soft-delete các dòng bị bỏ
            existingMap.values().forEach(d -> d.setActive(false));
            detailRepository.saveAll(existingMap.values());
            detailRepository.saveAll(updatedDetails);

            // Fix #10: Tính lại tổng tiền
            po.setTotalPrice(computeTotal(updatedDetails));
            po.setDetails(updatedDetails);
        }

        po = poRepository.save(po);
        createActivity(getCurrentAccount(), String.valueOf(po.getId()), "purchase_order", "Cập nhật đơn nhập hàng");
        return mapToDetail(po);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  UPDATE STATUS
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public void updateStatus(PurchaseOrderStatusUpdateRequestDTO request) {
        PurchaseOrder po = poRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Đơn nhập hàng không tồn tại"));

        String currentStatus = po.getPaymentStatus();
        String newStatus     = request.getNewStatus();

        if (currentStatus.equals(newStatus)) return;

        // Fix #7: Kiểm soát chuyển đổi trạng thái hợp lệ
        Set<String> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidDataException(
                    "Không thể chuyển đơn hàng từ trạng thái [" + currentStatus + "] sang [" + newStatus + "]");
        }

        Account currentUser = getCurrentAccount();

        switch (newStatus) {
            case "PENDING":
                po.setPaymentStatus(newStatus);
                createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Đơn hàng chờ duyệt");
                break;
            case "APPROVED":
                po.setPaymentStatus(newStatus);
                createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Đơn hàng đã được duyệt");
                break;
            case "CANCELLED":
                po.setPaymentStatus(newStatus);
                createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Đơn nhập hàng đã bị hủy");
                break;
            case "COMPLETED":
                completeOrder(po, request, currentUser);
                break;
            default:
                throw new InvalidDataException("Trạng thái không hợp lệ: " + newStatus);
        }
        poRepository.save(po);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  COMPLETE ORDER (private)
    // ═══════════════════════════════════════════════════════════════════
    private void completeOrder(PurchaseOrder po,
                               PurchaseOrderStatusUpdateRequestDTO request,
                               Account currentUser) {
        // Fix #1: Supplier lấy từ PO, không nhận từ request nữa
        if (po.getSupplier() == null) {
            throw new InvalidDataException("Đơn hàng thiếu thông tin nhà cung cấp, vui lòng cập nhật trước");
        }

        // Warehouse: ưu tiên request (có thể override), fallback về warehouse của PO
        Warehouse warehouse;
        if (request.getWarehouseId() != null) {
            warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new InvalidDataException("Kho không tồn tại"));
        } else if (po.getWarehouse() != null) {
            warehouse = po.getWarehouse();
        } else {
            throw new InvalidDataException("Cần chỉ định kho nhận hàng");
        }

        Supplier supplier = po.getSupplier();
        String txCode = "TX-IN-AUTO-" + po.getPurchaseOrderCode();

        // Fix #4: Idempotent – kiểm tra đã tồn tại StockTransaction chưa
        if (stockTxRepository.existsByStockTransactionCode(txCode)) {
            throw new InvalidDataException("Đơn hàng này đã được xử lý nhập kho, vui lòng kiểm tra lại");
        }

        // 1. Cập nhật trạng thái PO
        po.setPaymentStatus("COMPLETED");

        // 2. Tạo StockTransaction IMPORT
        StockTransaction st = StockTransaction.builder()
                .stockTransactionCode(txCode)
                .transactionType("IMPORT")
                .status("COMPLETED")
                .totalMoney(po.getTotalPrice())
                .warehouse(warehouse)
                .account(currentUser)
                .createdTime(Instant.now())
                .transactionDate(Instant.now())
                .note("Hệ thống nhập kho tự động cho đơn nhập hàng " + po.getPurchaseOrderCode())
                .active(true)
                // Fix #9: Audit trail
                .completedDate(Instant.now())
                .completedBy(currentUser.getId())
                .build();
        stockTxRepository.save(st);

        // 3. Xử lý từng dòng chi tiết
        List<PurchaseOrderDetail> details = detailRepository.findByPurchaseOrderIdAndActiveTrue(po.getId());
        final Warehouse finalWarehouse = warehouse;
        for (PurchaseOrderDetail detail : details) {
            Ingredient ingredient = detail.getIngredient();

            // Fix #2: Tính averagePrice theo bình quân gia quyền (Weighted Average)
            Integer totalOldQty = stockLevelRepository.sumQuantityByIngredientId(ingredient.getId());
            if (totalOldQty == null) totalOldQty = 0;

            BigDecimal oldAvg = ingredient.getAveragePrice() != null
                    ? ingredient.getAveragePrice() : BigDecimal.ZERO;
            BigDecimal totalOldValue = oldAvg.multiply(BigDecimal.valueOf(totalOldQty));
            BigDecimal newValue = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
            int newTotalQty = totalOldQty + detail.getQuantity();

            BigDecimal newAvg = newTotalQty > 0
                    ? totalOldValue.add(newValue).divide(BigDecimal.valueOf(newTotalQty), 2, RoundingMode.HALF_UP)
                    : detail.getUnitPrice();
            ingredient.setAveragePrice(newAvg);
            ingredientRepository.save(ingredient);

            // Fix #6: Gộp lô – tìm lô hiện có cùng ingredient + warehouse
            Optional<StockLevel> existingLot =
                    stockLevelRepository.findByIngredientIdAndWarehouseIdAndActiveTrue(
                            ingredient.getId(), finalWarehouse.getId());

            StockLevel sl;
            if (existingLot.isPresent()) {
                // Gộp vào lô cũ: cộng số lượng, tính lại unitPrice theo bình quân gia quyền
                sl = existingLot.get();
                int oldQty = sl.getQuantity();
                BigDecimal mergedPrice = sl.getUnitPrice().multiply(BigDecimal.valueOf(oldQty))
                        .add(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                        .divide(BigDecimal.valueOf(oldQty + detail.getQuantity()), 2, RoundingMode.HALF_UP);
                sl.setQuantity(oldQty + detail.getQuantity());
                sl.setUnitPrice(mergedPrice);
                sl.setLastUpdatedTime(Instant.now());
            } else {
                // Tạo lô mới
                sl = StockLevel.builder()
                        .ingredient(ingredient)
                        .warehouse(finalWarehouse)
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .expirationDate(Instant.now().plusSeconds((long) ingredient.getSelfLife() * 86400L))
                        .createdTime(Instant.now())
                        .lastUpdatedTime(Instant.now())
                        .active(true)
                        .build();
            }
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
                .debtName("Nợ nhập hàng " + po.getPurchaseOrderCode()
                        + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .totalMoney(po.getTotalPrice())
                .isPaId(false)
                .note("Nợ nhập hàng " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .createdTime(Instant.now())
                .active(true)
                .supplier(supplier)
                .build();
        debtRepository.save(debt);

        // 5. Tạo CashFlow DEBT
        CashFlow cf = CashFlow.builder()
                .totalMoney(po.getTotalPrice())
                .flowType("DEBT")
                .note("Nợ nhập hàng " + po.getPurchaseOrderCode()
                        + " - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .account(currentUser)
                .createdTime(Instant.now())
                .active(true)
                .build();
        cashFlowRepository.save(cf);

        // 6. Ghi activity
        createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Đơn nhập hàng đã hoàn thành");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Fix #8: Phân quyền trạng thái tạo đơn.
     * Admin được phép giữ trạng thái hợp lệ bất kỳ (trừ COMPLETED/CANCELLED khi tạo mới).
     * Nhân viên chỉ được tạo ở DRAFT hoặc PENDING.
     */
    private String resolveInitialStatus(Account account, String requestedStatus) {
        boolean isAdmin = account.getRole() != null
                && "ADMIN".equalsIgnoreCase(account.getRole().getRoleCode());

        Set<String> forbidden = Set.of("COMPLETED", "CANCELLED");
        if (forbidden.contains(requestedStatus)) {
            throw new InvalidDataException("Không thể tạo đơn hàng với trạng thái: " + requestedStatus);
        }

        if (!isAdmin) {
            // Nhân viên chỉ được DRAFT hoặc PENDING
            if (!"DRAFT".equals(requestedStatus) && !"PENDING".equals(requestedStatus)) {
                return "PENDING";
            }
        }
        return requestedStatus != null ? requestedStatus : "DRAFT";
    }

    /** Fix #3: Sinh mã PO an toàn dùng MAX(id). */
    private String generateNextCode() {
        Long maxId = poRepository.findMaxId();
        long nextNumber = (maxId == null ? 0L : maxId) + 1L;
        return "PO-" + String.format("%06d", nextNumber);
    }

    /** Fix #10: Validate quantity > 0, unitPrice > 0, ingredient active. */
    private void validateDetailItem(Integer quantity, BigDecimal unitPrice) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidDataException("Số lượng phải lớn hơn 0");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Đơn giá phải lớn hơn 0");
        }
    }

    /** Fix #10: Xây dựng danh sách PurchaseOrderDetail từ request, validate từng dòng. */
    private List<PurchaseOrderDetail> buildDetails(PurchaseOrder po,
                                                   List<PurchaseOrderCreateRequestDTO.Detail> dtos) {
        List<PurchaseOrderDetail> result = new ArrayList<>();
        for (var dto : dtos) {
            validateDetailItem(dto.getQuantity(), dto.getUnitPrice());
            Ingredient ingredient = ingredientRepository.findById(dto.getIngredientId())
                    .filter(i -> Boolean.TRUE.equals(i.getActive()))
                    .orElseThrow(() -> new InvalidDataException(
                            "Nguyên liệu không hợp lệ hoặc đã vô hiệu hóa: id=" + dto.getIngredientId()));
            result.add(PurchaseOrderDetail.builder()
                    .purchaseOrder(po)
                    .ingredient(ingredient)
                    .quantity(dto.getQuantity())
                    .unitPrice(dto.getUnitPrice())
                    .createdTime(Instant.now())
                    .active(true)
                    .build());
        }
        return result;
    }

    /** Fix #10: Tính tổng tiền từ danh sách details (chỉ tính dòng active). */
    private BigDecimal computeTotal(List<PurchaseOrderDetail> details) {
        return details.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Map PurchaseOrder entity sang DTO (dùng sau create/update khi đã có details trong memory). */
    private PurchaseOrderDetailResponseDTO mapToDetail(PurchaseOrder po) {
        List<PurchaseOrderDetailResponseDTO.DetailItem> items = po.getDetails().stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
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
                .totalPrice(po.getTotalPrice())
                .paymentStatus(po.getPaymentStatus())
                .accountFullName(po.getAccount().getFullName())
                .supplierName(po.getSupplier() != null ? po.getSupplier().getSupplierName() : null)  // Fix #1
                .warehouseName(po.getWarehouse() != null ? po.getWarehouse().getWarehouseName() : null) // Fix #1
                .createdTime(po.getCreatedTime())
                .orderDate(po.getOrderDate())
                .details(items)
                .build();
    }

    private Account getCurrentAccount() {
        return serviceSupport.getCurrentAccount();
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
}