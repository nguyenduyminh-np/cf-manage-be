// AccountCreateRequestDTO.java
package com.duyminhdev.cf_manager.dto.request.account;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.Instant;

@Data
public class AccountCreateRequestDTO {

    private String accountCode; // optional

    @NotBlank(message = "Tên đăng nhập không được trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được trống")
    @Size(min = 6, max = 100, message = "Mật khẩu từ 6 ký tự trở lên")
    private String password;

    @NotBlank(message = "Họ tên không được trống")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    private String photo; // URL hoặc base64

    @NotNull(message = "Ngày sinh không được trống")
    private Instant dateOfBirth;

    private String phoneNumber;

    @NotNull(message = "Vai trò không được trống")
    private Integer roleId;
}