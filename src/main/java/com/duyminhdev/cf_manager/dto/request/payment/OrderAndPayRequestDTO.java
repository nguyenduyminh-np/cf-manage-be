package com.duyminhdev.cf_manager.dto.request.payment;

import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderDetailPayloadDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * Request DTO cho API "Đặt món & Thanh toán ngay" (POS quick checkout).
 * Cho phép nhân viên tạo đơn order và thực hiện thanh toán chỉ trong 1 lần gọi API,
 * không cần qua các bước: lưu order → đổi trạng thái → thanh toán riêng biệt.
 */
@Data
public class OrderAndPayRequestDTO {

    // ── Thông tin đơn order ──────────────────────────────────────────────────

    @NotNull(message = "Mã bàn không được để trống")
    @Positive(message = "Mã bàn phải lớn hơn 0")
    private Integer tableId;

    /** Ghi chú chung cho đơn hàng (tuỳ chọn). */
    private String description;

    @NotEmpty(message = "Danh sách món ăn không được để rỗng")
    @Valid
    private List<DishOrderDetailPayloadDTO> dishOrderDetails;

    // ── Thông tin thanh toán ─────────────────────────────────────────────────

    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Pattern(regexp = "CASH|BANK_TRANSFER", message = "Phương thức thanh toán không hợp lệ: chỉ chấp nhận CASH hoặc BANK_TRANSFER")
    private String paymentMethod;
}
