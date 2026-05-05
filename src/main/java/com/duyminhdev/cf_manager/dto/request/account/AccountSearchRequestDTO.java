// AccountSearchRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.account;

import lombok.Data;
import java.time.Instant;

@Data
public class AccountSearchRequestDTO {
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Integer roleId;
    private Boolean isActive;
    private Instant fromBirthDate;
    private Instant toBirthDate;
    private Integer page;
    private Integer limit;
    private String sortField;
    private String sortDir;
}