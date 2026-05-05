package com.duyminhdev.cf_manager.dto.request.supplier;

import lombok.Data;
import java.time.Instant;

@Data
public class SupplierSearchRequestDTO {
    private String supplierCode;        // LIKE
    private String supplierName;        // LIKE
    private String contactInfo;         // LIKE
    private String address;             // LIKE
    private Instant fromDate;
    private Instant toDate;
    private Boolean isActive;           // null -> tất cả, true/false lọc chính xác
    private Integer page;               // 0‑based
    private Integer limit;
    private String sortField;           // mặc định "createdTime"
    private String sortDir;             // asc/desc
}