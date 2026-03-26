package com.duyminhdev.cf_manager.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class ApiResponse<T> implements Serializable {
    private final int status;
    private final String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * GET, POST
     */
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    /**
     * POST, PUT, DELETE
     */
    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}


