// AccountListItemDTO.java
package com.duyminhdev.cf_manager.dto.response.account;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class AccountListItemDTO {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String photo;
    private Boolean isActive;
    private String roleName;
    private Instant dateOfBirth;
    private Instant createdAt;
}