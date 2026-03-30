package main.java.com.duyminhdev.cf_manager.repository.spec;

import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingSearchRequestDTO;
import com.duyminhdev.cf_manager.entity.TableBooking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TableBookingSpec {

    private TableBookingSpec() {
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "bookingTime",
            "checkInTime",
            "bookingStatus",
            "customerName",
            "phoneNumber",
            "deposit",
            "createdTime"
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

            if (request.getBookingFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("bookingTime"), request.getBookingFrom()));
            }

            if (request.getBookingTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("bookingTime"), request.getBookingTo()));
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
