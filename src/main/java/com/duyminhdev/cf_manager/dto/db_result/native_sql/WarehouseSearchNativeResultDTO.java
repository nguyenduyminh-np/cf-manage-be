package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class WarehouseSearchNativeResultDTO {
    private Integer id;
    private String warehouseCode;
    private String warehouseName;
    private String location;
    private String note;
    private Instant createdTime;
    private Boolean active;
    private Integer ingredientCount;
}
