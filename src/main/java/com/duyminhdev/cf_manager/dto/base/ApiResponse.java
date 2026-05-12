package com.duyminhdev.cf_manager.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class ApiResponse<T> implements Serializable {
    private final int status;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * Danh sách cảnh báo nghiệp vụ (không phải lỗi).
     * Null nếu không có warning. FE nên hiển thị dưới dạng toast vàng.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> warnings;

    /**
     * GET, POST — response thông thường
     */
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * POST, PUT, DELETE — không có data
     */
    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * Response có warnings kèm data — HTTP 200 nhưng FE nên hiển thị toast vàng.
     */
    public ApiResponse(int status, String message, T data, List<String> warnings) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.warnings = (warnings != null && !warnings.isEmpty()) ? warnings : null;
    }
}
