// AccountSearchNativeResult.java
package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class AccountSearchNativeResult {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String photo;
    private Boolean isActive;
    private Integer roleId;
    private String roleName;
    private Instant dateOfBirth;
    private Instant createdAt;
}