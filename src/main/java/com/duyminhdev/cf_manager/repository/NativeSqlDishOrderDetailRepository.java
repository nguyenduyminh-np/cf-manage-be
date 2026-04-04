package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishGroupedByTableNativeResultDTO;

import java.util.List;

public interface NativeSqlDishOrderDetailRepository {

    List<DishGroupedByTableNativeResultDTO> findGroupedByTableId(Integer tableId);
}
