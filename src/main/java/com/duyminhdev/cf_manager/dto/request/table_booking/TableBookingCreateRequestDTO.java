package com.duyminhdev.cf_manager.dto.request.table_booking;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class TableBookingCreateRequestDTO {
    @NotNull(message = "Mã bàn không được để trống")
    @Positive(message = "Mã bàn phải lớn hơn 0")
    private Integer tableId;

    @JsonAlias("bookingTime")
    @NotNull(message = "Thời gian đặt bàn không được để trống")
    private Instant expectedArriveTime;

    @NotNull(message = "Thời gian dự kiến trả bàn không được để trống")
  //  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant expectedCheckOut;

    @Size(max = 255, message = "Tên khách hàng không được vượt quá 255 ký tự")
    private String customerName;

    @Pattern(regexp = "^$|" + ValidateValueConstants.PHONE_NUMBER, message = "Số điện thoại không đúng định dạng")
    private String phoneNumber;

    @JsonAlias("deposit")
    @DecimalMin(value = "0", inclusive = true, message = "Số tiền đặt cọc phải lớn hơn hoặc bằng 0")
    private BigDecimal depositAmount;

    private Boolean depositPaid;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant depositPaidAt;

    private Boolean depositForfeited;

    @Size(max = 255, message = "Mã giao dịch đặt cọc không được vượt quá 255 ký tự")
    private String depositTxnRef;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Mã trạng thái đặt bàn không hợp lệ")
    private String bookingStatus;

    @Size(max = 255, message = "Ghi chú không được vượt quá 255 ký tự")
    private String note;

    /**
     * true: tạo booking theo luồng khách vãng lai (check-in ngay),
     * false/null: tạo booking thông thường.
     */
    private Boolean isWalkIn;
}
