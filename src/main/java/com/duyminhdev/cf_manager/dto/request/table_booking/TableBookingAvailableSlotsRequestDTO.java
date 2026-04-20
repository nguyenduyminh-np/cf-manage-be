package com.duyminhdev.cf_manager.dto.request.table_booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TableBookingAvailableSlotsRequestDTO {

    @NotNull(message = "tableId is required")
    @Positive(message = "tableId must be > 0")
    private Integer tableId;

    @NotNull(message = "date is required")
    private LocalDate date;
}
