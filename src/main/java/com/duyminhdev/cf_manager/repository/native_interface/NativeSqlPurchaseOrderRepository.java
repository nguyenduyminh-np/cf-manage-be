package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderSearchNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderDetailNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderIngredientSelectNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.PurchaseOrderItemNativeResult;
import com.duyminhdev.cf_manager.dto.request.purchase_order.PurchaseOrderSearchRequestDTO;
import java.util.List;
import java.util.Optional;

public interface NativeSqlPurchaseOrderRepository {
    List<PurchaseOrderSearchNativeResult> search(PurchaseOrderSearchRequestDTO request, int offset, int limit);
    long count(PurchaseOrderSearchRequestDTO request);
    Optional<PurchaseOrderDetailNativeResult> findDetailById(Integer id);
    List<PurchaseOrderItemNativeResult> findItemsByOrderId(Integer orderId);
    List<PurchaseOrderIngredientSelectNativeResult> findIngredientsBySupplierId(Integer supplierId);
}
