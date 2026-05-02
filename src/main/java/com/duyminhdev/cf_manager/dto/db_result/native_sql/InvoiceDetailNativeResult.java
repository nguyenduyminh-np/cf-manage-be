package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class InvoiceDetailNativeResult {
    private Integer invoiceId;
    private String invoiceCode;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private Instant createdAt;
    // bàn
    private Integer tableId;
    private String tableCode;
    private String tableName;
    private Integer floor;
    private Integer slot;
    private String tableStatus;
    // account
    private Integer accountId;
    private String username;
    private String fullName;
    // customer
    private Integer bookingId;
    private String customerName;
    private String phoneNumber;
    // items: sẽ lấy riêng bằng query phụ hoặc map trong service
}