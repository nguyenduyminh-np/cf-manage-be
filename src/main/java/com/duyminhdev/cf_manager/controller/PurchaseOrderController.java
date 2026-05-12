package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.purchase_order.*;
import com.duyminhdev.cf_manager.dto.response.purchase_order.*;
import com.duyminhdev.cf_manager.security.authorization.AdminOnlyAccess;
import com.duyminhdev.cf_manager.service.PurchaseOrderService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/purchase-order")
@RequiredArgsConstructor
@AdminOnlyAccess
public class PurchaseOrderController {

    private final PurchaseOrderService service;

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<PurchaseOrderListItemDTO>>> search(@Valid @RequestBody PurchaseOrderSearchRequestDTO request) {
        return new ApiResponse<>(200, "SEARCH_SUCCESS", service.search(request));
    }

    @PostMapping("/export")
    public void exportExcel(@Valid @RequestBody PurchaseOrderSearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<PurchaseOrderExportDTO> items = service.exportData(request);
        String fileName = "DANH_SACH_DON_NHAP_HANG_" + System.currentTimeMillis() + ".xlsx";
        String title = "DANH SÁCH ĐƠN NHẬP HÀNG";

        ExcelUtils.export(response, PurchaseOrderExportDTO.class, items, fileName, title);
    }

    @PostMapping("/create")
    public ApiResponse<PurchaseOrderDetailResponseDTO> create(@Valid @RequestBody PurchaseOrderCreateRequestDTO request) {
        return new ApiResponse<>(201, "CREATE_SUCCESS", service.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<PurchaseOrderDetailResponseDTO> update(@Valid @RequestBody PurchaseOrderUpdateRequestDTO request) {
        return new ApiResponse<>(200, "UPDATE_SUCCESS", service.update(request));
    }

    @PostMapping("/status")
    public ApiResponse<Void> updateStatus(@Valid @RequestBody PurchaseOrderStatusUpdateRequestDTO request) {
        service.updateStatus(request);
        return new ApiResponse<>(200, "STATUS_UPDATED");
    }

    @PostMapping("/detail")
    public ApiResponse<PurchaseOrderDetailResponseDTO> detail(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        return new ApiResponse<>(200, "DETAIL_SUCCESS", service.getDetail(id));
    }

    @PostMapping("/danh-sach-nha-kho")
    public ApiResponse<List<PurchaseOrderWarehouseSelectDTO>> danhSachNhaKho() {
        return new ApiResponse<>(200, "GET_DANH_SACH_NHA_KHO_SUCCESS", service.getDanhSachNhaKho());
    }

    @PostMapping("/danh-sach-nha-cung-cap")
    public ApiResponse<List<PurchaseOrderSupplierSelectDTO>> danhSachNhaCungCap() {
        return new ApiResponse<>(200, "GET_DANH_SACH_NHA_CUNG_CAP_SUCCESS", service.getDanhSachNhaCungCap());
    }

    @PostMapping("/danh-sach-nguyen-lieu-theo-ncc")
    public ApiResponse<List<PurchaseOrderIngredientSelectDTO>> danhSachNguyenLieuTheoNcc(
            @Valid @RequestBody PurchaseOrderIngredientBySupplierRequestDTO request) {
        return new ApiResponse<>(200, "GET_DANH_SACH_NGUYEN_LIEU_THEO_NCC_SUCCESS",
                service.getDanhSachNguyenLieuTheoNcc(request.getSupplierId()));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        service.delete(id);
        return new ApiResponse<>(200, "DELETE_SUCCESS");
    }
}
