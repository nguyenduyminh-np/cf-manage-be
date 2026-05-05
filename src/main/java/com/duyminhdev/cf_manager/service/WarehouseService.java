package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.warehouse.*;
import com.duyminhdev.cf_manager.dto.response.warehouse.*;

import java.util.List;

public interface WarehouseService {
    PageResponse<List<WarehouseResponseDTO>> search(WarehouseSearchRequestDTO request);
    List<WarehouseExportDTO> exportData(WarehouseSearchRequestDTO request);
    WarehouseDetailResponseDTO create(WarehouseCreateRequestDTO request);
    WarehouseDetailResponseDTO update(WarehouseUpdateRequestDTO request);
    void delete(WarehouseIdRequest request);
    WarehouseDetailResponseDTO getDetail(WarehouseIdRequest request);
    List<WarehouseOptionDTO> getOptions();
}
