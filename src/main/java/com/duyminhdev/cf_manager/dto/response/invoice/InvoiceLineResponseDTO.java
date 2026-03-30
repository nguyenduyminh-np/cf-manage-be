package com.duyminhdev.cf_manager.dto.response.invoice;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceLineResponseDTO {

    private Integer invoiceDetailId;
    private Integer dishId;
    private String dishCode;
    private String dishName;

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}

