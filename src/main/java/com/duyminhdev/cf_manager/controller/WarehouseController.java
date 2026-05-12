package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.warehouse.*;
import com.duyminhdev.cf_manager.dto.response.warehouse.*;
import com.duyminhdev.cf_manager.security.authorization.AdminOnlyAccess;
import com.duyminhdev.cf_manager.service.WarehouseService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
@AdminOnlyAccess
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<WarehouseResponseDTO>>> search(
            @Valid @RequestBody WarehouseSearchRequestDTO request) {
        return new ApiResponse<>(200, "SEARCH_WAREHOUSE_SUCCESS", warehouseService.search(request));
    }

    @PostMapping("/export")
    public void exportExcel(@Valid @RequestBody WarehouseSearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<WarehouseExportDTO> items = warehouseService.exportData(request);
        String fileName = "DANH_SACH_NHA_KHO_" + System.currentTimeMillis() + ".xlsx";
        ExcelUtils.export(response, WarehouseExportDTO.class, items, fileName, "DANH SÁCH NHÀ KHO");
    }

    @PostMapping("/create")
    public ApiResponse<WarehouseDetailResponseDTO> create(
            @Valid @RequestBody WarehouseCreateRequestDTO request) {
        return new ApiResponse<>(201, "CREATE_WAREHOUSE_SUCCESS", warehouseService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<WarehouseDetailResponseDTO> update(
            @Valid @RequestBody WarehouseUpdateRequestDTO request) {
        return new ApiResponse<>(200, "UPDATE_WAREHOUSE_SUCCESS", warehouseService.update(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody WarehouseIdRequest request) {
        warehouseService.delete(request);
        return new ApiResponse<>(200, "DELETE_WAREHOUSE_SUCCESS");
    }

    @PostMapping("/detail")
    public ApiResponse<WarehouseDetailResponseDTO> detail(@Valid @RequestBody WarehouseIdRequest request) {
        return new ApiResponse<>(200, "GET_WAREHOUSE_DETAIL_SUCCESS", warehouseService.getDetail(request));
    }

    @PostMapping("/options")
    public ApiResponse<List<WarehouseOptionDTO>> options() {
        return new ApiResponse<>(200, "WAREHOUSE_OPTIONS_SUCCESS", warehouseService.getOptions());
    }
}
