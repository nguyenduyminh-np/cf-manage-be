package com.duyminhdev.cf_manager.dto.response.dish_category;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishCategoryExportDTO {

    @ExcelColumn("Mã danh mục")
    private String dishCategoryCode;

    @ExcelColumn("Tên danh mục")
    private String dishCategoryName;

    @ExcelColumn("Ngày tạo")
    private String createdTime;

    @ExcelColumn("Trạng thái")
    private String active;
}