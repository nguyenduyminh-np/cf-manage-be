package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient,Integer> {
	Optional<Ingredient> findByIngredientCode(String ingredientCode);

    boolean existsByIngredientCategoryIdAndActiveTrue(Integer categoryId);
}
