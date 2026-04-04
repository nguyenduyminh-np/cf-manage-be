package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderListByTableRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderStatusUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderUpdateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order.DishOrderResponseDTO;

import java.util.List;

public interface DishOrderService {

    /**
     * Lấy danh sách order của một bàn theo thời gian tạo giảm dần.
     */
    List<DishOrderResponseDTO> listByTable(DishOrderListByTableRequestDTO request);

    /**
     * Tạo order món mới cho bàn và đồng bộ trạng thái bàn.
     */
    DishOrderResponseDTO create(DishOrderCreateRequestDTO request);

    /**
     * Cập nhật order món và đồng bộ trạng thái bàn liên quan.
     */
    DishOrderResponseDTO update(DishOrderUpdateRequestDTO request);

    /**
     * Cập nhật trạng thái order món và tính lại trạng thái bàn.
     */
    Boolean updateStatus(DishOrderStatusUpdateRequestDTO request);
}
