package com.duyminhdev.cf_manager.dto.request.invoice;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceDetailPayloadDTO {

    @Positive(message = "Mã chi tiết hóa đơn phải lớn hơn 0")
    private Integer invoiceDetailId;

    @NotNull(message = "Mã món ăn không được để trống")
    @Positive(message = "Mã món ăn phải lớn hơn 0")
    private Integer dishId;

    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;

    @NotNull(message = "Giá đơn vị không được để trống")
    @DecimalMin(value = "0", inclusive = false, message = "Giá đơn vị phải lớn hơn 0")
    private BigDecimal unitPrice;
}

