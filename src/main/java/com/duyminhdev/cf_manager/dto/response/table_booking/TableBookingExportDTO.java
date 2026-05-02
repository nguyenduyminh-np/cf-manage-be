package com.duyminhdev.cf_manager.dto.response.table_booking;

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
public class TableBookingExportDTO {

    @ExcelColumn("Mã booking")
    private Integer bookingId;

    @ExcelColumn("Mã hóa đơn booking")
    private String bookingInvoiceCode;

    @ExcelColumn("Mã bàn")
    private Integer tableId;

    @ExcelColumn("Mã code bàn")
    private String tableCode;

    @ExcelColumn("Tên bàn")
    private String tableName;

    @ExcelColumn("Thời gian đến")
    private String expectedArriveTime;

    @ExcelColumn("Thời gian check-in")
    private String checkInAt;

    @ExcelColumn("Thời gian dự kiến trả bàn")
    private String expectedCheckOut;

    @ExcelColumn("Thời gian check-out")
    private String checkOutAt;

    @ExcelColumn("Trạng thái")
    private String bookingStatus;

    @ExcelColumn("Tên trạng thái")
    private String bookingStatusName;

    @ExcelColumn("Tên khách hàng")
    private String customerName;

    @ExcelColumn("Số điện thoại")
    private String phoneNumber;

    @ExcelColumn("Tiền cọc")
    private BigDecimal depositAmount;

    @ExcelColumn("Đã cọc")
    private Boolean depositPaid;

    @ExcelColumn("Thời gian cọc")
    private String depositPaidAt;

    @ExcelColumn("Phạt cọc")
    private Boolean depositForfeited;

    @ExcelColumn("Mã giao dịch cọc")
    private String depositTxnRef;

    @ExcelColumn("Ghi chú")
    private String note;

    @ExcelColumn("Mã nhân viên")
    private Integer accountId;

    @ExcelColumn("Tên đăng nhập")
    private String accountUsername;

    @ExcelColumn("Họ tên nhân viên")
    private String accountFullName;

    @ExcelColumn("Hoạt động")
    private Boolean active;

    @ExcelColumn("Ngày tạo")
    private String createdAt;
}