package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderDetailNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderIngredientSelectNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderItemNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderSearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.purchase_order.*;
import com.duyminhdev.cf_manager.dto.response.purchase_order.*;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.enums.PurchaseOrderStatusEnum;
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

    // Bảng chuyển đổi trạng thái được quản lý trong PurchaseOrderStatusEnum.allowedTransitions()

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
                .map(r -> {
                    String statusCode = r.getPaymentStatus();
                    String statusName = PurchaseOrderStatusEnum.isValidCode(statusCode)
                            ? PurchaseOrderStatusEnum.fromCode(statusCode).getLabel()
                            : statusCode;
                    return PurchaseOrderListItemDTO.builder()
                            .id(r.getId())
                            .purchaseOrderCode(r.getPurchaseOrderCode())
                            .totalPrice(r.getTotalPrice())
                            .paymentStatus(statusCode)
                            .paymentStatusName(statusName)
                            .accountFullName(r.getFullName())
                            .supplierName(r.getSupplierName())
                            .orderDate(r.getOrderDate())
                            .createdTime(r.getCreatedTime())
                            .build();
                })
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
                        .ingredientCode(item.getIngredientCode())
                        .ingredientName(item.getIngredientName())
                        .supplierId(item.getSupplierId())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        String statusCode = header.getPaymentStatus();
        String statusName = PurchaseOrderStatusEnum.isValidCode(statusCode)
                ? PurchaseOrderStatusEnum.fromCode(statusCode).getLabel()
                : statusCode;

        return PurchaseOrderDetailResponseDTO.builder()
                .id(header.getId())
                .purchaseOrderCode(header.getPurchaseOrderCode())
                .totalPrice(header.getTotalPrice())
                .paymentStatus(statusCode)
                .paymentStatusName(statusName)
                .accountFullName(header.getFullName())
                .supplierId(header.getSupplierId())
                .supplierName(header.getSupplierName())
                .warehouseId(header.getWarehouseId())
                .warehouseName(header.getWarehouseName())
                .orderDate(header.getOrderDate())
                .createdTime(header.getCreatedTime())
                .details(detailItems)
                .build();
    }

    @Override
    public List<PurchaseOrderWarehouseSelectDTO> getDanhSachNhaKho() {
        return warehouseRepository.findAllByActiveTrueOrderByWarehouseNameAsc()
                .stream()
                .map(warehouse -> PurchaseOrderWarehouseSelectDTO.builder()
                        .id(warehouse.getId())
                        .warehouseCode(warehouse.getWarehouseCode())
                        .warehouseName(warehouse.getWarehouseName())
                        .build())
                .toList();
    }

    @Override
    public List<PurchaseOrderSupplierSelectDTO> getDanhSachNhaCungCap() {
        return supplierRepository.findAllByActiveTrueOrderBySupplierNameAsc()
                .stream()
                .map(supplier -> PurchaseOrderSupplierSelectDTO.builder()
                        .id(supplier.getId())
                        .supplierCode(supplier.getSupplierCode())
                        .supplierName(supplier.getSupplierName())
                        .build())
                .toList();
    }

    @Override
    public List<PurchaseOrderIngredientSelectDTO> getDanhSachNguyenLieuTheoNcc(Integer supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .orElseThrow(() -> new InvalidDataException("Nha cung cap khong ton tai hoac da vo hieu hoa"));

        List<PurchaseOrderIngredientSelectNativeResult> rows =
                nativeSqlPurchaseOrderRepository.findIngredientsBySupplierId(supplier.getId());

        return rows.stream()
                .map(row -> PurchaseOrderIngredientSelectDTO.builder()
                        .ingredientId(row.getIngredientId())
                        .ingredientCode(row.getIngredientCode())
                        .ingredientName(row.getIngredientName())
                        .supplierId(row.getSupplierId())
                        .build())
                .toList();
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

        // Validate status code hợp lệ trước
        if (request.getPaymentStatus() != null) {
            serviceSupport.validatePurchaseOrderStatusCode(request.getPaymentStatus());
        }
        // Phân quyền – nhân viên chỉ được tạo đơn ở DRAFT/PENDING
        PurchaseOrderStatusEnum effectiveStatusEnum = resolveInitialStatus(currentUser, request.getPaymentStatus());
        String effectiveStatus = effectiveStatusEnum.getCode();

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

        PurchaseOrderStatusEnum currentStatusEnum = PurchaseOrderStatusEnum.fromCode(po.getPaymentStatus());
        if (currentStatusEnum.isTerminal()) {
            throw new InvalidDataException("Không thể sửa đơn hàng ở trạng thái: " + currentStatusEnum.getLabel());
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
        if (request.getPaymentStatus() != null) {
            serviceSupport.validatePurchaseOrderStatusCode(request.getPaymentStatus());
            po.setPaymentStatus(PurchaseOrderStatusEnum.fromCode(request.getPaymentStatus()).getCode());
        }
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

        // Validate + parse cả hai trạng thái qua enum
        PurchaseOrderStatusEnum currentStatus = PurchaseOrderStatusEnum.fromCode(po.getPaymentStatus());
        serviceSupport.validatePurchaseOrderStatusCode(request.getNewStatus());
        PurchaseOrderStatusEnum targetStatus  = PurchaseOrderStatusEnum.fromCode(request.getNewStatus());

        if (currentStatus == targetStatus) return;

        // Kiểm soát chuyển đổi trạng thái hợp lệ qua enum
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new InvalidDataException(
                    "Không thể chuyển đơn hàng từ [" + currentStatus.getLabel()
                    + "] sang [" + targetStatus.getLabel() + "]");
        }

        Account currentUser = getCurrentAccount();

        switch (targetStatus) {
            case PENDING:
                po.setPaymentStatus(targetStatus.getCode());
                createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Đơn hàng chờ duyệt");
                break;
            case APPROVED:
                po.setPaymentStatus(targetStatus.getCode());
                createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Đơn hàng đã được duyệt");
                break;
            case CANCELLED:
                po.setPaymentStatus(targetStatus.getCode());
                createActivity(currentUser, String.valueOf(po.getId()), "purchase_order", "Đơn nhập hàng đã bị hủy");
                break;
            case COMPLETED:
                completeOrder(po, request, currentUser);
                break;
            default:
                throw new InvalidDataException("Trạng thái không hợp lệ: " + targetStatus.getCode());
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
    private PurchaseOrderStatusEnum resolveInitialStatus(Account account, String requestedStatus) {
        boolean isAdmin = account.getRole() != null
                && "ADMIN".equalsIgnoreCase(account.getRole().getRoleCode());

        // Không được tạo đơn ở trạng thái cuối
        PurchaseOrderStatusEnum requested = requestedStatus != null
                ? PurchaseOrderStatusEnum.fromCode(requestedStatus)
                : PurchaseOrderStatusEnum.DRAFT;

        if (requested.isTerminal()) {
            throw new InvalidDataException(
                    "Không thể tạo đơn hàng với trạng thái: " + requested.getLabel());
        }

        if (!isAdmin && !requested.isDraft() && !requested.isPending()) {
            // Nhân viên chỉ được DRAFT hoặc PENDING
            return PurchaseOrderStatusEnum.PENDING;
        }

        return requested;
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
        List<PurchaseOrderDetailResponseDTO.DetailItem> items = detailRepository.findByPurchaseOrderIdAndActiveTrue(po.getId()).stream()
                .map(d -> PurchaseOrderDetailResponseDTO.DetailItem.builder()
                        .id(d.getId())
                        .ingredientId(d.getIngredient().getId())
                        .ingredientCode(d.getIngredient().getIngredientCode())
                        .ingredientName(d.getIngredient().getIngredientName())
                        .supplierId(d.getIngredient().getSupplier() != null ? d.getIngredient().getSupplier().getId() : null)
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .lineTotal(d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        String statusCode = po.getPaymentStatus();
        String statusName = PurchaseOrderStatusEnum.isValidCode(statusCode)
                ? PurchaseOrderStatusEnum.fromCode(statusCode).getLabel()
                : statusCode;

        return PurchaseOrderDetailResponseDTO.builder()
                .id(po.getId())
                .purchaseOrderCode(po.getPurchaseOrderCode())
                .totalPrice(po.getTotalPrice())
                .paymentStatus(statusCode)
                .paymentStatusName(statusName)
                .accountFullName(po.getAccount().getFullName())
                .supplierId(po.getSupplier() != null ? po.getSupplier().getId() : null)
                .supplierName(po.getSupplier() != null ? po.getSupplier().getSupplierName() : null)
                .warehouseId(po.getWarehouse() != null ? po.getWarehouse().getId() : null)
                .warehouseName(po.getWarehouse() != null ? po.getWarehouse().getWarehouseName() : null)
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

    // ═══════════════════════════════════════════════════════════════════
    //  DELETE (soft-delete)
    // ═══════════════════════════════════════════════════════════════════
    @Override
    @Transactional
    public void delete(Integer id) {
        if (id == null) {
            throw new InvalidDataException("Id đơn nhập hàng không được trống");
        }

        PurchaseOrder po = poRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Đơn nhập hàng không tồn tại"));

        PurchaseOrderStatusEnum currentStatus = PurchaseOrderStatusEnum.fromCode(po.getPaymentStatus());

        // Chỉ cho phép xóa đơn ở trạng thái DRAFT
        if (!currentStatus.isDraft()) {
            throw new InvalidDataException(
                    "Chỉ có thể xóa đơn nhập hàng ở trạng thái Bản nháp (DRAFT). "
                    + "Đơn hiện tại đang ở: " + currentStatus.getLabel());
        }

        // Soft-delete toàn bộ detail trước
        List<PurchaseOrderDetail> details = detailRepository.findByPurchaseOrderId(po.getId());
        details.forEach(d -> d.setActive(false));
        detailRepository.saveAll(details);

        // Soft-delete đơn hàng
        po.setActive(false);
        poRepository.save(po);

        createActivity(getCurrentAccount(), String.valueOf(po.getId()), "purchase_order",
                "Xóa đơn nhập hàng " + po.getPurchaseOrderCode());
    }
}
