package com.duyminhdev.cf_manager.dto.response.supplier;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierOptionDTO {
    private Integer id;
    private String supplierCode;
    private String supplierName;
}