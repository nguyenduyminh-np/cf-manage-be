package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.request.dish_order_detail.DishOrderDetailListByOrderRequestDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order_detail.DishOrderDetailListByTableRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order_detail.DishGroupedByTableResponseDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order_detail.DishOrderDetailResponseDTO;
import com.duyminhdev.cf_manager.mapper.DishOrderDetailMapper;
import com.duyminhdev.cf_manager.repository.DishOrderDetailRepository;
import com.duyminhdev.cf_manager.repository.NativeSqlDishOrderDetailRepository;
import com.duyminhdev.cf_manager.service.DishOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishOrderDetailServiceImpl implements DishOrderDetailService {

    private final DishOrderDetailRepository dishOrderDetailRepository;
    private final NativeSqlDishOrderDetailRepository nativeSqlDishOrderDetailRepository;
    private final DishOrderDetailMapper dishOrderDetailMapper;

    @Override
    /**
     * Lấy danh sách chi tiết món của một order.
     */
    public List<DishOrderDetailResponseDTO> listByOrder(DishOrderDetailListByOrderRequestDTO request) {
        /**
         * Flow list order details:
         * 1. Query toàn bộ detail active theo dishOrderId
         * 2. Sắp xếp theo thời gian tạo tăng dần (từ repository)
         * 3. Map entity sang response DTO
         */
        return dishOrderDetailRepository.findAllByDishOrderIdAndActiveTrueOrderByCreatedTimeAsc(request.getDishOrderId())
                .stream()
                .map(dishOrderDetailMapper::toResponseDTO)
                .toList();
    }

    @Override
    /**
     * Lấy danh sách món gộp theo bàn để checkout.
     */
    public List<DishGroupedByTableResponseDTO> listByTable(DishOrderDetailListByTableRequestDTO request) {
        /**
         * Flow list grouped dishes by table:
         * 1. Query aggregate món theo bàn từ native repository
         * 2. Chỉ lấy các order còn hiệu lực thanh toán
         * 3. Map native result sang response DTO
         */
        return nativeSqlDishOrderDetailRepository.findGroupedByTableId(request.getTableId())
                .stream()
                .map(dishOrderDetailMapper::toGroupedResponseDTO)
                .toList();
    }
}