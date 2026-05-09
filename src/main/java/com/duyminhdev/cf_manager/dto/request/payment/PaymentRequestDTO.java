package com.duyminhdev.cf_manager.dto.request.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentRequestDTO {
    @NotNull(message = "Mã đơn hàng không được để trống")
    private Integer orderId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Pattern(regexp = "CASH|BANK_TRANSFER", message = "Phương thức thanh toán không hợp lệ")
    private String paymentMethod;

    /**
     * Mã voucher giảm giá (tùy chọn).
     * Nếu được cung cấp, sẽ được validate và áp dụng khi thanh toán.
     */
    private String voucherCode;
}
