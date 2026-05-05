package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.supplier.*;
import com.duyminhdev.cf_manager.dto.response.supplier.*;

import java.util.List;

public interface SupplierService {
    PageResponse<List<SupplierListItemDTO>> search(SupplierSearchRequestDTO request);
    List<SupplierExportDTO> exportData(SupplierSearchRequestDTO request);
    SupplierDetailResponseDTO create(SupplierCreateRequestDTO request);
    SupplierDetailResponseDTO update(SupplierUpdateRequestDTO request);
    void delete(Integer id);
    SupplierDetailResponseDTO getDetail(Integer id);
    List<SupplierOptionDTO> getOptions();
}