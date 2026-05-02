package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableBookingDetailNativeResultDTO {

    private Integer bookingId;
    private String bookingInvoiceCode;

    private Integer tableId;
    private String tableCode;
    private String tableName;

    private Instant expectedArriveTime;
    private Instant checkInAt;
    private Instant expectedCheckOut;
    private Instant checkOutAt;

    private String bookingStatus;

    private String customerName;
    private String phoneNumber;
    private BigDecimal depositAmount;
    private Boolean depositPaid;
    private Instant depositPaidAt;
    private Boolean depositForfeited;
    private String depositTxnRef;
    private String note;

    private Integer accountId;
    private String accountUsername;
    private String accountFullName;

    private Boolean active;
    private Instant createdAt;
}
