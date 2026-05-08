package com.duyminhdev.cf_manager.dto.response.dashboard;

import lombok.Builder;
import lombok.Data;

/**
 * Biểu đồ 5: Cột chồng – Trạng thái bàn theo tầng.
 * Mỗi item là một tầng với số bàn OCCUPIED và AVAILABLE.
 */
@Data
@Builder
public class DashboardTableByFloorItemDTO {
    /** Tầng (floor). */
    private Integer floor;
    /** Số bàn đang có khách. */
    private Integer occupied;
    /** Số bàn đang trống. */
    private Integer available;
}
