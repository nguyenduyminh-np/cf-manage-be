package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderDetailPayloadDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order_detail.DishGroupedByTableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order_detail.DishOrderDetailResponseDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishGroupedByTableNativeResultDTO;
import com.duyminhdev.cf_manager.entity.DishOrderDetail;
import org.mapstruct.*;

@Mapper(config = BaseMapperConfig.class)
public interface DishOrderDetailMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dishOrder", ignore = true)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "active", ignore = true)
    DishOrderDetail toNewEntity(DishOrderDetailPayloadDTO request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dishOrder", ignore = true)
    @Mapping(target = "dish", ignore = true)
    @Mapping(target = "price", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromPayload(DishOrderDetailPayloadDTO request, @MappingTarget DishOrderDetail entity);

    @Mapping(target = "dishOrderDetailId", source = "id")
    @Mapping(target = "dishOrderId", source = "dishOrder.id")
    @Mapping(target = "dishId", source = "dish.id")
    @Mapping(target = "dishCode", source = "dish.dishCode")
    @Mapping(target = "dishName", source = "dish.dishName")
    @Mapping(target = "dishPhoto", source = "dish.photo")
    @Mapping(target = "totalPrice", expression = "java(entity.getPrice() != null && entity.getQuantity() != null ? entity.getPrice().multiply(java.math.BigDecimal.valueOf(entity.getQuantity())) : null)")
    DishOrderDetailResponseDTO toResponseDTO(DishOrderDetail entity);

    DishGroupedByTableResponseDTO toGroupedResponseDTO(DishGroupedByTableNativeResultDTO source);
}

