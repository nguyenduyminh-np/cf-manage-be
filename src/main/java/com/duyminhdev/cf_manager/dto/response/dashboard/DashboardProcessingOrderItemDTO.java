package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Bảng 1: Đơn hàng đang chế biến (status = PROCESSING, hôm nay).
 */
@Data
@Builder
public class DashboardProcessingOrderItemDTO {
    /** ID của đơn hàng. */
    private Integer orderId;
    /** Tên bàn đang phục vụ. */
    private String tableName;
    /** Thời điểm tạo đơn. */
    private Instant createdAt;
    /** Tóm tắt các món: "Cà phê đen x2, Bánh mì x1". */
    private String itemsSummary;
}
