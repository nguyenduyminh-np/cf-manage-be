package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.constant.VnPayConstant;
import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.DishGroupedByTableNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResult;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceDetailNativeResultDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.InvoiceSearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.invoice.*;
import com.duyminhdev.cf_manager.dto.response.invoice.*;
import com.duyminhdev.cf_manager.dto.response.payment.AccountDto;
import com.duyminhdev.cf_manager.dto.response.payment.CustomerInfoDto;
import com.duyminhdev.cf_manager.dto.response.payment.DiningTableDto;
import com.duyminhdev.cf_manager.dto.response.payment.OrderItemDto;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.enums.DishOrderStatusCodeEnum;
import com.duyminhdev.cf_manager.enums.PaymentMethodEnum;
import com.duyminhdev.cf_manager.enums.PaymentStatusEnum;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.mapper.InvoiceDetailPayloadMapper;
import com.duyminhdev.cf_manager.mapper.InvoiceMapper;
import com.duyminhdev.cf_manager.repository.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlDishOrderDetailRepository;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlInvoiceRepository;
import com.duyminhdev.cf_manager.service.InvoiceService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final DishOrderRepository dishOrderRepository;
    private final TableBookingRepository tableBookingRepository;
    private final NativeSqlDishOrderDetailRepository nativeSqlDishOrderDetailRepository;
    private final NativeSqlInvoiceRepository nativeSqlInvoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoiceDetailPayloadMapper invoiceDetailPayloadMapper;
    private final ServiceSupport serviceSupport;

    @Override
    public PageResponse<List<InvoiceListItemDTO>> search(InvoiceSearchRequestDTO request) {
        int page = request.getPage() != null && request.getPage() >= 0 ? request.getPage() : 0;
        int limit = request.getLimit() != null && request.getLimit() > 0 ? request.getLimit() : 20;
        int offset = page * limit;

        List<InvoiceSearchNativeResult> rows = nativeSqlInvoiceRepository.searchInvoices(request, offset, limit);
        long total = nativeSqlInvoiceRepository.countInvoices(request);

        List<InvoiceListItemDTO> list = rows.stream()
                .map(r -> InvoiceListItemDTO.builder()
                        .id(r.getId())
                        .invoiceCode(r.getInvoiceCode())
                        .totalAmount(r.getTotalAmount())
                        .paymentStatus(r.getPaymentStatus())
                        .paymentMethod(r.getPaymentMethod())
                        .createdAt(r.getCreatedAt())
                        .fullName(r.getFullName())
                        .bookingInvoiceCode(r.getBookingInvoiceCode())
                        .build())
                .collect(Collectors.toList());

        PageResponse<List<InvoiceListItemDTO>> response = new PageResponse<>();
        response.setRows(list);
        response.setPageNo(page);
        response.setPageSize(limit);
        response.setTotalElements((int) total);
        response.setTotalPages((int) Math.ceil((double) total / limit));
        return response;
    }

    @Override
    public List<InvoiceExportDTO> exportData(InvoiceSearchRequestDTO request) {
        long total = nativeSqlInvoiceRepository.countInvoices(request);
        if (total <= 0) {
            return List.of();
        }

        List<InvoiceSearchNativeResult> rows = nativeSqlInvoiceRepository.searchInvoices(request, 0, (int) total);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return rows.stream()
                .map(r -> InvoiceExportDTO.builder()
                        .invoiceCode(r.getInvoiceCode())
                        .totalAmount(r.getTotalAmount())
                        .paymentStatus(r.getPaymentStatus())
                        .paymentMethod(r.getPaymentMethod())
                        .createdAt(r.getCreatedAt() != null ? formatter.format(r.getCreatedAt()) : null)
                        .fullName(r.getFullName())
                        .bookingInvoiceCode(r.getBookingInvoiceCode())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceDetailResponseDTO getDetail(Integer invoiceId) {
        InvoiceDetailNativeResult result = nativeSqlInvoiceRepository.findInvoiceDetailById(invoiceId)
                .orElseThrow(() -> new InvalidDataException("Hóa đơn không tồn tại"));

        // Lấy chi tiết món từ invoice_detail
        List<InvoiceDetail> details = invoiceDetailRepository.findAllByInvoiceId(invoiceId);
        List<OrderItemDto> items = details.stream().map(d -> {
            BigDecimal unitPrice = d.getUnitPrice();
            return OrderItemDto.builder()
                    .dishId(d.getDish().getId())
                    .dishCode(d.getDish().getDishCode())
                    .dishName(d.getDish().getDishName())
                    .quantity(d.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(unitPrice.multiply(BigDecimal.valueOf(d.getQuantity())))
                    .build();
        }).collect(Collectors.toList());

        // Build thông tin bàn, account, customer
        DiningTableDto tableDto = DiningTableDto.builder()
                .id(result.getTableId())
                .tableCode(result.getTableCode())
                .tableName(result.getTableName())
                .floor(result.getFloor())
                .slot(result.getSlot())
                .build();

        AccountDto accountDto = AccountDto.builder()
                .accountId(result.getAccountId())
                .username(result.getUsername())
                .fullName(result.getFullName())
                .build();

        CustomerInfoDto customerDto = CustomerInfoDto.builder()
                .bookingId(result.getBookingId())
                .customerName(result.getCustomerName() != null ? result.getCustomerName() : "Khách vãng lai")
                .phoneNumber(result.getPhoneNumber())
                .build();

        return InvoiceDetailResponseDTO.builder()
                .invoiceId(result.getInvoiceId())
                .invoiceCode(result.getInvoiceCode())
                .totalAmount(result.getTotalAmount())
                .paymentStatus(result.getPaymentStatus())
                .paymentMethod(result.getPaymentMethod())
                .createdAt(result.getCreatedAt())
                .diningTable(tableDto)
                .createdBy(accountDto)
                .customer(customerDto)
                .items(items)
                .build();
    }


    @Override
    /**
     * Đếm số hóa đơn trong ngày và trả về mã hóa đơn kế tiếp.
     */
    public InvoiceCountResponseDTO count(InvoiceCountRequestDTO request) {
        /**
         * Flow:
         * 1. Đếm số invoice hôm nay
         * 2. Sinh next invoice code
         */
        long totalInvoicesToday = countInvoicesToday();

        return InvoiceCountResponseDTO.builder()
                .totalInvoices(totalInvoicesToday)
                .nextInvoiceCode(generateNextInvoiceCode())
                .build();
    }

    @Override
    @Transactional
    /**
     * Tạo hóa đơn mới, lưu chi tiết hóa đơn và xử lý luồng thanh toán tương ứng.
     */
    public InvoiceResponseDTO create(InvoiceCreateRequestDTO request) {

        serviceSupport.validatePaymentMethodCode(request.getPaymentMethod());

        String paymentStatus = request.getPaymentStatus();
        if (paymentStatus == null || paymentStatus.isBlank()) {
            paymentStatus = PaymentStatusEnum.PENDING.getCode();
        }
        serviceSupport.validatePaymentStatusCode(paymentStatus);

        TableEntity table = serviceSupport.getActiveTable(request.getTableId());
        Account currentAccount = serviceSupport.getCurrentAccount();

        validateInvoiceTotal(request.getTableId(), request.getTotalMoney());

        Invoice invoice = invoiceMapper.toNewEntity(request);
        invoice.setInvoiceCode(generateNextInvoiceCode());
        invoice.setTable(table);
        invoice.setAccount(currentAccount);
        invoice.setPaymentStatus(paymentStatus);
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setCreatedTime(Instant.now());
        invoice.setActive(true);

        DishOrder dishOrder = dishOrderRepository.findById(request.getDishOrderId().intValue())
                .orElseThrow(() -> new com.duyminhdev.cf_manager.exceptions.InvalidDataException(
                        "Không tìm thấy đơn đặt món với id: " + request.getDishOrderId()
                ));
        invoice.setDishOrder(dishOrder);

        if (request.getBookingId() != null) {
            TableBooking booking = tableBookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new com.duyminhdev.cf_manager.exceptions.InvalidDataException(
                            "Không tìm thấy đặt bàn với id: " + request.getBookingId()
                    ));
            invoice.setBooking(booking);
        }

        invoice.setCustomerName(request.getCustomerName());
        invoice.setCustomerPhone(request.getCustomerPhone());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        saveInvoiceDetails(savedInvoice, request.getInvoiceDetails());

        if (PaymentStatusEnum.PAID.getCode().equalsIgnoreCase(paymentStatus)) {
            finalizeSuccessfulPayment(savedInvoice);
        }

        InvoiceResponseDTO response = invoiceMapper.toResponseDTO(savedInvoice);

        if (PaymentMethodEnum.BANK_TRANSFER.getCode().equalsIgnoreCase(savedInvoice.getPaymentMethod())
                && PaymentStatusEnum.PENDING.getCode().equalsIgnoreCase(savedInvoice.getPaymentStatus())) {
            response.setUriVnPay(buildVnPayUrl(savedInvoice));
        }

        return response;
    }

    @Override
    @Transactional
    /**
     * Xác nhận trạng thái thanh toán cho hóa đơn.
     */
    public InvoiceConfirmPaymentResponseDTO confirmPayment(InvoiceConfirmPaymentRequestDTO request) {
        /**
         * Flow confirm payment:
         * 1. Validate status
         * 2. Load invoice
         * 3. Update payment status
         * 4. Nếu status = PAID -> chốt toàn bộ unfinished order của bàn
         * 5. Recompute trạng thái bàn
         */
        serviceSupport.validatePaymentStatusCode(request.getPaymentStatus());

        Invoice invoice = invoiceRepository.findByIdAndActiveTrue(request.getInvoiceId())
                .orElseThrow(() -> new com.duyminhdev.cf_manager.exceptions.InvalidDataException(
                        "Không tìm thấy hóa đơn với id: " + request.getInvoiceId()
                ));

        invoice.setPaymentStatus(request.getPaymentStatus());
        Invoice savedInvoice = invoiceRepository.save(invoice);

        if (PaymentStatusEnum.PAID.getCode().equalsIgnoreCase(savedInvoice.getPaymentStatus())) {
            finalizeSuccessfulPayment(savedInvoice);
        }

        return invoiceMapper.toConfirmPaymentResponseDTO(savedInvoice);
    }

    @Override
    /**
     * Lấy chi tiết hóa đơn để phục vụ hiển thị chứng từ thanh toán.
     */
    public InvoiceDetailResponse detail(InvoiceDetailRequestDTO request) {
        /**
         * Flow detail invoice:
         * 1. Gọi native query lấy full rows
         * 2. Map header + line items
         * 3. Nếu bank transfer pending thì kèm payment URL
         */
        List<InvoiceDetailNativeResultDTO> rows = nativeSqlInvoiceRepository.findInvoiceDetailByInvoiceId(request.getInvoiceId());
        if (rows == null || rows.isEmpty()) {
            throw new com.duyminhdev.cf_manager.exceptions.InvalidDataException(
                    "Không tìm thấy chi tiết hóa đơn với invoiceId: " + request.getInvoiceId()
            );
        }

        InvoiceDetailResponse response = invoiceMapper.toDetailResponseDTO(rows);

        if (PaymentMethodEnum.BANK_TRANSFER.getCode().equalsIgnoreCase(response.getPaymentMethod())
                && PaymentStatusEnum.PENDING.getCode().equalsIgnoreCase(response.getPaymentStatus())) {
            response.setUriVnPay(buildVnPayUrlByData(
                    response.getInvoiceId(),
                    response.getInvoiceCode(),
                    response.getTotalMoney()
            ));
        }

        return response;
    }

    private void validateInvoiceTotal(Integer tableId, BigDecimal requestTotalMoney) {
        if (requestTotalMoney == null || requestTotalMoney.compareTo(BigDecimal.ZERO) <= 0) {
            throw new com.duyminhdev.cf_manager.exceptions.InvalidDataException("Tổng tiền hóa đơn (totalMoney) phải lớn hơn 0");
        }

        /**
         * Dùng grouped checkout để tính lại tổng tiền server-side.
         * Đây là chốt chặn quan trọng để FE không gửi totalMoney sai.
         */
        List<DishGroupedByTableNativeResultDTO> groupedDishes =
                nativeSqlDishOrderDetailRepository.findGroupedByTableId(tableId);

        BigDecimal calculatedTotal = groupedDishes.stream()
                .map(DishGroupedByTableNativeResultDTO::getTotalPrice)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (calculatedTotal.compareTo(requestTotalMoney) != 0) {
            throw new com.duyminhdev.cf_manager.exceptions.InvalidDataException(
                    "Tổng tiền trong yêu cầu không khớp với tổng giá trị các món ăn đã gọi của bàn"
            );
        }
    }

    private void saveInvoiceDetails(Invoice invoice, List<InvoiceDetailPayloadDTO> payloads) {
        List<InvoiceDetail> details = payloads.stream()
                .map(payload -> {
                    Dish dish = serviceSupport.getActiveDish(payload.getDishId());

                    InvoiceDetail detail = invoiceDetailPayloadMapper.toNewEntity(payload);

                    detail.setInvoice(invoice);
                    detail.setDish(dish);
                    detail.setCreatedTime(Instant.now());
                    detail.setActive(true);
                    return detail;
                })
                .toList();

        invoiceDetailRepository.saveAll(details);
    }

    private void finalizeSuccessfulPayment(Invoice invoice) {
        /**
         * Đây là điểm chốt business:
         * 1. Lấy toàn bộ unfinished orders của bàn
         * 2. Mark toàn bộ -> DONE
         * 3. Recompute trạng thái bàn
         *    - nếu còn booking upcoming -> BOOKED
         *    - else -> AVAILABLE
         */
        List<DishOrder> unfinishedOrders = dishOrderRepository.findAllUnfinishedOrdersByTableId(
                invoice.getTable().getId(),
                List.of(
                        DishOrderStatusCodeEnum.CANCEL.getCode(),
                        DishOrderStatusCodeEnum.PAID.getCode()
                )
        );

        if (!unfinishedOrders.isEmpty()) {
            DishOrderStatus paidStatus =
                    serviceSupport.getDishOrderStatusByCode(DishOrderStatusCodeEnum.PAID.getCode());
            unfinishedOrders.forEach(order -> order.setStatus(paidStatus));
            dishOrderRepository.saveAll(unfinishedOrders);
        }

        serviceSupport.recomputeAndSyncTableStatus(invoice.getTable().getId());
    }

    private long countInvoicesToday() {
        Instant start = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = start.plusSeconds(86_400L).minusNanos(1);
        return invoiceRepository.countByCreatedTimeBetweenAndActiveTrue(start, end);
    }

    private String generateNextInvoiceCode() {
        long countToday = countInvoicesToday() + 1;
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "HD-" + date + "-" + String.format("%04d", countToday);
    }

    private String buildVnPayUrl(Invoice invoice) {
        return buildVnPayUrlByData(invoice.getId(), invoice.getInvoiceCode(), invoice.getTotalMoney());
    }

    private String buildVnPayUrlByData(Integer invoiceId, String invoiceCode, BigDecimal totalMoney) {
        return VnPayConstant.VNPAY_URL
                + "?invoiceId=" + url(invoiceId)
                + "&invoiceCode=" + url(invoiceCode)
                + "&amount=" + url(totalMoney)
                + "&description=" + url("Thanh toan hoa don " + invoiceCode);
    }

    private String url(Object value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }
}
