package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.OrderHistoryNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.OrderHistorySearchRequestDTO;

import java.util.List;

public interface NativeSqlOrderHistoryRepository {
    PageResponse<List<OrderHistoryNativeResultDTO>> search(OrderHistorySearchRequestDTO request);
}
