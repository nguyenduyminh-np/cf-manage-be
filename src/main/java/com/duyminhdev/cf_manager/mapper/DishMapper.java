package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.response.dish.DishResponseDTO;
import com.duyminhdev.cf_manager.entity.Dish;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface DishMapper {

    @Mapping(target = "dishId", source = "id")
    @Mapping(target = "dishCategoryId", source = "dishCategory.id")
    @Mapping(target = "dishCategoryCode", source = "dishCategory.dishCategoryCode")
    @Mapping(target = "dishCategoryName", source = "dishCategory.dishCategoryName")
    DishResponseDTO toResponseDTO(Dish entity);
}

