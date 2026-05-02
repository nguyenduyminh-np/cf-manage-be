package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class InvoiceSearchNativeResult {
    private Integer id;
    private String invoiceCode;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private Instant createdAt;
    private String fullName;
    private String bookingInvoiceCode;
}