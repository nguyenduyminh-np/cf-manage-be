package com.duyminhdev.cf_manager.dto.response.ingredient;

import lombok.Builder;
import lombok.Data;

/**
 * DTO dùng làm nguồn dữ liệu cho select/dropdown nhà cung cấp
 * khi thêm mới / cập nhật nguyên liệu.
 */
@Data
@Builder
public class SupplierSelectDTO {
    private String supplierCode;
    private String supplierName;
}
