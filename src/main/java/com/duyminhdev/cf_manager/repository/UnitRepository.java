package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Integer> {
    List<Unit> findAllByActiveTrueOrderByUnitNameAsc();

    Optional<Unit> findByIdAndActiveTrue(Integer id);

    long countByActiveTrue();
}
