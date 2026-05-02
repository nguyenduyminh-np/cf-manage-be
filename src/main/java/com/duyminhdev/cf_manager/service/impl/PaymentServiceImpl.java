package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableBookingDetailNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.payment.PaymentRequestDTO;
import com.duyminhdev.cf_manager.dto.response.payment.*;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.enums.DishOrderStatusCodeEnum;
import com.duyminhdev.cf_manager.enums.FlowTypeEnum;
import com.duyminhdev.cf_manager.enums.PaymentMethodEnum;
import com.duyminhdev.cf_manager.enums.PaymentStatusEnum;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlTableBookingRepository;
import com.duyminhdev.cf_manager.service.PaymentService;
import com.duyminhdev.cf_manager.utils.InvoiceCodeService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final DishOrderRepository dishOrderRepository;
    private final DishOrderDetailRepository dishOrderDetailRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final CashFlowRepository cashFlowRepository;
    private final TableBookingRepository tableBookingRepository;
    private final DishOrderStatusRepository dishOrderStatusRepository;
    private final NativeSqlTableBookingRepository nativeSqlTableBookingRepository;

    private final ServiceSupport serviceSupport;
    private final InvoiceCodeService invoiceCodeService;

    @Override
    public PaymentResponse processPayment(PaymentRequestDTO request) {
        Integer orderId = request.getOrderId();
        PaymentMethodEnum paymentMethod = PaymentMethodEnum.fromCode(request.getPaymentMethod());

        // 1. Lấy và validate đơn hàng
        DishOrder order = dishOrderRepository.findByIdAndActiveTrue(orderId)
                .orElseThrow(() -> new InvalidDataException("Không tìm thấy đơn hàng để thanh toán: "+orderId));

        String statusCode = order.getStatus().getDishOrderStatusCode();
        if (DishOrderStatusCodeEnum.CANCEL.getCode().equalsIgnoreCase(statusCode)) {
            throw new InvalidDataException("Đơn hàng đã bị hủy, không thể thanh toán");
        }
        if (DishOrderStatusCodeEnum.PAID.getCode().equalsIgnoreCase(statusCode)) {
            throw new InvalidDataException("Đơn hàng này đã được thanh toán rồi");
        }

        // TO-DO kiểm tra xem đơn hàng đó đã có hóa đơn hay chưa

        // 2. Lấy danh sách món của order và tính tổng tiên
        List<DishOrderDetail> details = dishOrderDetailRepository.findAllByDishOrderIdAndActiveTrueOrderByCreatedTimeAsc(orderId);

            // tính toán tiền cho order, chuẩn bị dữ liệu cho InvoiceDetail: chi tiết hóa đơn
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItemSnapshot> snapshots = new ArrayList<>();
        for (DishOrderDetail detail : details) {
            Dish dish = detail.getDish();
            BigDecimal unitPrice = dish.getPrice(); // đơn giá gốc hiện tại
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(detail.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            snapshots.add(new OrderItemSnapshot(dish, detail.getQuantity(), unitPrice));
        }

        // 3. Lấy thông tin khách hàng từ booking đang hoạt động
        CustomerInfoDto customerInfo = getActiveBookingCustomer(order.getTable().getId());

        // 4. Tạo hóa đơn
        Account cashier = serviceSupport.getCurrentAccount();
        String invoiceCode = invoiceCodeService.generateInvoiceCode();
        Invoice invoice = Invoice.builder()
                .invoiceCode(invoiceCode)
                .totalMoney(totalAmount)
                .paymentStatus(PaymentStatusEnum.PAID.name())
                .paymentMethod(paymentMethod.getLabel()) // "Tiền mặt" / "Chuyển khoản"
                .createdTime(Instant.now())
                .active(true)
                .account(cashier)
                .table(order.getTable())
                .dishOrder(order)
                .booking(customerInfo.getBookingId() != null ?
                        tableBookingRepository.findById(customerInfo.getBookingId()).orElse(null) : null)
                .customerName(customerInfo.getCustomerName())
                .customerPhone(customerInfo.getPhoneNumber())
                .build();
        invoice = invoiceRepository.save(invoice);

        // 5. Tạo chi tiết hóa đơn
        List<InvoiceDetail> invoiceDetails = new ArrayList<>();
        for (OrderItemSnapshot item : snapshots) {
            InvoiceDetail detail = InvoiceDetail.builder()
                    .quantity(item.quantity)
                    .unitPrice(item.unitPrice)
                    .createdTime(Instant.now())
                    .active(true)
                    .invoice(invoice)
                    .dish(item.dish)
                    .build();
            invoiceDetails.add(invoiceDetailRepository.save(detail));
        }

        // 6. Ghi nhận dòng tiền
        CashFlow cashFlow = CashFlow.builder()
                .totalMoney(totalAmount)
                .flowType(FlowTypeEnum.INCOME.name())
                .note("Thanh toán hóa đơn " + invoiceCode)
                .createdTime(Instant.now())
                .active(true)
                .account(cashier)
                .build();
        cashFlow = cashFlowRepository.save(cashFlow);

        // 7. Cập nhật trạng thái đơn hàng thành PAID
        DishOrderStatus paidStatus = dishOrderStatusRepository
                .findByDishOrderStatusCode(DishOrderStatusCodeEnum.PAID.getCode())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạng thái PAID"));
        order.setStatus(paidStatus);
        dishOrderRepository.save(order);

        // 8. Đồng bộ trạng thái bàn (có thể giải phóng nếu không còn order unfinished)
        serviceSupport.recomputeAndSyncTableStatus(order.getTable().getId());

        // 9. Xây dựng response
        return PaymentResponse.builder()
                .invoice(toInvoiceDto(invoice))
                .invoiceDetails(invoiceDetails.stream().map(this::toInvoiceDetailDto).toList())
                .cashFlow(toCashFlowDto(cashFlow))
                .build();
    }

    private CustomerInfoDto getActiveBookingCustomer(Integer tableId) {
        Optional<TableBookingDetailNativeResultDTO> bookingOpt =
                nativeSqlTableBookingRepository.findActiveBookingByTableId(tableId);
        if (bookingOpt.isPresent()) {
            TableBookingDetailNativeResultDTO b = bookingOpt.get();
            return  CustomerInfoDto.builder()
                    .bookingId(b.getBookingId())
                    .phoneNumber(b.getPhoneNumber())
                    .customerName(b.getCustomerName())
                    .build();
        } else {
            TableEntity table = serviceSupport.getActiveTable(tableId);
            return  CustomerInfoDto.builder()
                    .bookingId(null)
                    .phoneNumber("Khách vãng lai")
                    .customerName("Khách vãng lai/không có booking voi don order mon này")
                    .build();
        }
    }

    // --- Các mapper thành DTO ---
    private InvoiceDto toInvoiceDto(Invoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceCode(invoice.getInvoiceCode())
                .totalAmount(invoice.getTotalMoney())
                .paymentStatus(invoice.getPaymentStatus())
                .paymentMethod(invoice.getPaymentMethod())
                .createdAt(invoice.getCreatedTime())
                .diningTableId(invoice.getTable().getId())
                .guestCount(invoice.getTotalGuest())
                .dishOrderId(invoice.getDishOrder().getId())
                .accountId(invoice.getAccount().getId())
                .bookingId(invoice.getBooking() != null ? invoice.getBooking().getId() : null)
                .customerName(invoice.getCustomerName())
                .customerPhone(invoice.getCustomerPhone())
                .build();
    }

    private InvoiceDetailDto toInvoiceDetailDto(InvoiceDetail detail) {
        Dish dish = detail.getDish();
        return InvoiceDetailDto.builder()
                .id(detail.getId())
                .dishId(dish.getId())
                .dishName(dish.getDishName())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .subtotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .build();
    }

    private CashFlowDto toCashFlowDto(CashFlow cashFlow) {
        return CashFlowDto.builder()
                .id(cashFlow.getId())
                .totalAmount(cashFlow.getTotalMoney())
                .flowType(cashFlow.getFlowType())
                .note(cashFlow.getNote())
                .createdAt(cashFlow.getCreatedTime())
                .build();
    }

    private record OrderItemSnapshot(Dish dish, Integer quantity, BigDecimal unitPrice) {}
}
