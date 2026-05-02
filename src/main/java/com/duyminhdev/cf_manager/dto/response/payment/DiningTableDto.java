package com.duyminhdev.cf_manager.dto.response.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiningTableDto {
    private Integer id;
    private String tableCode;
    private String tableName;
    private Integer floor;
    private Integer slot;
}