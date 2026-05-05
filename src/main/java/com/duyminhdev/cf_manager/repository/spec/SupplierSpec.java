package com.duyminhdev.cf_manager.repository.spec;

import com.duyminhdev.cf_manager.dto.request.supplier.SupplierSearchRequestDTO;
import com.duyminhdev.cf_manager.entity.Supplier;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

public final class SupplierSpec {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "supplierCode",
            "supplierName",
            "contactInfo",
            "address",
            "active",
            "createdTime"
    );

    private SupplierSpec() {
    }

    public static Specification<Supplier> byCriteria(SupplierSearchRequestDTO request) {
        SupplierSearchRequestDTO safeRequest = request != null ? request : new SupplierSearchRequestDTO();

        return Specification.allOf(
                supplierCodeContains(safeRequest.getSupplierCode()),
                supplierNameContains(safeRequest.getSupplierName()),
                contactInfoContains(safeRequest.getContactInfo()),
                addressContains(safeRequest.getAddress()),
                createdTimeGte(safeRequest.getFromDate()),
                createdTimeLte(safeRequest.getToDate()),
                activeEquals(safeRequest.getIsActive())
        );
    }

    public static Sort resolveSort(String sortField, String sortDir) {
        String field = StringUtils.hasText(sortField) && ALLOWED_SORT_FIELDS.contains(sortField.trim())
                ? sortField.trim()
                : "createdTime";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    private static Specification<Supplier> supplierCodeContains(String supplierCode) {
        return hasTextSpec(supplierCode, "supplierCode");
    }

    private static Specification<Supplier> supplierNameContains(String supplierName) {
        return hasTextSpec(supplierName, "supplierName");
    }

    private static Specification<Supplier> contactInfoContains(String contactInfo) {
        return hasTextSpec(contactInfo, "contactInfo");
    }

    private static Specification<Supplier> addressContains(String address) {
        return hasTextSpec(address, "address");
    }

    private static Specification<Supplier> createdTimeGte(Instant fromDate) {
        return fromDate == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdTime"), fromDate);
    }

    private static Specification<Supplier> createdTimeLte(Instant toDate) {
        return toDate == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdTime"), toDate);
    }

    private static Specification<Supplier> activeEquals(Boolean isActive) {
        return isActive == null ? null : (root, query, cb) -> cb.equal(root.get("active"), isActive);
    }

    private static Specification<Supplier> hasTextSpec(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        String pattern = "%" + escapeLike(value.trim().toLowerCase(Locale.ROOT)) + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(fieldName)), pattern, '\\');
    }

    private static String escapeLike(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}