package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentPreviewResponseDTO {
    private Integer orderId;
    private Instant orderCreatedAt;
    private DiningTableDto diningTable;
    private AccountDto createdBy;
    private CustomerInfoDto customer;
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private List<String> suggestedPaymentMethods;
}
