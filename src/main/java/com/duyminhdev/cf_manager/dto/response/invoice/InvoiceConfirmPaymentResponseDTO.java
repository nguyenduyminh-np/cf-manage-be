package com.duyminhdev.cf_manager.dto.response.invoice;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceConfirmPaymentResponseDTO {

    private Integer invoiceId;
    private String invoiceCode;
    private String paymentStatus;
    private String paymentStatusName;
}

