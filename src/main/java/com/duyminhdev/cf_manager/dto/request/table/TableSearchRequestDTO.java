package com.duyminhdev.cf_manager.dto.request.table;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import com.duyminhdev.cf_manager.dto.base.PageFilterRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableSearchRequestDTO extends PageFilterRequest {
    private String keyword;

    @Min(value = 1, message = "Số tầng phải từ 1 trở lên")
    @Max(value= 100, message = "Số tầng vượt quá giới hạn cho phép")
    private Integer floor;

    @Min(value = 1, message = "Số chỗ phải từ 1 trở lên")
    @Max(value = 1000, message = "Số chỗ không được vượt quá 1000")
    private Integer slot;

  //  @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid tableStatus code")
    private String tableStatus;

    private Boolean active;
}
