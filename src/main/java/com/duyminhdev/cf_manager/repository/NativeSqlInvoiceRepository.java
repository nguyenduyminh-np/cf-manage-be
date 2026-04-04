package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResultDTO;

import java.util.List;

public interface NativeSqlInvoiceRepository {

    List<InvoiceDetailNativeResultDTO> findInvoiceDetailByInvoiceId(Integer invoiceId);
}
