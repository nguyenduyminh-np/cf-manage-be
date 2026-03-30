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

    @NotNull(message = "tableId is required")
    @Positive(message = "tableId must be > 0")
    private Integer tableId;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid paymentMethod code")
    private String paymentMethod;

    @Pattern(regexp = ValidateValueConstants.BUSINESS_CODE, message = "Invalid paymentStatus code")
    private String paymentStatus;

    @NotNull(message = "totalMoney is required")
    @DecimalMin(value = "0", inclusive = false, message = "totalMoney must be > 0")
    private BigDecimal totalMoney;

    @Positive(message = "guestCount must be > 0")
    private Integer guestCount = 1;

    @NotEmpty(message = "invoiceDetails must not be empty")
    @Valid
    private List<InvoiceDetailPayloadDTO> invoiceDetails;
}

