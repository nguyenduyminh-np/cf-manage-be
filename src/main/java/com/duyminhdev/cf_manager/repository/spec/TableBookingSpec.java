package com.duyminhdev.cf_manager.repository.spec;  

import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingSearchRequestDTO;
import com.duyminhdev.cf_manager.entity.TableBooking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TableBookingSpec {

    private TableBookingSpec() {
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "expectedArriveTime",
            "checkInAt",
            "expectedCheckOut",
            "checkOutAt",
            "bookingStatus",
            "customerName",
            "phoneNumber",
            "depositAmount",
            "createdAt"
    );

    public static Specification<TableBooking> byCriteria(TableBookingSearchRequestDTO request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getTableId() != null) {
                predicates.add(cb.equal(root.get("table").get("id"), request.getTableId()));
            }

            if (StringUtils.hasText(request.getBookingStatus())) {
                predicates.add(cb.equal(
                        cb.upper(root.get("bookingStatus")),
                        request.getBookingStatus().trim().toUpperCase()
                ));
            }

            if (StringUtils.hasText(request.getCustomerName())) {
                String value = escapeLike(request.getCustomerName().trim().toLowerCase()) + "%";
                predicates.add(cb.like(cb.lower(root.get("customerName")), value, '\\'));
            }

            if (StringUtils.hasText(request.getPhoneNumber())) {
                String value = escapeLike(request.getPhoneNumber().trim().toLowerCase()) + "%";
                predicates.add(cb.like(cb.lower(root.get("phoneNumber")), value, '\\'));
            }

            if (request.getCheckInAt() != null) {
                Date checkInDate = Date.valueOf(request.getCheckInAt().atZone(ZoneOffset.UTC).toLocalDate());
                predicates.add(cb.equal(
                        cb.function("DATE", Date.class, root.get("checkInAt")),
                        checkInDate
                ));
            }

            if (request.getCheckOutAt() != null) {
                Date checkOutDate = Date.valueOf(request.getCheckOutAt().atZone(ZoneOffset.UTC).toLocalDate());
                predicates.add(cb.equal(
                        cb.function("DATE", Date.class, root.get("checkOutAt")),
                        checkOutDate
                ));
            }

            if (request.getActive() != null) {
                predicates.add(cb.equal(root.get("active"), request.getActive()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Sort resolveSort(String sortField, String sortDir) {
        String resolvedField = ALLOWED_SORT_FIELDS.contains(sortField) ? sortField : "expectedArriveTime";
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
