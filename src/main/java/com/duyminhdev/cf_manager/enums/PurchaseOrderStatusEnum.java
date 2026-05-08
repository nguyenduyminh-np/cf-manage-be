package com.duyminhdev.cf_manager.enums;

import com.duyminhdev.cf_manager.utils.EnumCodeSupport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum PurchaseOrderStatusEnum implements EnumCodeSupport {

    DRAFT("DRAFT", "Bản nháp"),
    PENDING("PENDING", "Chờ duyệt"),
    APPROVED("APPROVED", "Đã duyệt"),
    COMPLETED("COMPLETED", "Hoàn thành"),
    CANCELLED("CANCELLED", "Đã huỷ");

    private final String code;
    private final String label;

    // ── Bảng chuyển đổi trạng thái hợp lệ ──────────────────────────────
    public Set<PurchaseOrderStatusEnum> allowedTransitions() {
        return switch (this) {
            case DRAFT     -> Set.of(PENDING, APPROVED);
            case PENDING   -> Set.of(APPROVED, CANCELLED);
            case APPROVED  -> Set.of(COMPLETED, CANCELLED);
            case COMPLETED -> Set.of();
            case CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(PurchaseOrderStatusEnum target) {
        return allowedTransitions().contains(target);
    }

    // ── Lookup helpers ───────────────────────────────────────────────────
    public static PurchaseOrderStatusEnum fromCode(String code) {
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid purchase order status code: " + code));
    }

    public static boolean isValidCode(String code) {
        return Arrays.stream(values())
                .anyMatch(item -> item.code.equalsIgnoreCase(code));
    }

    // ── State-check helpers ──────────────────────────────────────────────
    public boolean isDraft()      { return this == DRAFT; }
    public boolean isPending()    { return this == PENDING; }
    public boolean isApproved()   { return this == APPROVED; }
    public boolean isCompleted()  { return this == COMPLETED; }
    public boolean isCancelled()  { return this == CANCELLED; }

    /** Trạng thái cuối – không thể sửa hay chuyển tiếp. */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /** Trạng thái cho phép chỉnh sửa nội dung đơn hàng. */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING;
    }
}
