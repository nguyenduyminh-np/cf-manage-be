package com.duyminhdev.cf_manager.dto.response.dish;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishExportDTO {
    @ExcelColumn("Mã món")
    private String dishCode;

    @ExcelColumn("Tên món")
    private String dishName;

    @ExcelColumn("Giá (VNĐ)")
    private BigDecimal price;

    @ExcelColumn("Ảnh")
    private String photo;

    @ExcelColumn("Ngày tạo")
    private String createdTime; // dd/MM/yyyy HH:mm:ss

    @ExcelColumn("Trạng thái")
    private String active; // "Hoạt động" hoặc "Không hoạt động"

    @ExcelColumn("Danh mục")
    private String dishCategoryName;
}