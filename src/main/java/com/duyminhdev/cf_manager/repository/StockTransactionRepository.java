package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, Integer> {

    /** Fix #4: Kiểm tra đã tồn tại giao dịch nhập kho cho PO này chưa (idempotent) */
    boolean existsByStockTransactionCode(String stockTransactionCode);

    /** Fix #2: Tổng số lượng tồn kho hiện tại của một nguyên liệu (dùng tính bình quân gia quyền) */
    @Query("SELECT COALESCE(SUM(sl.quantity), 0) FROM StockLevel sl WHERE sl.ingredient.id = :ingredientId AND sl.active = true")
    Integer sumQuantityByIngredientId(Integer ingredientId);

    @Query("SELECT COUNT(st) FROM StockTransaction st " +
           "WHERE st.warehouse.id = :warehouseId AND st.status NOT IN ('completed','cancelled')")
    long countPendingTransactions(@Param("warehouseId") Integer warehouseId);
}