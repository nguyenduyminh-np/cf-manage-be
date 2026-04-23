package com.duyminhdev.cf_manager.dto.request.invoice;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class InvoiceCreateRequestDTO {

    @NotNull(message = "Mã bàn không được để trống")
    @Positive(message = "Mã bàn phải lớn hơn 0")
    private Integer tableId;

    @NotNull(message = "Mã đơn đặt món không được để trống")
    @Positive(message = "Mã đơn đặt món phải lớn hơn 0")
    private Long dishOrderId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Mã phương thức thanh toán không hợp lệ")
    private String paymentMethod;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Mã trạng thái thanh toán không hợp lệ")
    private String paymentStatus;

    @NotNull(message = "Tổng tiền hóa đơn không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Tổng tiền hóa đơn phải lớn hơn 0")
    private BigDecimal totalMoney;

    @Positive(message = "Số lượng khách phải lớn hơn 0")
    private Integer guestCount = 1;

    @Positive(message = "Mã đặt bàn phải lớn hơn 0")
    private Integer bookingId;

    @Size(max = 255, message = "Tên khách không được vượt quá 255 ký tự")
    private String customerName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String customerPhone;

    @NotEmpty(message = "Danh sách chi tiết hóa đơn không được để rỗng")
    @Valid
    private List<InvoiceDetailPayloadDTO> invoiceDetails;
}

