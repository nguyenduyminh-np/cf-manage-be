package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PaymentResponse {
    private InvoiceDto invoice;
    private List<InvoiceDetailDto> invoiceDetails;
    private CashFlowDto cashFlow;
}