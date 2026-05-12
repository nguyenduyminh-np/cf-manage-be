package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.supplier.*;
import com.duyminhdev.cf_manager.dto.response.supplier.*;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.security.authorization.AdminOnlyAccess;
import com.duyminhdev.cf_manager.service.SupplierService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/supplier")
@RequiredArgsConstructor
@AdminOnlyAccess
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<SupplierListItemDTO>>> search(
            @Valid @RequestBody SupplierSearchRequestDTO request) {
        return new ApiResponse<>(200, "SEARCH_SUPPLIER_SUCCESS", supplierService.search(request));
    }

    @PostMapping("/export")
    public void exportExcel(@Valid @RequestBody SupplierSearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<SupplierExportDTO> items = supplierService.exportData(request);

        String fileName = "DANH_SACH_NHA_CUNG_CAP_" + System.currentTimeMillis() + ".xlsx";
        String title = "DANH SÁCH NHÀ CUNG CẤP";

        ExcelUtils.export(response, SupplierExportDTO.class, items, fileName, title);
    }

    @PostMapping("/create")
    public ApiResponse<SupplierDetailResponseDTO> create(
            @Valid @RequestBody SupplierCreateRequestDTO request) {
        return new ApiResponse<>(201, "CREATE_SUPPLIER_SUCCESS", supplierService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<SupplierDetailResponseDTO> update(
            @Valid @RequestBody SupplierUpdateRequestDTO request) {
        return new ApiResponse<>(200, "UPDATE_SUPPLIER_SUCCESS", supplierService.update(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        if (id == null) throw new InvalidDataException("id is required");
        supplierService.delete(id);
        return new ApiResponse<>(200, "DELETE_SUPPLIER_SUCCESS");
    }

    @PostMapping("/detail")
    public ApiResponse<SupplierDetailResponseDTO> detail(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        if (id == null) throw new InvalidDataException("id is required");
        return new ApiResponse<>(200, "SUPPLIER_DETAIL_SUCCESS", supplierService.getDetail(id));
    }

    @PostMapping("/options")
    public ApiResponse<List<SupplierOptionDTO>> options() {
        return new ApiResponse<>(200, "SUPPLIER_OPTIONS_SUCCESS", supplierService.getOptions());
    }
}