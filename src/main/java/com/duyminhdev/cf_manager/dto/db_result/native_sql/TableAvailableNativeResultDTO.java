package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableAvailableNativeResultDTO {

    private Integer tableId;
    private String tableName;
    private String tableCode;
    private String tableStatus;
    private Integer floor;
    private Integer slot;
}