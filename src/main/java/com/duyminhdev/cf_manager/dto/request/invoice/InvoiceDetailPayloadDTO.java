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

    @Positive(message = "invoiceDetailId must be > 0")
    private Integer invoiceDetailId;

    @NotNull(message = "dishId is required")
    @Positive(message = "dishId must be > 0")
    private Integer dishId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be > 0")
    private Integer quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0", inclusive = false, message = "unitPrice must be > 0")
    private BigDecimal unitPrice;
}

