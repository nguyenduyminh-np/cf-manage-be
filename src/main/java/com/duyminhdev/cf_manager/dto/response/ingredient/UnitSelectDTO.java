package com.duyminhdev.cf_manager.dto.response.ingredient;

import lombok.Builder;
import lombok.Data;

/**
 * DTO dùng làm nguồn dữ liệu cho select/dropdown đơn vị tính
 * khi thêm mới / cập nhật nguyên liệu.
 */
@Data
@Builder
public class UnitSelectDTO {
    private String unitCode;
    private String unitName;
}
