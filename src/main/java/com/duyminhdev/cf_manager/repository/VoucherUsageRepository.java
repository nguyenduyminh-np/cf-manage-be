package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.VoucherUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, Integer> {

    boolean existsByVoucherIdAndDishOrderId(Integer voucherId, Integer dishOrderId);

    /**
     * Xóa bản ghi usage khi hoàn voucher (hủy đơn).
     */
    @Modifying
    @Query("DELETE FROM VoucherUsage vu WHERE vu.dishOrder.id = :dishOrderId")
    void deleteByDishOrderId(@Param("dishOrderId") Integer dishOrderId);
}
