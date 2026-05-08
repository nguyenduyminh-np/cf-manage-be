package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

/**
 * Biểu đồ 2: Cột – Số đơn hàng theo giờ trong ngày.
 * Mỗi item đại diện cho một khung giờ (0-23).
 */
@Data
@Builder
public class DashboardOrderByHourItemDTO {
    /** Giờ trong ngày (0 – 23). */
    private Integer hour;
    /** Số đơn hàng trong giờ đó. */
    private Integer orderCount;
}
