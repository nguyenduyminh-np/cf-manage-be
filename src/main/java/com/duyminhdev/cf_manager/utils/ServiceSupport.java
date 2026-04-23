package com.duyminhdev.cf_manager.utils;

import com.duyminhdev.cf_manager.entity.Account;
import com.duyminhdev.cf_manager.entity.Dish;
import com.duyminhdev.cf_manager.entity.DishOrderStatus;
import com.duyminhdev.cf_manager.entity.TableEntity;
import com.duyminhdev.cf_manager.enums.*;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceSupport {

    private static final long RESERVE_WINDOW_MINUTES = 30L;

    private final AccountRepository accountRepository;
    private final TableRepository tableRepository;
    private final DishRepository dishRepository;
    private final DishOrderStatusRepository dishOrderStatusRepository;
    private final TableBookingRepository tableBookingRepository;
    private final DishOrderRepository dishOrderRepository;

    /**
     * Lấy account hiện tại từ JWT/security context.
     * Đây là rule chung dùng ở booking/order/invoice.
     */
    public Account getCurrentAccount() {
        if (!SecurityUtils.isAuthenticated()) {
            throw new InvalidDataException("Authentication is required for this operation");
        }

        String username = SecurityUtils.getCurrentUsername();
        return accountRepository.findByUsername(username)
                .filter(account -> Boolean.TRUE.equals(account.getActive()))
                .orElseThrow(() -> new InvalidDataException("Current authenticated account is invalid or inactive"));
    }

    /**
     * Lấy bàn còn trống / sẵn sàng
     */
    public TableEntity getActiveTable(Integer tableId) {
        return tableRepository.findByIdAndActiveTrue(tableId)
                .orElseThrow(() -> new InvalidDataException("Table not found with id: " + tableId));
    }

    /**
     * Lấy Cac món đã sãn sàng phục vụ
     */
    public Dish getActiveDish(Integer dishId) {
        return dishRepository.findByIdAndActiveTrue(dishId)
                .orElseThrow(() -> new InvalidDataException("Dish not found with id: " + dishId));
    }

    /**
     *  Lấy trạng thái của order món
     */
    public DishOrderStatus getDishOrderStatusByCode(String statusCode) {
        String normalized = normalizeCode(statusCode);
        return dishOrderStatusRepository.findByDishOrderStatusCode(normalized)
                .filter(status -> Boolean.TRUE.equals(status.getActive()))
                .orElseThrow(() -> new InvalidDataException("Dish order status not found with code: " + statusCode));
    }

    /**
     * check thời gian đặt bàn
     */
    public void validateExpectedArriveTime(Instant expectedArriveTime) {
        if (expectedArriveTime == null) {
            throw new InvalidDataException("Thời gian đặt bàn không được để trống");
        }
        // Temporarily disable rule: expectedArriveTime must be at least 2 hours after current time.
        // if (expectedArriveTime.isBefore(Instant.now().plus(Duration.ofHours(2)))) {
        //     throw new InvalidDataException("Thời gian đặt bàn phải sớm hơn thời điểm hiện tại ít nhất 2 tiếng");
        // }
    }

    public void validateExpectedCheckoutTime(Instant expectedArriveTime, Instant expectedCheckOut) {
        if (expectedArriveTime == null) {
            throw new InvalidDataException("expectedArriveTime is required");
        }
        if (expectedCheckOut == null) {
            throw new InvalidDataException("expectedCheckOut is required");
        }
        if (expectedCheckOut.isBefore(expectedArriveTime) || expectedCheckOut.equals(expectedArriveTime)) {
            throw new InvalidDataException("expectedCheckOut must be greater than expectedArriveTime");
        }
    }

    public void validateBookingTimes(
            Instant expectedArriveTime,
            Instant expectedCheckOut,
            Instant checkInAt,
            Instant checkOutAt
    ) {
        if (expectedArriveTime != null && expectedCheckOut != null) {
            validateExpectedCheckoutTime(expectedArriveTime, expectedCheckOut);
        }

        if (checkOutAt != null && checkInAt == null) {
            throw new InvalidDataException("checkInAt is required when checkOutAt is provided");
        }

        if (checkInAt != null && checkOutAt != null && checkOutAt.isBefore(checkInAt)) {
            throw new InvalidDataException("checkOutAt must be >= checkInAt");
        }
    }

    /**
     * Check trạng thái bàn
     */
    public void validateTableStatusCode(String tableStatus) {
        if (!TableStatusEnum.isValidCode(tableStatus)) {
            throw new InvalidDataException("Invalid table status: " + tableStatus);
        }
    }

    public void validateBookingStatusCode(String bookingStatus) {
        if (!BookingStatusEnum.isValidCode(bookingStatus)) {
            throw new InvalidDataException("Invalid booking status: " + bookingStatus);
        }
    }

    public void validateDishOrderStatusCode(String dishOrderStatus) {
        if (!DishOrderStatusCodeEnum.isValidCode(dishOrderStatus)) {
            throw new InvalidDataException("Invalid dish order status: " + dishOrderStatus);
        }
    }

    public void validatePaymentStatusCode(String paymentStatus) {
        if (!PaymentStatusEnum.isValidCode(paymentStatus)) {
            throw new InvalidDataException("Invalid payment status: " + paymentStatus);
        }
    }

    public void validatePaymentMethodCode(String paymentMethod) {
        if (!PaymentMethodEnum.isValidCode(paymentMethod)) {
            throw new InvalidDataException("Invalid payment method: " + paymentMethod);
        }
    }

    public void updateTableStatus(TableEntity table, TableStatusEnum status) {
        table.setTableStatus(status.getCode());
        tableRepository.save(table);
    }

    /**
     * Kiểm tra booking upcoming trong 2 giờ.
     * Chỉ tính các booking còn hiệu lực giữ bàn.
     */
    public boolean hasUpcomingActiveBooking(Integer tableId) {
        Instant fromTime = Instant.now();
        Instant toTime = fromTime.plus(Duration.ofMinutes(RESERVE_WINDOW_MINUTES));

        return tableBookingRepository.existsUpcomingActiveBookingByTableIdAndStatuses(
                tableId,
                List.of(
                        BookingStatusEnum.CONFIRMED.getCode()
                ),
                fromTime,
                toTime
        );
    }

    /**
     * unfinished = không thuộc CANCELLED, DONE
     */
    public boolean hasUnfinishedOrders(Integer tableId) {
        return dishOrderRepository.existsUnfinishedOrdersByTableId(
                tableId,
                List.of(
                        DishOrderStatusCodeEnum.CANCEL.getCode(),
                        DishOrderStatusCodeEnum.PAID.getCode()
                )
        );
    }

    public boolean hasActiveCheckedInBooking(Integer tableId) {
        return tableBookingRepository.existsActiveBookingOnTableByStatuses(
                tableId,
                List.of(BookingStatusEnum.CHECKED_IN.getCode())
        );
    }

    /**
     * Rule trạng thái bàn tập trung duy nhất tại đây.
     */
    public TableStatusEnum resolveTableStatus(Integer tableId) {
        if (hasActiveCheckedInBooking(tableId)) {
            return TableStatusEnum.OCCUPIED;
        }

        if (hasUpcomingActiveBooking(tableId)) {
            return TableStatusEnum.BOOKED;
        }

        return TableStatusEnum.AVAILABLE;
    }

    /**
     * Service khác chỉ cần gọi hàm này sau khi mutate booking/order/payment.
     */
    public void recomputeAndSyncTableStatus(Integer tableId) {
        TableEntity table = getActiveTable(tableId);
        TableStatusEnum resolvedStatus = resolveTableStatus(tableId);
        updateTableStatus(table, resolvedStatus);
    }

    private String normalizeCode(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
