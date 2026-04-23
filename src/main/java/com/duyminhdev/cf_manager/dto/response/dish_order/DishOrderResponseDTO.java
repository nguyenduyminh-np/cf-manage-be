package com.duyminhdev.cf_manager.dto.response.dish_order;

import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderDetailPayloadDTO;
import jakarta.validation.Valid;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DishOrderResponseDTO {
    private Integer dishOrderId;
    private Integer tableId;
    private Integer accountId;
    private String accountName;
    private String note;
    private BigDecimal totalBill;

    private Integer dishOrderStatusId;
    private String dishOrderStatusName;
    private Instant createdTime;

    private List<DishOrderDetailsDTO> dishOrderDetails;
}

