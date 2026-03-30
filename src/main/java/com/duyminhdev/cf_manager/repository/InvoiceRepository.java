package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Invoice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    @EntityGraph(attributePaths = {"table", "account"})
    Optional<Invoice> findByIdAndActiveTrue(Integer id);

    long countByActiveTrue();
}
