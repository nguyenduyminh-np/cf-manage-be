package com.duyminhdev.cf_manager.dto.request.invoice;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InvoiceSearchRequestDTO {
    private String invoiceCode;          // tìm kiếm gần đúng
    private String paymentStatus;        // chính xác
    private String paymentMethod;        // chính xác
    private BigDecimal totalAmountFrom;  // >=
    private BigDecimal totalAmountTo;    // <=
    private Integer page;                // 0‑based
    private Integer limit;
    private String sortField;            // mặc định "createdAt"
    private String sortDir;              // asc/desc
}