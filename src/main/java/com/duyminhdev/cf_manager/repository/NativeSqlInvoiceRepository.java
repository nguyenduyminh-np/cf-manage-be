package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceSearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.invoice.InvoiceSearchRequestDTO;

import java.util.List;
import java.util.Optional;

public interface NativeSqlInvoiceRepository {
    Optional<InvoiceDetailNativeResult> findInvoiceDetailById(Integer invoiceId);
    List<InvoiceSearchNativeResult> searchInvoices(InvoiceSearchRequestDTO request, int offset, int limit);
    long countInvoices(InvoiceSearchRequestDTO request);
    List<InvoiceDetailNativeResultDTO> findInvoiceDetailByInvoiceId(Integer invoiceId);
}
