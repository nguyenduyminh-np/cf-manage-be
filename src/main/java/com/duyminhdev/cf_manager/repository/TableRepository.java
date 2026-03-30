package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Integer> {
    Optional<TableEntity> findByIdAndActiveTrue(Integer id);
    Optional<TableEntity> findByIdAndActiveIsTrue(Integer id);
    List<TableEntity> findAllByActiveTrueOrderByFloorAscSlotAscIdAsc();
    boolean existsByIdAndActiveTrue(Integer id);
}
