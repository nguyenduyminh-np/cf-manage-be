package com.duyminhdev.cf_manager.dto.db_result.native_sql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kết quả native query trả về thông tin bàn trống cho chatbot.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableAvailabilityDTO {

    private Integer tableId;
    private String  tableCode;
    private String  tableName;
    private Integer floor;

    /** Số chỗ ngồi */
    private Integer slot;
}
