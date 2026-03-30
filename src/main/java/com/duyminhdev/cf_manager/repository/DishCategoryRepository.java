package com.duyminhdev.cf_manager.repository;
import com.duyminhdev.cf_manager.entity.DishCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishCategoryRepository extends JpaRepository<DishCategory, Integer> {

    List<DishCategory> findAllByActiveTrueOrderByDishCategoryNameAsc();
}
