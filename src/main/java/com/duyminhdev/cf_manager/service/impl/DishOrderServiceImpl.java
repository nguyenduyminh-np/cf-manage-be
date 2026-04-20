package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.request.dish_order.*;
import com.duyminhdev.cf_manager.dto.response.dish_order.DishOrderResponseDTO;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.enums.DishOrderStatusCodeEnum;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.mapper.DishOrderDetailMapper;
import com.duyminhdev.cf_manager.mapper.DishOrderMapper;
import com.duyminhdev.cf_manager.repository.DishOrderDetailRepository;
import com.duyminhdev.cf_manager.repository.DishOrderRepository;
import com.duyminhdev.cf_manager.service.DishOrderService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishOrderServiceImpl implements DishOrderService {

    private final DishOrderRepository dishOrderRepository;
    private final DishOrderDetailRepository dishOrderDetailRepository;
    private final DishOrderMapper dishOrderMapper;
    private final DishOrderDetailMapper dishOrderDetailMapper;
    private final ServiceSupport serviceSupport;

    @Override
    /**
     * Lấy danh sách order của một bàn theo thời gian tạo giảm dần.
     */
    public List<DishOrderResponseDTO> listByTable(DishOrderListByTableRequestDTO request) {
        /**
         * Flow list orders by table:
         * 1. Query toàn bộ order active theo tableId
         * 2. Sắp xếp theo createdTime giảm dần (từ repository)
         * 3. Map entity sang response DTO
         */
        return dishOrderRepository.findAllByTableIdAndActiveTrueOrderByCreatedTimeDesc(request.getTableId())
                .stream()
                .map(dishOrderMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    /**
     * Tạo order mới và lưu toàn bộ dòng chi tiết món.
     */
    public DishOrderResponseDTO create(DishOrderCreateRequestDTO request) {
        /**
         * Flow create order:
         * 1. Resolve default status = PROCESSING
         * 2. Load table + current account + order status
         * 3. Tạo order header
         * 4. Save order
         * 5. Replace toàn bộ detail lines
         * 6. Recompute trạng thái bàn
         */
        String statusCode = request.getDishOrderStatus();
        if (statusCode == null || statusCode.isBlank()) {
            statusCode = DishOrderStatusCodeEnum.PROCESSING.getCode();
        }
        serviceSupport.validateDishOrderStatusCode(statusCode);

        TableEntity table = serviceSupport.getActiveTable(request.getTableId());
        Account currentAccount = serviceSupport.getCurrentAccount();
        DishOrderStatus dishOrderStatus = serviceSupport.getDishOrderStatusByCode(statusCode);

        DishOrder order = DishOrder.builder()
                .table(table)
                .account(currentAccount)
                .status(dishOrderStatus)
            .createdTime(Instant.now())
                .active(true)
                .build();

        DishOrder savedOrder = dishOrderRepository.save(order);

        replaceOrderDetails(savedOrder, request.getDishOrderDetails());

        serviceSupport.recomputeAndSyncTableStatus(table.getId());

        return dishOrderMapper.toResponseDTO(savedOrder);
    }

    @Override
    @Transactional
    /**
     * Cập nhật order hiện có, hỗ trợ đổi bàn và thay toàn bộ chi tiết món.
     */
    public DishOrderResponseDTO update(DishOrderUpdateRequestDTO request) {
        /**
         * Flow update order:
         * 1. Resolve status
         * 2. Load order cũ
         * 3. Nếu đổi bàn -> cần nhớ oldTableId
         * 4. Update header
         * 5. Replace details
         * 6. Recompute old table + new table
         */
        String statusCode = request.getDishOrderStatus();
        if (statusCode == null || statusCode.isBlank()) {
            statusCode = DishOrderStatusCodeEnum.PROCESSING.getCode();
        }
        serviceSupport.validateDishOrderStatusCode(statusCode);

        DishOrder existing = dishOrderRepository.findByIdAndActiveTrue(request.getDishOrderId())
                .orElseThrow(() -> new InvalidDataException(
                        "Dish order not found with id: " + request.getDishOrderId()
                ));

        Integer oldTableId = existing.getTable() != null ? existing.getTable().getId() : null;

        TableEntity newTable = serviceSupport.getActiveTable(request.getTableId());
        DishOrderStatus dishOrderStatus = serviceSupport.getDishOrderStatusByCode(statusCode);

        existing.setTable(newTable);
        existing.setStatus(dishOrderStatus);

        DishOrder savedOrder = dishOrderRepository.save(existing);

        replaceOrderDetails(savedOrder, request.getDishOrderDetails());

        if (oldTableId != null) {
            serviceSupport.recomputeAndSyncTableStatus(oldTableId);
        }
        serviceSupport.recomputeAndSyncTableStatus(newTable.getId());

        return dishOrderMapper.toResponseDTO(savedOrder);
    }

    @Override
    @Transactional
    /**
     * Cập nhật trạng thái order và đồng bộ lại trạng thái bàn.
     */
    public Boolean updateStatus(DishOrderStatusUpdateRequestDTO request) {
        /**
         * Flow update status order:
         * 1. Validate status
         * 2. Load order
         * 3. Set status mới
         * 4. Save
         * 5. Recompute trạng thái bàn
         */
        serviceSupport.validateDishOrderStatusCode(request.getDishOrderStatus());

        DishOrder existing = dishOrderRepository.findByIdAndActiveTrue(request.getDishOrderId())
                .orElseThrow(() -> new InvalidDataException(
                        "Dish order not found with id: " + request.getDishOrderId()
                ));

        DishOrderStatus newStatus = serviceSupport.getDishOrderStatusByCode(request.getDishOrderStatus());
        existing.setStatus(newStatus);
        dishOrderRepository.save(existing);

        serviceSupport.recomputeAndSyncTableStatus(existing.getTable().getId());

        return true;
    }

    private void replaceOrderDetails(DishOrder order, List<DishOrderDetailPayloadDTO> payloads) {
        /**
         * CURRENT DECISION:
         * - Giữ hard delete
         * - Chưa soft delete
         * - Có TODO để refactor sau nếu cần audit detail history
         */
        dishOrderDetailRepository.deleteAllByDishOrderId(order.getId());

        List<DishOrderDetail> details = payloads.stream()
                .map(payload -> {
                    Dish dish = serviceSupport.getActiveDish(payload.getDishId());

                    DishOrderDetail detail = dishOrderDetailMapper.toNewEntity(payload);

                    /**
                     * Snapshot giá tại thời điểm gọi món.
                     * Không đọc lại giá cũ từ request.
                     */
                    detail.setDishOrder(order);
                    detail.setDish(dish);
                    detail.setPrice(dish.getPrice());
                    detail.setCreatedTime(Instant.now());
                    detail.setActive(true);
                    return detail;
                })
                .toList();

        dishOrderDetailRepository.saveAll(details);
    }
}
