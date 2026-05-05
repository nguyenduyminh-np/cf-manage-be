package com.duyminhdev.cf_manager.dto.response.warehouse;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.Data;

@Data
public class WarehouseExportDTO {
    @ExcelColumn(value = "Mã kho") private String warehouseCode;
    @ExcelColumn(value = "Tên kho") private String warehouseName;
    @ExcelColumn(value = "Vị trí") private String location;
    @ExcelColumn(value = "Ghi chú") private String note;
    @ExcelColumn(value = "Trạng thái") private String active;
}
