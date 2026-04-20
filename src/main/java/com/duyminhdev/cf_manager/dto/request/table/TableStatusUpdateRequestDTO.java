package com.duyminhdev.cf_manager.dto.request.table;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TableStatusUpdateRequestDTO {
    @NotNull(message = "Mã bàn (tableId) không được để trống")
    @Positive(message = "Mã bàn (tableId) phải lớn hơn 0")
    private Integer tableId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Mã trạng thái bàn (tableStatus) không hợp lệ")
    private String tableStatus;
}
