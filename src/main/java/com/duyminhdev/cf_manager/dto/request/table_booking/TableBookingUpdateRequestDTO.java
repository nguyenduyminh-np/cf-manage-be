package com.duyminhdev.cf_manager.dto.request.table_booking;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TableBookingUpdateRequestDTO {
    @NotNull(message = "bookingId is required")
    @Positive(message = "bookingId must be > 0")
    private Integer bookingId;

    @NotNull(message = "tableId is required")
    @Positive(message = "tableId must be > 0")
    private Integer tableId;

    @NotNull(message = "bookingTime is required")
    private LocalDateTime bookingTime;

    @Size(max = 255, message = "customerName must be <= 255 characters")
    private String customerName;

    @Pattern(regexp = "^$|" + ValidateValueConstants.PHONE_NUMBER, message = "Invalid phone number")
    private String phoneNumber;

    @DecimalMin(value = "0", inclusive = true, message = "deposit must be >= 0")
    private BigDecimal deposit;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid bookingStatus code")
    private String bookingStatus;

    @Size(max = 255, message = "note must be <= 255 characters")
    private String note;
}
