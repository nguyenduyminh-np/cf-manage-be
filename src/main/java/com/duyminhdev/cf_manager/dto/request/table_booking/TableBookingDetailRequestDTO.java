package com.duyminhdev.cf_manager.dto.request.table_booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableBookingDetailRequestDTO {

    @NotNull(message = "Mã đặt bàn không được để trống")
    @Positive(message = "Mã đặt bàn phải lớn hơn 0")
    private Integer bookingId;
}
