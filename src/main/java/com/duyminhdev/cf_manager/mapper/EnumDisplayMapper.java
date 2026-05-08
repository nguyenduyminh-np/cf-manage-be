package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.enums.BookingStatusEnum;
import com.duyminhdev.cf_manager.enums.DishOrderStatusCodeEnum;
import com.duyminhdev.cf_manager.enums.PaymentMethodEnum;
import com.duyminhdev.cf_manager.enums.PaymentStatusEnum;
import com.duyminhdev.cf_manager.enums.PurchaseOrderStatusEnum;
import com.duyminhdev.cf_manager.enums.TableStatusEnum;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class EnumDisplayMapper {

    @Named("toTableStatusName")
    public String toTableStatusName(String code) {
        return safeResolveTableStatus(code);
    }

    @Named("toBookingStatusName")
    public String toBookingStatusName(String code) {
        return safeResolveBookingStatus(code);
    }

    @Named("toDishOrderStatusName")
    public String toDishOrderStatusName(String code) {
        return safeResolveDishOrderStatus(code);
    }

    @Named("toPaymentStatusName")
    public String toPaymentStatusName(String code) {
        return safeResolvePaymentStatus(code);
    }

    @Named("toPaymentMethodName")
    public String toPaymentMethodName(String code) {
        return safeResolvePaymentMethod(code);
    }

    @Named("toPurchaseOrderStatusName")
    public String toPurchaseOrderStatusName(String code) {
        return safeResolvePurchaseOrderStatus(code);
    }

    private String safeResolveTableStatus(String code) {
        try {
            return code == null ? null : TableStatusEnum.fromCode(code).getLabel();
        } catch (Exception ex) {
            return code;
        }
    }

    private String safeResolveBookingStatus(String code) {
        try {
            return code == null ? null : BookingStatusEnum.fromCode(code).getLabel();
        } catch (Exception ex) {
            return code;
        }
    }

    private String safeResolveDishOrderStatus(String code) {
        try {
            return code == null ? null : DishOrderStatusCodeEnum.fromCode(code).getLabel();
        } catch (Exception ex) {
            return code;
        }
    }

    private String safeResolvePaymentStatus(String code) {
        try {
            return code == null ? null : PaymentStatusEnum.fromCode(code).getLabel();
        } catch (Exception ex) {
            return code;
        }
    }

    private String safeResolvePaymentMethod(String code) {
        try {
            return code == null ? null : PaymentMethodEnum.fromCode(code).getLabel();
        } catch (Exception ex) {
            return code;
        }
    }

    private String safeResolvePurchaseOrderStatus(String code) {
        try {
            return code == null ? null : PurchaseOrderStatusEnum.fromCode(code).getLabel();
        } catch (Exception ex) {
            return code;
        }
    }
}
