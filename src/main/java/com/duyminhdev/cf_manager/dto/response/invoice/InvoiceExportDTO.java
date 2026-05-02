package com.duyminhdev.cf_manager.dto.response.invoice;

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
public class InvoiceExportDTO {

    @ExcelColumn("Mã hóa đơn")
    private String invoiceCode;

    @ExcelColumn("Tổng tiền")
    private BigDecimal totalAmount;

    @ExcelColumn("Trạng thái thanh toán")
    private String paymentStatus;

    @ExcelColumn("Phương thức thanh toán")
    private String paymentMethod;

    @ExcelColumn("Ngày tạo")
    private String createdAt;

    @ExcelColumn("Nhân viên tạo")
    private String fullName;

    @ExcelColumn("Mã đặt bàn")
    private String bookingInvoiceCode;
}