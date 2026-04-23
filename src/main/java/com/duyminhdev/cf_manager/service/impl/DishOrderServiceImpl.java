package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishSearchNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.OrderHistoryNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.*;
import com.duyminhdev.cf_manager.dto.response.dish_order.DishOrderResponseDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order.DishOrderDetailsDTO;
import com.duyminhdev.cf_manager.dto.response.dish_order.OrderHistoryResponseDTO;
import com.duyminhdev.cf_manager.dto.response.payment.*;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.enums.DishOrderStatusCodeEnum;
import com.duyminhdev.cf_manager.enums.PaymentMethodEnum;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.mapper.DishOrderDetailMapper;
import com.duyminhdev.cf_manager.mapper.DishOrderMapper;
import com.duyminhdev.cf_manager.repository.DishOrderDetailRepository;
import com.duyminhdev.cf_manager.repository.DishOrderRepository;
import com.duyminhdev.cf_manager.repository.NativeSqlDishRepository;
import com.duyminhdev.cf_manager.repository.NativeSqlTableBookingRepository;
import com.duyminhdev.cf_manager.repository.impl.NativeSqlOrderHistoryRepositoryImpl;
import com.duyminhdev.cf_manager.service.DishOrderService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishOrderServiceImpl implements DishOrderService {

    private final DishOrderRepository dishOrderRepository;
    private final DishOrderDetailRepository dishOrderDetailRepository;
    private final NativeSqlTableBookingRepository nativeSqlTableBookingRepository;
    private final NativeSqlOrderHistoryRepositoryImpl nativeSqlOrderHistoryRepository;
    private final NativeSqlDishRepository nativeSqlDishRepository;

    private final DishOrderMapper dishOrderMapper;
    private final ServiceSupport serviceSupport;


    @Override
    public PaymentPreviewResponseDTO getPaymentPreview(Integer orderId) {
        // 1. Lấy đơn hàng
        DishOrder order = dishOrderRepository.findByIdAndActiveTrue(orderId).orElseThrow(() -> new InvalidDataException("Khong tim thay don hang de thanh toan với id"+ orderId));

        // 2. Kiểm tra trạng thái có thể thanh toán
        String statusCode = order.getStatus().getDishOrderStatusCode();
        if (DishOrderStatusCodeEnum.CANCEL.getCode().equalsIgnoreCase(statusCode)) {
            throw new InvalidDataException("Đơn hàng đã bị hủy, không thể thanh toán");
        }
        if (DishOrderStatusCodeEnum.PAID.getCode().equalsIgnoreCase(statusCode)) {
            throw new InvalidDataException("Đơn hàng này đã được thanh toán rồi");
        }

        // 3. Lấy danh sách chi tiết mon cua 1 order - 1 order gom nhieu order detail
        List<DishOrderDetail> details = dishOrderDetailRepository.findAllByDishOrderIdAndActiveTrueOrderByCreatedTimeAsc(orderId);

        // 4. Tính toán items và tổng tiền
        List<OrderItemDto> items = details.stream().map(detail -> {
            Dish dish = detail.getDish();
            BigDecimal unitPrice = dish.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(detail.getQuantity()));

            return OrderItemDto.builder()
                    .dishId(dish.getId())
                    .dishCode(dish.getDishName())
                    .quantity(detail.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
        }).collect(Collectors.toList());

        // 5. Tính tổng tiền của 1 hóa đơn - đơn đặt món
        BigDecimal totalAmount = items.stream()
                .map(OrderItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Tạo thong tin bàn cho hóa đơn
        TableEntity table = order.getTable();
        DiningTableDto tableDto = DiningTableDto.builder()
                .id(table.getId())
                .tableCode(table.getTableCode())
                .tableName(table.getTableName())
                .floor(table.getFloor())
                .slot(table.getSlot())
                .status(table.getTableStatus())
                .build();
        // 7. Nhân viên tạo đơn
        Account createdBy = order.getAccount();
        AccountDto accountDto = AccountDto.builder()
                .accountId(createdBy.getId())
                .username(createdBy.getUsername())
                .fullName(createdBy.getFullName())
                .build();

        // 8. Thông tin khách hàng từ booking đang hoạt động
        CustomerInfoDto customerInfo = buildCustomerInfo(table.getId());
        return PaymentPreviewResponseDTO.builder()
                .orderId(order.getId())
                .orderCreatedAt(order.getCreatedTime())
                .diningTable(tableDto)
                .createdBy(accountDto)
                .customer(customerInfo)
                .items(items)
                .totalAmount(totalAmount)
                .suggestedPaymentMethods(List.of(
                        PaymentMethodEnum.CASH.name(),
                        PaymentMethodEnum.BANK_TRANSFER.name()))
                .build();
    }

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

        // 1. Tạo metadata cho đơn đặt hàng
        Account currentAccount = serviceSupport.getCurrentAccount();
        TableEntity table = serviceSupport.getActiveTable(request.getTableId());
        DishOrderStatus status = serviceSupport.getDishOrderStatusByCode(DishOrderStatusCodeEnum.PROCESSING.getCode());

        // 2. Tạo DishOrder , chưa có totalBill
        DishOrder dishOrder = DishOrder.builder()
                .note(request.getDescription())
                .createdTime(Instant.now())
                .active(true)
                .status(status)
                .table(table)
                .account(currentAccount)
                .build();

        // 3. Lưu DishOrder để có ID cho các row order_detail mapping tới (1 order gồm nhiều order_detail đại diện cho mỗi món khác nhau trong 1 đơn order)
        DishOrder savedOrder = dishOrderRepository.save(dishOrder);

        // 4. Tạo danh sách OrderDetail và tính tổng bill
        BigDecimal totalBill = BigDecimal.ZERO;
        List<DishOrderDetail> detailsToSave = new ArrayList<>();

        // duyệt list order detail mà client request:
        // Tính toán totalBill của đơn order, tổng tiền 1 order-detail trong order, init dữ liệu các order-detail của 1 order
        for(DishOrderDetailPayloadDTO payload: request.getDishOrderDetails()){
            Dish dish = serviceSupport.getActiveDish(payload.getDishId());

            // Tính toán thành tiền 1 order detail trong đơn order
            BigDecimal unitPrice = dish.getPrice(); // giá gốc
            // Tổng tiền của 1 order detail
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(payload.getQuantity()));

            DishOrderDetail detail = DishOrderDetail.builder()
                    .dishOrder(savedOrder)
                    .dish(dish)
                    .quantity(payload.getQuantity())
                    .note(payload.getNote())
                    .price(lineTotal)
                    .createdTime(Instant.now())
                    .active(true)
                    .build();
            detailsToSave.add(detail);
            totalBill = totalBill.add(lineTotal);
        }
        // 5. sau khi tính toán , init dữ liệu => save
        List<DishOrderDetail> savedDetails = dishOrderDetailRepository.saveAll(detailsToSave);

        // 6. Cập nhật totalBill cho đơn order tổng
        savedOrder.setTotalBill(totalBill);
        dishOrderRepository.save(savedOrder);

        // 7. đồng bộ trạng thái bàn
        serviceSupport.recomputeAndSyncTableStatus(table.getId());

        // 8. Xây dựng DTO response
        DishOrderResponseDTO response = dishOrderMapper.toResponseDTO(savedOrder);

        // Map danh sách detail với unitPrice từ dish.price
        List<DishOrderDetailsDTO> detailDTOs = savedDetails.stream()
                .map(detail -> {
                    Dish dish = detail.getDish();
                    return DishOrderDetailsDTO.builder()
                            .dishOrderDetailId(detail.getId())
                            .dishOrderId(detail.getDishOrder().getId())
                            .dishId(dish.getId())
                            .dishName(dish.getDishName())
                            .photo(dish.getPhoto())
                            .quantity(detail.getQuantity())
                            .note(detail.getNote())
                            .unitPrice(dish.getPrice())      // Đơn giá gốc
                            .totalPrice(detail.getPrice())   // Thành tiền dòng
                            .build();
                })
                .collect(Collectors.toList());

        response.setDishOrderDetails(detailDTOs);
        return response;
    }

    @Override
    @Transactional
    public DishOrderResponseDTO update(DishOrderUpdateRequestDTO request) {
        // 1. Xác định và validate trạng thái
        String statusCode = request.getDishOrderStatus();
        if (statusCode == null || statusCode.isBlank()) {
            statusCode = DishOrderStatusCodeEnum.PROCESSING.getCode();
        }
        serviceSupport.validateDishOrderStatusCode(statusCode);

        // 2. Lấy đơn hàng hiện tại
        DishOrder existing = dishOrderRepository.findByIdAndActiveTrue(request.getDishOrderId())
                .orElseThrow(() -> new InvalidDataException(
                        "Dish order not found with id: " + request.getDishOrderId()
                ));

        // 3. Lưu lại bàn cũ để đồng bộ sau
        Integer oldTableId = existing.getTable() != null ? existing.getTable().getId() : null;

        // 4. Lấy bàn mới và trạng thái mới
        TableEntity newTable = serviceSupport.getActiveTable(request.getTableId());
        DishOrderStatus newStatus = serviceSupport.getDishOrderStatusByCode(statusCode);

        // 5. Cập nhật thông tin header
        existing.setTable(newTable);
        existing.setStatus(newStatus);
        if (request.getDescription() != null) {
            existing.setNote(request.getDescription());
        }

        // Lưu tạm để đảm bảo có ID khi tạo detail (thực tế đã có)
        DishOrder savedOrder = dishOrderRepository.save(existing);

        // 6. Thay thế chi tiết món và nhận về danh sách đã lưu
        List<DishOrderDetail> savedDetails = replaceOrderDetails(savedOrder, request.getDishOrderDetails());

        // 7. Tính lại tổng bill từ các dòng đã lưu
        BigDecimal totalBill = savedDetails.stream()
                .map(DishOrderDetail::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        savedOrder.setTotalBill(totalBill);
        dishOrderRepository.save(savedOrder);

        // 8. Đồng bộ trạng thái bàn
        if (oldTableId != null && !oldTableId.equals(newTable.getId())) {
            serviceSupport.recomputeAndSyncTableStatus(oldTableId);
        }
        serviceSupport.recomputeAndSyncTableStatus(newTable.getId());

        // 9. Xây dựng response DTO
        DishOrderResponseDTO response = dishOrderMapper.toResponseDTO(savedOrder);

        List<DishOrderDetailsDTO> detailDTOs = savedDetails.stream()
                .map(detail -> {
                    Dish dish = detail.getDish();
                    return DishOrderDetailsDTO.builder()
                            .dishOrderDetailId(detail.getId())
                            .dishOrderId(detail.getDishOrder().getId())
                            .dishId(dish.getId())
                            .dishName(dish.getDishName())
                            .photo(dish.getPhoto())
                            .quantity(detail.getQuantity())
                            .note(detail.getNote())
                            .unitPrice(dish.getPrice())      // Đơn giá gốc hiện tại (có thể khác snapshot)
                            .totalPrice(detail.getPrice())   // Thành tiền dòng (snapshot)
                            .build();
                })
                .collect(Collectors.toList());

        response.setDishOrderDetails(detailDTOs);
        return response;
    }
    @Override
    @Transactional
    /**
     * Cập nhật trạng thái order và đồng bộ lại trạng thái bàn.
     */
    public Boolean updateStatus(DishOrderStatusUpdateRequestDTO request) {
        /**
         * Flow update status order bulk:
         * 1. Validate status
         * 2. Lặp qua danh sách dishOrderId
         * 3. Load order
         * 4. Set status mới
         * 5. Save
         */
        serviceSupport.validateDishOrderStatusCode(request.getDishOrderStatus());

        DishOrderStatus newStatus = serviceSupport.getDishOrderStatusByCode(request.getDishOrderStatus());

        for (Integer dishOrderId : request.getDishOrderIds()) {
            DishOrder existing = dishOrderRepository.findByIdAndActiveTrue(dishOrderId)
                    .orElseThrow(() -> new InvalidDataException(
                            "Dish order not found with id: " + dishOrderId
                    ));

            existing.setStatus(newStatus);
            dishOrderRepository.save(existing);

            // serviceSupport.recomputeAndSyncTableStatus(existing.getTable().getId());
        }

        return true;
    }

    @Override
    public PageResponse<List<OrderHistoryResponseDTO>> searchOrderHistoryByTable(OrderHistorySearchRequestDTO request) {
        PageResponse<List<OrderHistoryNativeResultDTO>> pageResult = nativeSqlOrderHistoryRepository.search(request);

        List<OrderHistoryResponseDTO> mappedRows = pageResult.getRows().stream()
                .map(row -> OrderHistoryResponseDTO.builder()
                        .dishOrderId(row.getDishOrderId())
                        .tableName(row.getTableName())
                        .employeeName(row.getEmployeeName())
                        .orderStatus(row.getOrderStatus())
                        .dishOrderStatusCode(row.getDishOrderStatusCode())
                        .createdAt(row.getCreatedAt())
                        .note(row.getNote())
                        .totalQuantity(row.getTotalQuantity())
                        .totalAmount(row.getTotalAmount())
                        .build())
                .toList();

        PageResponse<List<OrderHistoryResponseDTO>> response = new PageResponse<>();
        response.setRows(mappedRows);
        response.setPageNo(pageResult.getPageNo());
        response.setPageSize(pageResult.getPageSize());
        response.setTotalElements(pageResult.getTotalElements());
        response.setTotalPages(pageResult.getTotalPages());

        return response;
    }

    @Override
    public PageResponse<List<DishSearchNativeResultDTO>> searchDishesForPosOrderDishes(DishSearchRequestDTO request) {
        return nativeSqlDishRepository.search(request);
    }

    private List<DishOrderDetail> replaceOrderDetails(DishOrder order, List<DishOrderDetailPayloadDTO> payloads) {
        // Xóa toàn bộ chi tiết cũ của đơn hàng
        dishOrderDetailRepository.deleteAllByDishOrderId(order.getId());

        List<DishOrderDetail> details = new ArrayList<>();

        for (DishOrderDetailPayloadDTO payload : payloads) {
            Dish dish = serviceSupport.getActiveDish(payload.getDishId());

            // Tính thành tiền dòng = số lượng * đơn giá gốc
            BigDecimal lineTotal = dish.getPrice().multiply(BigDecimal.valueOf(payload.getQuantity()));

            DishOrderDetail detail = DishOrderDetail.builder()
                    .dishOrder(order)
                    .dish(dish)
                    .quantity(payload.getQuantity())
                    .note(payload.getNote())
                    .price(lineTotal)                    // Lưu thành tiền dòng
                    .createdTime(Instant.now())
                    .active(true)
                    .build();

            details.add(detail);
        }

        return dishOrderDetailRepository.saveAll(details);
    }

    private CustomerInfoDto buildCustomerInfo(Integer tableId) {
        return nativeSqlTableBookingRepository.findActiveBookingByTableId(tableId)
                .map(booking -> CustomerInfoDto.builder()
                        .bookingId(booking.getBookingId())
                        .customerName(booking.getCustomerName())
                        .phoneNumber(booking.getPhoneNumber())
                        .build())
                .orElseGet(() -> {
                    // Fallback
                    TableEntity table = serviceSupport.getActiveTable(tableId);
                    return CustomerInfoDto.builder()
                            .customerName("Khách vãng lai")
                            .build();
                });
    }
}
