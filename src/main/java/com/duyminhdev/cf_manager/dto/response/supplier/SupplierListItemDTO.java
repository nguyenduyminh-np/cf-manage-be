package com.duyminhdev.cf_manager.dto.response.supplier;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class SupplierListItemDTO {
    private Integer id;
    private String supplierCode;
    private String supplierName;
    private String contactInfo;
    private String address;
    private Instant createdTime;
    private Boolean active;
}