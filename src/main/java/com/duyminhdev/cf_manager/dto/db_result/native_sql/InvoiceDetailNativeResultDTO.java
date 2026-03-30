package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDetailNativeResultDTO {

    private Integer invoiceId;
    private String invoiceCode;

    private Integer tableId;
    private String tableCode;
    private String tableName;

    private Integer accountId;
    private String accountUsername;
    private String accountFullName;

    private String paymentStatus;
    private String paymentMethod;

    private Integer guestCount;
    private BigDecimal totalMoney;
    private LocalDateTime createdTime;

    private Integer invoiceDetailId;
    private Integer dishId;
    private String dishCode;
    private String dishName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
