package com.duyminhdev.cf_manager.dto.response.invoice;

import com.duyminhdev.cf_manager.dto.response.payment.*;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class InvoiceDetailResponseDTO {
    private Integer invoiceId;
    private String invoiceCode;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String paymentMethod;
    private Instant createdAt;
    private DiningTableDto diningTable;
    private AccountDto createdBy;          // nhân viên thanh toán
    private CustomerInfoDto customer;
    private List<OrderItemDto> items;     // món trong hóa đơn (từ invoice_detail)
}