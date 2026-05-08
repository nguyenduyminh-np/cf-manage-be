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

    /** Tìm theo mã – không phân biệt is_active (global unique check). */
    Optional<IngredientCategory> findByIngredientCategoryCode(String code);

    /**
     * Tìm theo mã nhưng loại trừ một ID cụ thể (dùng khi update để kiểm tra
     * trùng mã với các bản ghi khác, kể cả inactive).
     */
    Optional<IngredientCategory> findByIngredientCategoryCodeAndIdNot(String code, Integer id);

    /** Tìm toàn bộ con trực tiếp (bất kể is_active) để cascade. */
    List<IngredientCategory> findAllByParentCategory_Id(Integer parentId);

    /** Kiểm tra còn con active không (dùng cho thông báo lỗi khi cần). */
    boolean existsByParentCategory_IdAndActiveTrue(Integer parentCategoryId);
}
