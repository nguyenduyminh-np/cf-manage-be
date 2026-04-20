package com.duyminhdev.cf_manager.dto.request.table_booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableBookingConfirmRequestDTO {

    @NotNull(message = "bookingId is required")
    @Positive(message = "bookingId must be > 0")
    private Integer bookingId;
}
