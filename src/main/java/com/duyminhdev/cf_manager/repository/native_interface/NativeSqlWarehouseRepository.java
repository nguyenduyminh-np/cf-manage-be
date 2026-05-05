package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.WarehouseSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.warehouse.WarehouseSearchRequestDTO;
import java.util.List;

public interface NativeSqlWarehouseRepository {
    PageResponse<List<WarehouseSearchNativeResultDTO>> search(WarehouseSearchRequestDTO request);
    List<WarehouseSearchNativeResultDTO> findAllByCriteria(WarehouseSearchRequestDTO request);
}
