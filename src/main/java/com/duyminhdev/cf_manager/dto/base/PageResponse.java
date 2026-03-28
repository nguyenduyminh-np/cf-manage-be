package com.duyminhdev.cf_manager.dto.base;

import lombok.Data;

@Data
public class PageResponse<T> {
    private T data;
    private int pageNo;
    private int pageSize;
    private int totalElements;
    private int totalPages;
}
