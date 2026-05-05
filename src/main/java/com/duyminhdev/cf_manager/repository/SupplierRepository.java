package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Integer>,
        JpaSpecificationExecutor<Supplier> {

    List<Supplier> findAllByActiveTrueOrderBySupplierNameAsc();
}