package com.duyminhdev.cf_manager.dto.response.table_booking;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableBookingResponseDTO {

    private Integer bookingId;

    private Integer tableId;
    private String tableCode;
    private String tableName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant expectedArriveTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant checkInAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant expectedCheckOut;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant checkOutAt;

    private String bookingStatus;
    private String bookingStatusName;

    private String customerName;
    private String phoneNumber;
    private BigDecimal depositAmount;
    private Boolean depositPaid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant depositPaidAt;
    private Boolean depositForfeited;
    private String depositTxnRef;
    private String note;

    private Integer accountId;
    private String accountUsername;
    private String accountFullName;

    private Boolean active;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant createdAt;
}

