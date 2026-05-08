package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Bảng 2: Booking sắp đến trong 2 giờ tới
 * (không tính status CANCELLED hoặc EXPIRED).
 */
@Data
@Builder
public class DashboardUpcomingBookingItemDTO {
    /** Thời gian đến dự kiến. */
    private Instant expectedArriveTime;
    /** Tên khách hàng. */
    private String customerName;
    /** Số điện thoại. */
    private String phoneNumber;
    /** Tên bàn đã đặt. */
    private String tableName;
    /** Ghi chú của khách. */
    private String note;
}
