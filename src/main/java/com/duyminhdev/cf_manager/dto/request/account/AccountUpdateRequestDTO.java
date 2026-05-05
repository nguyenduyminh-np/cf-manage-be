// AccountUpdateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.account;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.Instant;

@Data
public class AccountUpdateRequestDTO {

    @NotNull(message = "Id không được trống")
    private Integer id;

    private String accountCode;

    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3 đến 50 ký tự")
    private String username;

    @Size(min = 6, max = 100, message = "Mật khẩu từ 6 ký tự trở lên")
    private String password; // nếu null thì không đổi

    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String photo;

    private Instant dateOfBirth;

    private String phoneNumber;

    private Integer roleId;

    private Boolean isActive;
}