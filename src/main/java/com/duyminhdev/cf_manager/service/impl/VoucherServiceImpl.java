package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.request.voucher.VoucherCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.voucher.AppliedVoucherDTO;
import com.duyminhdev.cf_manager.dto.response.voucher.DiscountPreviewDTO;
import com.duyminhdev.cf_manager.dto.response.voucher.VoucherResponseDTO;
import com.duyminhdev.cf_manager.entity.DishOrder;
import com.duyminhdev.cf_manager.entity.Voucher;
import com.duyminhdev.cf_manager.entity.VoucherUsage;
import com.duyminhdev.cf_manager.enums.DiscountTypeEnum;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.DishOrderRepository;
import com.duyminhdev.cf_manager.repository.VoucherRepository;
import com.duyminhdev.cf_manager.repository.VoucherUsageRepository;
import com.duyminhdev.cf_manager.service.VoucherService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final DishOrderRepository dishOrderRepository;
    private final ServiceSupport serviceSupport;

    // ── CRUD Admin ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VoucherResponseDTO create(VoucherCreateRequestDTO request) {
        // Kiểm tra mã voucher chưa tồn tại
        if (voucherRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new InvalidDataException("Mã voucher đã tồn tại: " + request.getCode());
        }

        // Validate: PERCENT value phải từ 1–100
        if (DiscountTypeEnum.PERCENT.getCode().equalsIgnoreCase(request.getDiscountType())) {
            if (request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new InvalidDataException("Giá trị giảm theo % không được vượt quá 100");
            }
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType().toUpperCase())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscount(request.getMaxDiscount())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .createdBy(serviceSupport.getCurrentAccount())  // Ghi nhận người tạo
                .createdAt(Instant.now())
                .build();

        return toResponseDTO(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponseDTO getById(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy voucher với id: " + id));
        return toResponseDTO(voucher);
    }

    @Override
    public List<VoucherResponseDTO> getAll() {
        return voucherRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public Boolean deactivate(Integer id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy voucher với id: " + id));
        voucher.setActive(false);
        voucherRepository.save(voucher);
        return true;
    }

    // ── Logic tích hợp Order / Payment ───────────────────────────────────────

    /**
     * {@inheritDoc}
     * Read-only: không thay đổi bất kỳ dữ liệu nào trong DB.
     */
    @Override
    public DiscountPreviewDTO previewVoucher(String code, BigDecimal totalAmount) {
        Voucher voucher = findValidVoucher(code);
        validateMinOrderAmount(voucher, totalAmount);

        BigDecimal discountAmount = calculateDiscount(voucher, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        return DiscountPreviewDTO.builder()
                .voucherCode(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .finalAmount(finalAmount)
                .build();
    }

    /**
     * {@inheritDoc}
     * Ghi DB: tăng usedCount (native UPDATE atomic) + insert voucher_usage.
     * Phải được gọi trong cùng @Transactional với processPayment/orderAndPay.
     */
    @Override
    @Transactional
    public AppliedVoucherDTO applyVoucher(String code, Integer dishOrderId, BigDecimal totalAmount) {
        Voucher voucher = findValidVoucher(code);
        validateMinOrderAmount(voucher, totalAmount);

        // Tăng usedCount bằng native UPDATE atomic để chống race condition
        // Nếu affected rows = 0 → voucher hết lượt hoặc hết hạn ngay tại thời điểm thanh toán
        int updated = voucherRepository.incrementUsedCount(voucher.getId());
        if (updated == 0) {
            throw new InvalidDataException(
                    "Voucher '" + code + "' đã hết lượt sử dụng hoặc không còn hiệu lực"
            );
        }

        BigDecimal discountAmount = calculateDiscount(voucher, totalAmount);
        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        // Ghi log lịch sử sử dụng
        DishOrder dishOrder = dishOrderRepository.findByIdAndActiveTrue(dishOrderId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đơn hàng: " + dishOrderId));

        VoucherUsage usage = VoucherUsage.builder()
                .voucher(voucher)
                .dishOrder(dishOrder)
                .account(serviceSupport.getCurrentAccount())
                .discountAmount(discountAmount)
                .usedAt(Instant.now())
                .build();
        voucherUsageRepository.save(usage);

        return AppliedVoucherDTO.builder()
                .voucherId(voucher.getId())
                .voucherCode(voucher.getCode())
                .discountAmount(discountAmount)
                .totalAmount(totalAmount)
                .finalAmount(finalAmount)
                .build();
    }

    /**
     * {@inheritDoc}
     * Hoàn trả voucher khi hủy đơn: xóa voucher_usage + giảm usedCount.
     * Nếu đơn không có voucher → bỏ qua (không throw).
     */
    @Override
    @Transactional
    public void releaseVoucher(Integer dishOrderId) {
        DishOrder dishOrder = dishOrderRepository.findByIdAndActiveTrue(dishOrderId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đơn hàng: " + dishOrderId));

        // Nếu đơn không gắn voucher → không cần làm gì
        if (dishOrder.getVoucher() == null) {
            return;
        }

        Integer voucherId = dishOrder.getVoucher().getId();

        // 1. Xóa bản ghi usage
        voucherUsageRepository.deleteByDishOrderId(dishOrderId);

        // 2. Giảm usedCount (đảm bảo >= 0 nhờ điều kiện trong native query)
        voucherRepository.decrementUsedCount(voucherId);

        // 3. Clear thông tin voucher trên DishOrder
        dishOrder.setVoucher(null);
        dishOrder.setDiscountAmount(null);
        dishOrder.setFinalTotal(null);
        dishOrderRepository.save(dishOrder);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Tìm voucher hợp lệ: active, còn trong thời hạn, còn lượt dùng.
     * Dùng chung cho cả preview và apply.
     */
    private Voucher findValidVoucher(String code) {
        Voucher voucher = voucherRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .orElseThrow(() -> new InvalidDataException("Mã voucher không hợp lệ hoặc đã bị vô hiệu: " + code));

        Instant now = Instant.now();

        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            throw new InvalidDataException("Voucher '" + code + "' chưa đến ngày áp dụng");
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            throw new InvalidDataException("Voucher '" + code + "' đã hết hạn");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new InvalidDataException("Voucher '" + code + "' đã hết lượt sử dụng");
        }

        return voucher;
    }

    private void validateMinOrderAmount(Voucher voucher, BigDecimal totalAmount) {
        if (voucher.getMinOrderAmount() != null
                && totalAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new InvalidDataException(
                    "Đơn hàng chưa đủ điều kiện áp dụng voucher. "
                    + "Tối thiểu: " + voucher.getMinOrderAmount() + "đ"
            );
        }
    }

    /**
     * Tính số tiền giảm thực tế:
     * <ul>
     *   <li>PERCENT: discountAmount = totalAmount × (value/100), giới hạn bởi maxDiscount</li>
     *   <li>FIXED:   discountAmount = min(value, totalAmount)</li>
     * </ul>
     */
    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal totalAmount) {
        DiscountTypeEnum type = DiscountTypeEnum.fromCode(voucher.getDiscountType());

        BigDecimal discount;
        if (type == DiscountTypeEnum.PERCENT) {
            discount = totalAmount
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            // Áp trần maxDiscount nếu có
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        } else {
            // FIXED: không giảm nhiều hơn tổng tiền
            discount = voucher.getDiscountValue().min(totalAmount);
        }

        return discount;
    }

    private VoucherResponseDTO toResponseDTO(Voucher voucher) {
        return VoucherResponseDTO.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderAmount(voucher.getMinOrderAmount())
                .maxDiscount(voucher.getMaxDiscount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .active(voucher.getActive())
                .createdBy(voucher.getCreatedBy() != null ? voucher.getCreatedBy().getId() : null)
                .createdAt(voucher.getCreatedAt())
                .build();
    }
}
