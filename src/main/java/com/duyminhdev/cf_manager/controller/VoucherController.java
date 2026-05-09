package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.request.voucher.VoucherCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.voucher.DiscountPreviewDTO;
import com.duyminhdev.cf_manager.dto.response.voucher.VoucherResponseDTO;
import com.duyminhdev.cf_manager.security.authorization.AdminOrManagerAccess;
import com.duyminhdev.cf_manager.service.VoucherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/voucher")
@RequiredArgsConstructor
@AdminOrManagerAccess
public class VoucherController {

    private final VoucherService voucherService;

    /** Tạo voucher mới. */
    @PostMapping("/create")
    public ApiResponse<VoucherResponseDTO> create(
            @Valid @RequestBody VoucherCreateRequestDTO request
    ) {
        return new ApiResponse<>(200, "CREATE_VOUCHER_SUCCESS", voucherService.create(request));
    }

    /** Lấy toàn bộ danh sách voucher (admin). */
    @PostMapping("/list")
    public ApiResponse<List<VoucherResponseDTO>> getAll() {
        return new ApiResponse<>(200, "GET_VOUCHER_LIST_SUCCESS", voucherService.getAll());
    }

    /** Lấy chi tiết một voucher theo id. */
    @PostMapping("/detail")
    public ApiResponse<VoucherResponseDTO> getById(
            @NotNull @Positive @RequestParam Integer id
    ) {
        return new ApiResponse<>(200, "GET_VOUCHER_DETAIL_SUCCESS", voucherService.getById(id));
    }

    /** Vô hiệu hóa voucher (không xóa vật lý). */
    @PostMapping("/deactivate")
    public ApiResponse<Boolean> deactivate(
            @NotNull @Positive @RequestParam Integer id
    ) {
        return new ApiResponse<>(200, "DEACTIVATE_VOUCHER_SUCCESS", voucherService.deactivate(id));
    }

    /**
     * Preview discount: kiểm tra mã + tính toán giảm giá KHÔNG thay đổi DB.
     * FE gọi khi nhân viên nhập mã voucher ở màn hình preview thanh toán.
     */
    @PostMapping("/preview")
    public ApiResponse<DiscountPreviewDTO> preview(
            @NotBlank @RequestParam String code,
            @NotNull @RequestParam BigDecimal totalAmount
    ) {
        return new ApiResponse<>(200, "PREVIEW_VOUCHER_SUCCESS",
                voucherService.previewVoucher(code, totalAmount));
    }
}
