package com.duyminhdev.cf_manager.dto.request.supplier;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierCreateRequestDTO {
    private String supplierCode;       // optional

    @NotBlank(message = "Tên nhà cung cấp không được trống")
    private String supplierName;

    @NotBlank(message = "Thông tin liên hệ không được trống")
    private String contactInfo;

    private String address;            // optional
}