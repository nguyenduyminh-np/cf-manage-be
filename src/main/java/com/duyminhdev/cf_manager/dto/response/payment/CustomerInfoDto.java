package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerInfoDto {
    private Integer bookingId;
    private String customerName;
    private String phoneNumber;
}