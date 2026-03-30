package com.duyminhdev.cf_manager.dto.response.table;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDetailResponseDTO {

    private Integer tableId;
    private String tableCode;
    private String tableName;

    private String tableStatus;
    private String tableStatusName;

    private Integer floor;
    private Integer slot;

    private Boolean active;
    private LocalDateTime createdTime;
}

