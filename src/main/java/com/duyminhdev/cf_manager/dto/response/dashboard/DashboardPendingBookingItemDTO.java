package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Bảng 6 – Đơn đặt bàn chờ xác nhận (booking_status = 'PENDING').
 * Hiển thị trên Dashboard cho mọi role để nhân viên biết cần xác nhận.
 */
@Data
@Builder
public class DashboardPendingBookingItemDTO {
    /** Mã booking. */
    private Integer bookingId;
    /** Tên khách hàng. */
    private String customerName;
    /** Số điện thoại. */
    private String phoneNumber;
    /** Tên bàn đặt. */
    private String tableName;
    /** Giờ đến dự kiến. */
    private Instant expectedArriveTime;
    /** Thời điểm tạo booking (để tính waiting time). */
    private Instant createdAt;
    /** Ghi chú. */
    private String note;
}
