package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
    List<Warehouse> findAllByActiveTrueOrderByWarehouseNameAsc();
}
