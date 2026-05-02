package com.duyminhdev.cf_manager.dto.response.table;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableExportDTO {

    @ExcelColumn("Mã bàn")
    private Integer tableId;

    @ExcelColumn("Mã code")
    private String tableCode;

    @ExcelColumn("Tên bàn")
    private String tableName;

    @ExcelColumn("Trạng thái")
    private String tableStatus;

    @ExcelColumn("Tên trạng thái")
    private String tableStatusName;

    @ExcelColumn("Tầng")
    private Integer floor;

    @ExcelColumn("Số chỗ")
    private Integer slot;

    @ExcelColumn("Số lượt đặt")
    private Integer totalBooking;

    @ExcelColumn("Lần đặt gần nhất")
    private String lastBookingTime;

    @ExcelColumn("Trạng thái hoạt động")
    private String active;
}