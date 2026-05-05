package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientCategoryRepository extends JpaRepository<IngredientCategory, Integer> {
    List<IngredientCategory> findAllByActiveTrueOrderByIngredientCategoryNameAsc();

    Optional<IngredientCategory> findByIdAndActiveTrue(Integer id);

    long countByActiveTrue();

    Optional<IngredientCategory> findByIngredientCategoryCode(String code);

    boolean existsByParentCategoryIdAndActiveTrue(Integer parentCategoryId);
}
