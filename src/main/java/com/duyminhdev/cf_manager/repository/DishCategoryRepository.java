package com.duyminhdev.cf_manager.repository;
import com.duyminhdev.cf_manager.entity.DishCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DishCategoryRepository extends JpaRepository<DishCategory, Integer> {

    List<DishCategory> findAllByActiveTrueOrderByDishCategoryNameAsc();
    Optional<DishCategory> findByIdAndActiveTrue(Integer id);
    long countByActiveTrue();
}
