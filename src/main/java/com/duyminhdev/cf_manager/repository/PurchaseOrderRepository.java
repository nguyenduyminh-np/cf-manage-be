// PurchaseOrderRepository.java
package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer>,
        JpaSpecificationExecutor<PurchaseOrder> {

    /** Fix #3: Lấy MAX(id) để sinh mã PO an toàn, tránh trùng khi có soft-delete */
    @Query("SELECT COALESCE(MAX(p.id), 0) FROM PurchaseOrder p")
    Long findMaxId();
}