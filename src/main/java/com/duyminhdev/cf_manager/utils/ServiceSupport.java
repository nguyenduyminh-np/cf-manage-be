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

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceSupport {

    private static final long UPCOMING_BOOKING_WINDOW_HOURS = 2L;

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
    public void validateBookingTime(LocalDateTime bookingTime) {
        if (bookingTime == null) {
            throw new InvalidDataException("bookingTime is required");
        }
        if (bookingTime.isBefore(LocalDateTime.now())) {
            throw new InvalidDataException("bookingTime must not be in the past");
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
        LocalDateTime fromTime = LocalDateTime.now();
        LocalDateTime toTime = fromTime.plusHours(UPCOMING_BOOKING_WINDOW_HOURS);

        return tableBookingRepository.existsUpcomingActiveBookingByTableIdAndStatuses(
                tableId,
                List.of(
                        BookingStatusEnum.PENDING.getCode(),
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
                        DishOrderStatusCodeEnum.DONE.getCode()
                )
        );
    }

    /**
     * Rule trạng thái bàn tập trung duy nhất tại đây.
     */
    public TableStatusEnum resolveTableStatus(Integer tableId) {
        if (hasUnfinishedOrders(tableId)) {
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
