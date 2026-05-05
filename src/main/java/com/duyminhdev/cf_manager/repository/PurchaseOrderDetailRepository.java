package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, Integer> {
    List<PurchaseOrderDetail> findByPurchaseOrderId(Integer purchaseOrderId);
    List<PurchaseOrderDetail> findByPurchaseOrderIdAndActiveTrue(Integer purchaseOrderId);
}