package com.duyminhdev.cf_manager.dto.response.supplier;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplierExportDTO {

    @ExcelColumn("Mã nhà cung cấp")
    private String supplierCode;

    @ExcelColumn("Tên nhà cung cấp")
    private String supplierName;

    @ExcelColumn("Thông tin liên hệ")
    private String contactInfo;

    @ExcelColumn("Địa chỉ")
    private String address;

    @ExcelColumn("Ngày tạo")
    private String createdTime;

    @ExcelColumn("Trạng thái")
    private String active;
}