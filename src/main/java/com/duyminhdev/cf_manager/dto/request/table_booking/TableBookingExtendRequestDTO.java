package com.duyminhdev.cf_manager.dto.request.table_booking;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TableBookingExtendRequestDTO {

    @NotNull(message = "expectedCheckOut is required")
    private LocalDateTime expectedCheckOut;

    private Boolean force;
}
