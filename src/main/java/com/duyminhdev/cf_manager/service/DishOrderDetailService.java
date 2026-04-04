package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.dish_order_detail.DishOrderDetailListByOrderRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order_detail.DishOrderDetailListByTableRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order_detail.DishGroupedByTableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order_detail.DishOrderDetailResponseDTO;

import java.util.List;

public interface DishOrderDetailService {

    /**
     * Lấy danh sách chi tiết món của một order.
     */
    List<DishOrderDetailResponseDTO> listByOrder(DishOrderDetailListByOrderRequestDTO request);

    /**
     * Lấy danh sách món đã gộp theo bàn phục vụ checkout.
     */
    List<DishGroupedByTableResponseDTO> listByTable(DishOrderDetailListByTableRequestDTO request);
}
