package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

/**
 * Biểu đồ 4: Ngang – Top 5 món bán chạy hôm nay.
 * Chỉ tính các đơn không bị hủy (status != CANCEL).
 */
@Data
@Builder
public class DashboardTopDishItemDTO {
    /** Tên món ăn. */
    private String dishName;
    /** Tổng số lượng bán ra trong ngày. */
    private Integer totalQuantity;
}
