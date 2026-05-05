package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.account.*;
import com.duyminhdev.cf_manager.dto.response.account.*;
import com.duyminhdev.cf_manager.service.AccountService;
import com.duyminhdev.cf_manager.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // giả sử quyền ADMIN
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/search")
    public ApiResponse<PageResponse<List<AccountListItemDTO>>> search(@Valid @RequestBody AccountSearchRequestDTO request) {
        return new ApiResponse<>(200, "SEARCH_SUCCESS", accountService.search(request));
    }

    @PostMapping("/export")
    public void exportExcel(@Valid @RequestBody AccountSearchRequestDTO request,
                            HttpServletResponse response) throws IOException {
        List<AccountExportDTO> items = accountService.exportData(request);
        String fileName = "DANH_SACH_TAI_KHOAN_" + System.currentTimeMillis() + ".xlsx";
        String title = "DANH SÁCH TÀI KHOẢN";
        ExcelUtils.export(response, AccountExportDTO.class, items, fileName, title);
    }

    @PostMapping("/create")
    public ApiResponse<AccountDetailDTO> create(@Valid @RequestBody AccountCreateRequestDTO request) {
        return new ApiResponse<>(201, "CREATE_SUCCESS", accountService.create(request));
    }

    @PostMapping("/update")
    public ApiResponse<AccountDetailDTO> update(@Valid @RequestBody AccountUpdateRequestDTO request) {
        return new ApiResponse<>(200, "UPDATE_SUCCESS", accountService.update(request));
    }

    @PostMapping("/delete")
    public ApiResponse<Void> delete(@Valid @RequestBody AccountDeleteRequestDTO request) {
        accountService.delete(request.getId());
        return new ApiResponse<>(200, "DELETE_SUCCESS");
    }

    @PostMapping("/detail")
    public ApiResponse<AccountDetailDTO> detail(@RequestBody Map<String, Integer> body) {
        Integer id = body.get("id");
        return new ApiResponse<>(200, "DETAIL_SUCCESS", accountService.getDetail(id));
    }
}