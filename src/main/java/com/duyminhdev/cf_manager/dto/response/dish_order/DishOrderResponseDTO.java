package com.duyminhdev.cf_manager.dto.response.dish_order;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishOrderResponseDTO {


    private Integer dishOrderId;

    private Integer tableId;
    private String tableCode;
    private String tableName;

    private Integer accountId;
    private String accountUsername;
    private String accountFullName;

    private Integer dishOrderStatusId;
    private String dishOrderStatus;
    private String dishOrderStatusName;

    private String note;
    private Boolean active;
    private Instant createdTime;
}

