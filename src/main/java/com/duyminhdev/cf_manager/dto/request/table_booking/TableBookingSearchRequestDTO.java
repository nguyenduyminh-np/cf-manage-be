package com.duyminhdev.cf_manager.dto.request.table_booking;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.duyminhdev.cf_manager.dto.base.PageFilterRequest;
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

    private String phoneNumber;

    @JsonAlias("check_in_at")
    private LocalDateTime checkInAt;

    @JsonAlias("check_out_at")
    private LocalDateTime checkOutAt;

    private Boolean active;
}
