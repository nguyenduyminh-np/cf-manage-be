// AccountDetailDTO.java
package com.duyminhdev.cf_manager.dto.response.account;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class AccountDetailDTO {
    private Integer id;
    private String accountCode;
    private String username;
    private String fullName;
    private String email;
    private String photo;
    private Instant dateOfBirth;
    private String phoneNumber;
    private Boolean isActive;
    private Integer roleId;
    private String roleName;
    private Instant createdAt;
    private Instant updatedAt; // có thể lấy từ version? Thực tế không có, bỏ qua hoặc dùng createdAt
}