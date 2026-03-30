package com.duyminhdev.cf_manager.dto.response.invoice;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceCountResponseDTO {

    private Long totalInvoices;
    private String nextInvoiceCode;
}

