package com.duyminhdev.cf_manager.dto.response.invoice;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class InvoiceListItemDTO {
    private Integer id;
    private String invoiceCode;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private Instant createdAt;
    private String fullName;                // từ account
    private String bookingInvoiceCode;     // từ table_booking
}