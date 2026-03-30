package com.duyminhdev.cf_manager.dto.request.invoice;

import com.duyminhdev.cf_manager.constant.ValidateValueConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceConfirmPaymentRequestDTO {

    @NotNull(message = "invoiceId is required")
    @Positive(message = "invoiceId must be > 0")
    private Integer invoiceId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid paymentStatus code")
    private String paymentStatus;
}

