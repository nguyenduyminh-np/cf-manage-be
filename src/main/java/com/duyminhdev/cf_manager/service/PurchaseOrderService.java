package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.purchase_order.*;
import com.duyminhdev.cf_manager.dto.response.purchase_order.*;

import java.util.List;

public interface PurchaseOrderService {
    PageResponse<List<PurchaseOrderListItemDTO>> search(PurchaseOrderSearchRequestDTO request);
    List<PurchaseOrderExportDTO> exportData(PurchaseOrderSearchRequestDTO request);
    PurchaseOrderDetailResponseDTO create(PurchaseOrderCreateRequestDTO request);
    PurchaseOrderDetailResponseDTO update(PurchaseOrderUpdateRequestDTO request);
    void updateStatus(PurchaseOrderStatusUpdateRequestDTO request);
    PurchaseOrderDetailResponseDTO getDetail(Integer id);
}