package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, Integer> {

    /** Fix #6: Tìm lô tồn kho hiện có cùng nguyên liệu + kho để gộp số lượng */
    Optional<StockLevel> findByIngredientIdAndWarehouseIdAndActiveTrue(
            Integer ingredientId, Integer warehouseId);

    /** Fix #2: Tổng số lượng của một nguyên liệu trong tất cả kho (dùng tính averagePrice) */
    @Query("SELECT COALESCE(SUM(sl.quantity), 0) FROM StockLevel sl WHERE sl.ingredient.id = :ingredientId AND sl.active = true")
    Integer sumQuantityByIngredientId(Integer ingredientId);

    List<StockLevel> findByIngredientIdAndActiveTrueAndExpirationDateAfterOrderByExpirationDate(
            Integer ingredientId, Instant now);

    @Query("SELECT COALESCE(SUM(sl.quantity), 0) FROM StockLevel sl " +
           "WHERE sl.ingredient.id = :ingredientId " +
           "AND sl.active = true " +
           "AND sl.expirationDate > CURRENT_TIMESTAMP")
    Long sumAvailableQuantityByIngredientId(@Param("ingredientId") Integer ingredientId);

    @Query("SELECT COALESCE(SUM(sl.quantity), 0) FROM StockLevel sl " +
           "WHERE sl.warehouse.id = :warehouseId AND sl.active = true AND sl.quantity > 0")
    long sumQuantityByWarehouse(@Param("warehouseId") Integer warehouseId);
}