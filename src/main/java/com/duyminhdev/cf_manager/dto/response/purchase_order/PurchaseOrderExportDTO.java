package com.duyminhdev.cf_manager.dto.response.purchase_order;

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
public class PurchaseOrderExportDTO {

    @ExcelColumn("Mã đơn nhập")
    private String purchaseOrderCode;

    @ExcelColumn("Tổng tiền")
    private BigDecimal totalPrice;

    @ExcelColumn("Trạng thái thanh toán")
    private String paymentStatus;

    @ExcelColumn("Nhân viên tạo")
    private String accountFullName;

    @ExcelColumn("Nhà cung cấp")
    private String supplierName;

    @ExcelColumn("Ngày đặt")
    private String orderDate;

    @ExcelColumn("Ngày tạo")
    private String createdTime;
}