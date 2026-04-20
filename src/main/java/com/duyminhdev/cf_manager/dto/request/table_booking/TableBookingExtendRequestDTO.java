package com.duyminhdev.cf_manager.dto.request.table_booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

@Getter
@Setter
public class TableBookingExtendRequestDTO {

    @NotNull(message = "bookingId is required")
    @Positive(message = "bookingId must be > 0")
    private Integer bookingId;

    @NotNull(message = "Thời gian dự kiến trả bàn không được để trống")
   // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant expectedCheckOut;

    private Boolean force;
}
