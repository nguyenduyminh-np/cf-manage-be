// AccountDeleteRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.account;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountDeleteRequestDTO {
    @NotNull(message = "Id không được trống")
    private Integer id;
}