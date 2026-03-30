package com.duyminhdev.cf_manager.mapper;

import com.duyminhdev.cf_manager.dto.response.dish_category.DishCategoryResponseDTO;
import com.duyminhdev.cf_manager.entity.DishCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = BaseMapperConfig.class)
public interface DishCategoryMapper {

    @Mapping(target = "dishCategoryId", source = "id")
    DishCategoryResponseDTO toResponseDTO(DishCategory entity);
}

