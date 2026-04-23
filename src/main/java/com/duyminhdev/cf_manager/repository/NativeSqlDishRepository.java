package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishSearchRequestDTO;

import java.util.List;

public interface NativeSqlDishRepository {

    PageResponse<List<DishSearchNativeResultDTO>> search(DishSearchRequestDTO request);

}