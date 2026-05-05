// AccountExportDTO.java
package com.duyminhdev.cf_manager.dto.response.account;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountExportDTO {

    @ExcelColumn("Tên đăng nhập")
    private String username;

    @ExcelColumn("Họ tên")
    private String fullName;

    @ExcelColumn("Email")
    private String email;

    @ExcelColumn("Số điện thoại")
    private String phoneNumber;

    @ExcelColumn("Vai trò")
    private String roleName;

    @ExcelColumn("Trạng thái")
    private String isActive; // "Hoạt động" / "Khóa"

    @ExcelColumn("Ngày sinh")
    private String dateOfBirth;

    @ExcelColumn("Ngày tạo")
    private String createdAt;
}