package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class InvoiceDto {
    private Integer id;
    private String invoiceCode;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private Instant createdAt;
    private Integer diningTableId;
    private Integer guestCount;
    private Integer dishOrderId;
    private Integer accountId;
    private Integer bookingId;
    private String customerName;
    private String customerPhone;
}