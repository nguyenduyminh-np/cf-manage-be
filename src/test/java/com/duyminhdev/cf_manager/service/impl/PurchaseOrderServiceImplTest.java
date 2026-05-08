package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderDetailNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderIngredientSelectNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderItemNativeResult;
import com.duyminhdev.cf_manager.dto.response.purchase_order.PurchaseOrderDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.response.purchase_order.PurchaseOrderIngredientSelectDTO;
import com.duyminhdev.cf_manager.dto.response.purchase_order.PurchaseOrderSupplierSelectDTO;
import com.duyminhdev.cf_manager.dto.response.purchase_order.PurchaseOrderWarehouseSelectDTO;
import com.duyminhdev.cf_manager.entity.Supplier;
import com.duyminhdev.cf_manager.entity.Warehouse;
import com.duyminhdev.cf_manager.repository.AccountActivityRepository;
import com.duyminhdev.cf_manager.repository.CashFlowRepository;
import com.duyminhdev.cf_manager.repository.DebtRepository;
import com.duyminhdev.cf_manager.repository.IngredientRepository;
import com.duyminhdev.cf_manager.repository.PurchaseOrderDetailRepository;
import com.duyminhdev.cf_manager.repository.PurchaseOrderRepository;
import com.duyminhdev.cf_manager.repository.StockLevelRepository;
import com.duyminhdev.cf_manager.repository.StockTransactionDetailRepository;
import com.duyminhdev.cf_manager.repository.StockTransactionRepository;
import com.duyminhdev.cf_manager.repository.SupplierRepository;
import com.duyminhdev.cf_manager.repository.WarehouseRepository;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlPurchaseOrderRepository;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private PurchaseOrderDetailRepository purchaseOrderDetailRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private StockTransactionRepository stockTransactionRepository;

    @Mock
    private StockLevelRepository stockLevelRepository;

    @Mock
    private StockTransactionDetailRepository stockTransactionDetailRepository;

    @Mock
    private DebtRepository debtRepository;

    @Mock
    private CashFlowRepository cashFlowRepository;

    @Mock
    private AccountActivityRepository accountActivityRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private NativeSqlPurchaseOrderRepository nativeSqlPurchaseOrderRepository;

    @Mock
    private ServiceSupport serviceSupport;

    private PurchaseOrderServiceImpl purchaseOrderService;

    @BeforeEach
    void setUp() {
        purchaseOrderService = new PurchaseOrderServiceImpl(
                purchaseOrderRepository,
                purchaseOrderDetailRepository,
                ingredientRepository,
                stockTransactionRepository,
                stockLevelRepository,
                stockTransactionDetailRepository,
                debtRepository,
                cashFlowRepository,
                accountActivityRepository,
                supplierRepository,
                warehouseRepository,
                nativeSqlPurchaseOrderRepository,
                serviceSupport
        );
    }

    @Test
    void getDanhSachNhaKho_shouldMapIdCodeAndName() {
        when(warehouseRepository.findAllByActiveTrueOrderByWarehouseNameAsc()).thenReturn(List.of(
                Warehouse.builder().id(2).warehouseCode("WH-002").warehouseName("Kho B").active(true).build(),
                Warehouse.builder().id(1).warehouseCode("WH-001").warehouseName("Kho A").active(true).build()
        ));

        List<PurchaseOrderWarehouseSelectDTO> result = purchaseOrderService.getDanhSachNhaKho();

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getId());
        assertEquals("WH-002", result.get(0).getWarehouseCode());
        assertEquals("Kho B", result.get(0).getWarehouseName());
    }

    @Test
    void getDanhSachNhaCungCap_shouldMapIdCodeAndName() {
        when(supplierRepository.findAllByActiveTrueOrderBySupplierNameAsc()).thenReturn(List.of(
                Supplier.builder().id(7).supplierCode("SUP-007").supplierName("NCC A").active(true).build()
        ));

        List<PurchaseOrderSupplierSelectDTO> result = purchaseOrderService.getDanhSachNhaCungCap();

        assertEquals(1, result.size());
        assertEquals(7, result.get(0).getId());
        assertEquals("SUP-007", result.get(0).getSupplierCode());
        assertEquals("NCC A", result.get(0).getSupplierName());
    }

    @Test
    void getDanhSachNguyenLieuTheoNcc_shouldMapIngredientFieldsBySupplier() {
        Integer supplierId = 5;
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(
                Supplier.builder().id(supplierId).supplierCode("SUP-005").supplierName("NCC 5").active(true).build()
        ));
        when(nativeSqlPurchaseOrderRepository.findIngredientsBySupplierId(supplierId)).thenReturn(List.of(
                PurchaseOrderIngredientSelectNativeResult.builder()
                        .ingredientId(11)
                        .ingredientCode("ING-011")
                        .ingredientName("Ca phe")
                        .supplierId(supplierId)
                        .build()
        ));

        List<PurchaseOrderIngredientSelectDTO> result = purchaseOrderService.getDanhSachNguyenLieuTheoNcc(supplierId);

        assertEquals(1, result.size());
        assertEquals(11, result.get(0).getIngredientId());
        assertEquals("ING-011", result.get(0).getIngredientCode());
        assertEquals("Ca phe", result.get(0).getIngredientName());
        assertEquals(supplierId, result.get(0).getSupplierId());
    }

    @Test
    void getDetail_shouldIncludeSupplierWarehouseAndIngredientJoinFields() {
        Integer orderId = 9;
        Instant now = Instant.parse("2026-05-06T10:15:30Z");

        when(nativeSqlPurchaseOrderRepository.findDetailById(orderId)).thenReturn(Optional.of(
                PurchaseOrderDetailNativeResult.builder()
                        .id(orderId)
                        .purchaseOrderCode("PO-000009")
                        .totalPrice(BigDecimal.valueOf(24000))
                        .paymentStatus("DRAFT")
                        .orderDate(now)
                        .createdTime(now)
                        .accountId(3)
                        .fullName("Nguyen Van A")
                        .supplierId(5)
                        .supplierName("NCC 5")
                        .warehouseId(2)
                        .warehouseName("Kho tong")
                        .build()
        ));
        when(nativeSqlPurchaseOrderRepository.findItemsByOrderId(orderId)).thenReturn(List.of(
                PurchaseOrderItemNativeResult.builder()
                        .detailId(100)
                        .ingredientId(11)
                        .ingredientCode("ING-011")
                        .ingredientName("Ca phe")
                        .supplierId(5)
                        .quantity(2)
                        .unitPrice(BigDecimal.valueOf(12000))
                        .build()
        ));

        PurchaseOrderDetailResponseDTO result = purchaseOrderService.getDetail(orderId);

        assertEquals(5, result.getSupplierId());
        assertEquals("NCC 5", result.getSupplierName());
        assertEquals(2, result.getWarehouseId());
        assertEquals("Kho tong", result.getWarehouseName());
        assertEquals(1, result.getDetails().size());
        assertEquals(11, result.getDetails().get(0).getIngredientId());
        assertEquals("ING-011", result.getDetails().get(0).getIngredientCode());
        assertEquals("Ca phe", result.getDetails().get(0).getIngredientName());
        assertEquals(5, result.getDetails().get(0).getSupplierId());
        assertEquals(BigDecimal.valueOf(24000), result.getDetails().get(0).getLineTotal());
    }
}
