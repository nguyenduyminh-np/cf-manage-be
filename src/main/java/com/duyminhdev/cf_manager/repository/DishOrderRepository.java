package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.DishOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishOrderRepository extends JpaRepository<DishOrder, Integer> {

    @EntityGraph(attributePaths = {"table", "account", "status"})
    List<DishOrder> findAllByTableIdAndActiveTrueOrderByCreatedTimeDesc(Integer tableId);

    @EntityGraph(attributePaths = {"table", "account", "status"})
    Optional<DishOrder> findByIdAndActiveTrue(Integer id);
}
