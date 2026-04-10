package com.duyminhdev.cf_manager.dto.request.table_booking;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TableBookingDepositRequestDTO {

    @DecimalMin(value = "0", inclusive = false, message = "depositAmount must be > 0")
    private BigDecimal depositAmount;

    private LocalDateTime depositPaidAt;

    @Size(max = 255, message = "depositTxnRef must be <= 255 characters")
    private String depositTxnRef;
}
