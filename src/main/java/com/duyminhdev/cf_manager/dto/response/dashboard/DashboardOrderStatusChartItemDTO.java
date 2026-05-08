package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

/**
 * Biểu đồ 3: Bánh – Trạng thái đơn hàng hôm nay.
 * Mỗi item là một trạng thái và số đơn tương ứng.
 */
@Data
@Builder
public class DashboardOrderStatusChartItemDTO {
    /** Tên trạng thái đơn hàng (hiển thị trên biểu đồ). */
    private String status;
    /** Số đơn hàng ở trạng thái này. */
    private Integer orderCount;
}
