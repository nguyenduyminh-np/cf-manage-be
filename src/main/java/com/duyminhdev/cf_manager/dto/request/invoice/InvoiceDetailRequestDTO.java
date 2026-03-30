package com.duyminhdev.cf_manager.dto.request.invoice;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceDetailRequestDTO {

    @NotNull(message = "invoiceId is required")
    @Positive(message = "invoiceId must be > 0")
    private Integer invoiceId;
}

