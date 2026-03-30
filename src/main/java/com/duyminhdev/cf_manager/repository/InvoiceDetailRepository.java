package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.InvoiceDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Integer> {

    @EntityGraph(attributePaths = {"invoice", "dish"})
    List<InvoiceDetail> findAllByInvoiceId(Integer invoiceId);
}
