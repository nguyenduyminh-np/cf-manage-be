package com.duyminhdev.cf_manager.dto.response.invoice;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDetailResponse {

    private Integer invoiceId;
    private String invoiceCode;
    private Long dishOrderId;

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

    private Integer bookingId;
    private String customerName;
    private String customerPhone;

    private Boolean active;
    private Instant createdTime;

    private String uriVnPay;

    private List<InvoiceLineResponseDTO> invoiceDetails;
}

