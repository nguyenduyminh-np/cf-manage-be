package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.response.dish_order.DishOrderResponseDTO;
import com.duyminhdev.cf_manager.entity.DishOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class, uses = EnumDisplayMapper.class)
public interface DishOrderMapper {

    @Mapping(target = "dishOrderId", source = "id")
    @Mapping(target = "tableId", source = "table.id")
    @Mapping(target = "tableCode", source = "table.tableCode")
    @Mapping(target = "tableName", source = "table.tableName")
    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "accountUsername", source = "account.username")
    @Mapping(target = "accountFullName", source = "account.fullName")
    @Mapping(target = "dishOrderStatusId", source = "status.id")
    @Mapping(target = "dishOrderStatus", source = "status.dishOrderStatusCode")
    @Mapping(target = "dishOrderStatusName", source = "status.dishOrderStatusCode", qualifiedByName = "toDishOrderStatusName")
    DishOrderResponseDTO toResponseDTO(DishOrder entity);
}

