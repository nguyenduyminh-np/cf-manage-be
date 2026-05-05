package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Kết quả native query tổng hợp doanh thu cho chatbot.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesSummaryDTO {

    /** Tổng doanh thu trong khoảng thời gian */
    private BigDecimal totalRevenue;

    /** Số hóa đơn đã thanh toán */
    private Long invoiceCount;
}
