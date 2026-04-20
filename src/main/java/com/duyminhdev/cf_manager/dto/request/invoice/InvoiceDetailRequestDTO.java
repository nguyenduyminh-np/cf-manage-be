package com.duyminhdev.cf_manager.dto.request.invoice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceDetailRequestDTO {

    @NotNull(message = "Mã hóa đơn không được để trống")
    @Positive(message = "Mã hóa đơn phải lớn hơn 0")
    private Integer invoiceId;
}

