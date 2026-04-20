package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableSearchNativeResultDTO {

    private Integer tableId;
    private String tableCode;
    private String tableName;

    private String tableStatus;

    private Integer floor;
    private Integer slot;

    private Integer totalBooking;
    private Instant lastBookingTime;

    private Boolean active;
}
