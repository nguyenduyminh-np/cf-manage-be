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

    @Min(value = 1, message = "floor must be >= 1")
    @Max(value= 100, message = "invalid floor range")
    private Integer floor;

    @Min(value = 1, message = "slot must be >= 1")
    @Max(value = 1000, message = "slot must be <= 1000")
    private Integer slot;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid tableStatus code")
    private String tableStatus;

    private Boolean active;
}
