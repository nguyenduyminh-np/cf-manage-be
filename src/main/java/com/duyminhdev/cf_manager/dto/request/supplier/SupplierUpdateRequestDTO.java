package com.duyminhdev.cf_manager.dto.request.supplier;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SupplierUpdateRequestDTO {
    @NotNull(message = "Id không được trống")
    private Integer id;

    private String supplierCode;
    private String supplierName;
    private String contactInfo;
    private String address;
    private Boolean active;
}