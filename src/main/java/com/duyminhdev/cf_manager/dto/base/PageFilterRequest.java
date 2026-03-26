package com.duyminhdev.cf_manager.dto.base;

import lombok.Data;

@Data
public class PageFilterRequest {
    private Integer page;
    private Integer limit;
    private String sortField;
    private String sortDir;
}
