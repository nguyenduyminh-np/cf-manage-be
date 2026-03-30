package com.duyminhdev.cf_manager.repository.spec;

import com.duyminhdev.cf_manager.dto.request.dish.DishSearchRequestDTO;
import com.duyminhdev.cf_manager.entity.Dish;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class DishSpec {

    private DishSpec() {
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "dishCode",
            "dishName",
            "price",
            "createdTime"
    );

    public static Specification<Dish> byCriteria(DishSearchRequestDTO request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getKeyword())) {
                String value = escapeLike(request.getKeyword().trim().toLowerCase()) + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("dishName")), value, '\\'),
                                cb.like(cb.lower(root.get("dishCode")), value, '\\')
                        )
                );
            }

            if (request.getDishCategoryId() != null) {
                predicates.add(cb.equal(root.get("dishCategory").get("id"), request.getDishCategoryId()));
            }

            if (request.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), request.getActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Sort resolveSort(String sortField, String sortDir) {
        String resolvedField = ALLOWED_SORT_FIELDS.contains(sortField) ? sortField : "id";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, resolvedField);
    }

    private static String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
