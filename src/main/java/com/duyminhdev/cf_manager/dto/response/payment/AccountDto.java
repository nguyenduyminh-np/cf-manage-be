package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountDto {
    private Integer accountId;
    private String username;
    private String fullName;
}