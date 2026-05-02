package com.duyminhdev.cf_manager.dto.response.dish_order;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryExportDTO {

    @ExcelColumn("Mã order")
    private Integer dishOrderId;

    @ExcelColumn("Tên bàn")
    private String tableName;

    @ExcelColumn("Tên nhân viên")
    private String employeeName;

    @ExcelColumn("Trạng thái")
    private String orderStatus;

    @ExcelColumn("Mã trạng thái")
    private String dishOrderStatusCode;

    @ExcelColumn("Ngày tạo")
    private String createdAt;

    @ExcelColumn("Ghi chú")
    private String note;

    @ExcelColumn("Tổng số lượng")
    private Long totalQuantity;

    @ExcelColumn("Tổng tiền")
    private BigDecimal totalAmount;
}