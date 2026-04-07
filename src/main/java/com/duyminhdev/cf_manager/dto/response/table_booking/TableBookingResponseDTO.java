package com.duyminhdev.cf_manager.dto.response.table_booking;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private LocalDateTime expectedArriveTime;
    private LocalDateTime checkInAt;
    private LocalDateTime expectedCheckOut;
    private LocalDateTime checkOutAt;

    private String bookingStatus;
    private String bookingStatusName;

    private String customerName;
    private String phoneNumber;
    private BigDecimal depositAmount;
    private String note;

    private Integer accountId;
    private String accountUsername;
    private String accountFullName;

    private Boolean active;
    private LocalDateTime createdAt;
}

