package com.duyminhdev.cf_manager.dto.request.table_booking;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import com.duyminhdev.cf_manager.dto.base.PageFilterRequest;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TableBookingSearchRequestDTO extends PageFilterRequest {
    private Integer tableId;

    //@Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid bookingStatus code")
    private String bookingStatus;

    private String customerName;

  //  @Pattern(regexp = "^$|" + ValidateValueConstants.PHONE_NUMBER, message = "Invalid phone number")
    private String phoneNumber;

    private LocalDateTime bookingFrom;

    private LocalDateTime bookingTo;

    private Boolean active;
}
