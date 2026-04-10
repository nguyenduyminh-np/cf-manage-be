package com.duyminhdev.cf_manager.dto.request.table_booking;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TableBookingCheckInRequestDTO {

    private LocalDateTime checkInAt;

    private Boolean force;
}
