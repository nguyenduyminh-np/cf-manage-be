package com.duyminhdev.cf_manager.dto.base;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Kết quả có thể kèm danh sách cảnh báo nghiệp vụ (warnings).
 * Dùng khi thao tác thành công nhưng hệ thống muốn thông báo thêm cho người dùng
 * (ví dụ: bàn có đơn CONFIRMED sắp tới trong 2 tiếng).
 * <p>
 * FE đọc field {@code warnings} để hiển thị toast màu vàng.
 */
@Getter
public class ServiceResult<T> {

    private final T data;
    private final List<String> warnings;

    private ServiceResult(T data, List<String> warnings) {
        this.data = data;
        this.warnings = warnings != null ? List.copyOf(warnings) : List.of();
    }

    /** Kết quả không có warning. */
    public static <T> ServiceResult<T> ok(T data) {
        return new ServiceResult<>(data, List.of());
    }

    /** Kết quả kèm danh sách warning. */
    public static <T> ServiceResult<T> withWarnings(T data, List<String> warnings) {
        return new ServiceResult<>(data, warnings);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Chuyển thành {@link ApiResponse}.
     * Nếu có warnings → truyền vào constructor 4 tham số để FE nhận được.
     */
    public ApiResponse<T> toApiResponse(int status, String message) {
        if (hasWarnings()) {
            return new ApiResponse<>(status, message, data, new ArrayList<>(warnings));
        }
        return new ApiResponse<>(status, message, data);
    }
}
