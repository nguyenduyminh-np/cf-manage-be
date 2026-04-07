package com.duyminhdev.cf_manager.dto.request.table_booking;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TableBookingStatusUpdateRequestDTO {
    @NotNull(message = "bookingId is required")
    @Positive(message = "bookingId must be > 0")
    private Integer bookingId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid bookingStatus code")
    private String bookingStatus;

    private LocalDateTime checkInAt;

    private LocalDateTime checkOutAt;
}
