package com.duyminhdev.cf_manager.dto.request.voucher;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class VoucherCreateRequestDTO {

    @NotBlank(message = "Mã voucher không được để trống")
    @Size(max = 50, message = "Mã voucher tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_\\-]+$", message = "Mã voucher chỉ gồm chữ hoa, số, dấu _ hoặc -")
    private String code;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @NotBlank(message = "Loại giảm giá không được để trống")
    @Pattern(regexp = "PERCENT|FIXED", message = "Loại giảm giá phải là PERCENT hoặc FIXED")
    private String discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    /** Đơn tối thiểu để áp dụng. Null = không giới hạn. */
    @DecimalMin(value = "0", inclusive = false, message = "Đơn tối thiểu phải lớn hơn 0")
    private BigDecimal minOrderAmount;

    /** Trần giảm tối đa (chỉ dùng với PERCENT). Null = không giới hạn. */
    @DecimalMin(value = "0", inclusive = false, message = "Giảm tối đa phải lớn hơn 0")
    private BigDecimal maxDiscount;

    /** Số lượt tối đa. Null = không giới hạn. */
    @Min(value = 1, message = "Số lượt phải ít nhất là 1")
    private Integer usageLimit;

    private Instant startDate;
    private Instant endDate;
}
