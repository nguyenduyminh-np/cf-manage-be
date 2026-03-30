package com.duyminhdev.cf_manager.dto.response.invoice;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {

    private Integer invoiceId;
    private String invoiceCode;

    private Integer tableId;
    private String tableCode;
    private String tableName;

    private Integer accountId;
    private String accountUsername;
    private String accountFullName;

    private String paymentStatus;
    private String paymentStatusName;

    private String paymentMethod;
    private String paymentMethodName;

    private Integer guestCount;
    private BigDecimal totalMoney;

    private Boolean active;
    private LocalDateTime createdTime;

    private String uriVnPay;
}

