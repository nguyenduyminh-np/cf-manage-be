package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.voucher.VoucherCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.voucher.AppliedVoucherDTO;
import com.duyminhdev.cf_manager.dto.response.voucher.DiscountPreviewDTO;
import com.duyminhdev.cf_manager.dto.response.voucher.VoucherResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface VoucherService {

    // ── Quản lý voucher (Admin) ───────────────────────────────────────────────

    VoucherResponseDTO create(VoucherCreateRequestDTO request);

    VoucherResponseDTO getById(Integer id);

    List<VoucherResponseDTO> getAll();

    /** Vô hiệu hóa voucher (soft delete). */
    Boolean deactivate(Integer id);

    // ── Tích hợp vào luồng Order / Payment ───────────────────────────────────

    /**
     * Chỉ tính toán discount, KHÔNG thay đổi DB.
     * Gọi ở bước {@code getPaymentPreview} để hiển thị preview cho FE.
     *
     * @param code        mã voucher (case-insensitive)
     * @param totalAmount tổng tiền gốc của đơn hàng
     * @return thông tin giảm giá dự kiến
     */
    DiscountPreviewDTO previewVoucher(String code, BigDecimal totalAmount);

    /**
     * Thực sự áp dụng voucher: tăng usedCount (native UPDATE atomic) và
     * ghi bản ghi {@code voucher_usage}. Chỉ gọi khi thanh toán thành công,
     * trong cùng {@code @Transactional} với Invoice + CashFlow.
     *
     * @param code        mã voucher
     * @param dishOrderId ID đơn hàng đang thanh toán
     * @param totalAmount tổng tiền gốc
     * @return kết quả bao gồm voucherId, discountAmount, finalAmount
     * @throws com.duyminhdev.cf_manager.exceptions.InvalidDataException nếu voucher không còn hợp lệ
     *         (hết lượt khi nhiều request đồng thời)
     */
    AppliedVoucherDTO applyVoucher(String code, Integer dishOrderId, BigDecimal totalAmount);

    /**
     * Hoàn trả voucher khi đơn hàng bị hủy (status CANCEL).
     * Xóa bản ghi {@code voucher_usage} và giảm {@code usedCount}.
     * Không throw exception nếu đơn không có voucher.
     *
     * @param dishOrderId ID đơn hàng bị hủy
     */
    void releaseVoucher(Integer dishOrderId);
}
