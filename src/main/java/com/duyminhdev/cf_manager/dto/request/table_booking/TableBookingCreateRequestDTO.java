package com.duyminhdev.cf_manager.dto.request.table_booking;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TableBookingCreateRequestDTO {
    @NotNull(message = "tableId is required")
    @Positive(message = "tableId must be > 0")
    private Integer tableId;

    @JsonAlias("bookingTime")
    @NotNull(message = "expectedArriveTime is required")
    @FutureOrPresent(message = "invalid time range")
    private LocalDateTime expectedArriveTime;

    @NotNull(message = "expectedCheckOut is required")
    private LocalDateTime expectedCheckOut;

    @Size(max = 255, message = "customerName must be <= 255 characters")
    private String customerName;

    @Pattern(regexp = "^$|" + ValidateValueConstants.PHONE_NUMBER, message = "Invalid phone number")
    private String phoneNumber;

    @JsonAlias("deposit")
    @DecimalMin(value = "0", inclusive = true, message = "depositAmount must be >= 0")
    private BigDecimal depositAmount;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid bookingStatus code")
    private String bookingStatus;

    @Size(max = 255, message = "note must be <= 255 characters")
    private String note;
}
