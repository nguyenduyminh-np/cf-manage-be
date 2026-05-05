package com.duyminhdev.cf_manager.repository.spec;

import com.duyminhdev.cf_manager.dto.request.purchase_order.PurchaseOrderSearchRequestDTO;
import com.duyminhdev.cf_manager.entity.PurchaseOrder;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderSpec {

    public static Specification<PurchaseOrder> byCriteria(PurchaseOrderSearchRequestDTO request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(request.getPurchaseOrderCode())) {
                predicates.add(cb.like(root.get("purchaseOrderCode"), "%" + request.getPurchaseOrderCode().trim() + "%"));
            }
            if (StringUtils.hasText(request.getPaymentStatus())) {
                predicates.add(cb.equal(root.get("paymentStatus"), request.getPaymentStatus().trim()));
            }
            if (request.getTotalPriceFrom() != null) {
                predicates.add(cb.ge(root.get("totalPrice"), request.getTotalPriceFrom()));
            }
            if (request.getTotalPriceTo() != null) {
                predicates.add(cb.le(root.get("totalPrice"), request.getTotalPriceTo()));
            }
            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), request.getFromDate()));
            }
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), request.getToDate()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<PurchaseOrder> sortByField(String sortField, String sortDir) {
        return (root, query, cb) -> {
            String field = (sortField != null) ? sortField : "createdTime";
            boolean asc = !"desc".equalsIgnoreCase(sortDir);
            switch (field) {
                case "totalPrice":
                    query.orderBy(asc ? cb.asc(root.get("totalPrice")) : cb.desc(root.get("totalPrice")));
                    break;
                case "paymentStatus":
                    query.orderBy(asc ? cb.asc(root.get("paymentStatus")) : cb.desc(root.get("paymentStatus")));
                    break;
                default:
                    query.orderBy(asc ? cb.asc(root.get("createdTime")) : cb.desc(root.get("createdTime")));
            }
            return cb.conjunction();
        };
    }
}