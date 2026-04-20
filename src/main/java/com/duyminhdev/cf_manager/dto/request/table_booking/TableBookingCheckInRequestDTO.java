package com.duyminhdev.cf_manager.dto.request.table_booking;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

@Getter
@Setter
public class TableBookingCheckInRequestDTO {

    @NotNull(message = "bookingId is required")
    @Positive(message = "bookingId must be > 0")
    private Integer bookingId;

 //   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant checkInAt;

    private Boolean force;
}
