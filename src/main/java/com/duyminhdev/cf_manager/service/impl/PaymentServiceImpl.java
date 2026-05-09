package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableBookingDetailNativeResultDTO;
import com.duyminhdev.cf_manager.dto.request.dish_order.DishOrderDetailPayloadDTO;
import com.duyminhdev.cf_manager.dto.request.payment.OrderAndPayRequestDTO;
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
import com.duyminhdev.cf_manager.service.VoucherService;
import com.duyminhdev.cf_manager.utils.InvoiceCodeService;
import com.duyminhdev.cf_manager.utils.ServiceSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final VoucherService voucherService;
    private final VoucherRepository voucherRepository;

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

        // 3b. Áp dụng voucher trong cùng transaction (nếu có)
        BigDecimal finalAmount = totalAmount;
        Voucher appliedVoucher = null;
        BigDecimal discountAmount = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            var applied = voucherService.applyVoucher(request.getVoucherCode(), orderId, totalAmount);
            discountAmount = applied.getDiscountAmount();
            finalAmount = applied.getFinalAmount();
            // Load Voucher entity để gắn FK vào Invoice/DishOrder
            appliedVoucher = voucherRepository.findById(applied.getVoucherId()).orElse(null);
            // Gắn voucher vào DishOrder (finalTotal và discountAmount sẽ được set ở bước 7)
            order.setVoucher(appliedVoucher);
        }

        // 4. Tạo hóa đơn (dùng finalAmount sau khi đã trừ voucher)
        Account cashier = serviceSupport.getCurrentAccount();
        String invoiceCode = invoiceCodeService.generateInvoiceCode();
        Invoice invoice = Invoice.builder()
                .invoiceCode(invoiceCode)
                .totalMoney(finalAmount)             // ← finalAmount sau voucher
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
                // ── Voucher snapshot ─────────────────────────────────────────
                .voucher(appliedVoucher)
                .voucherCode(appliedVoucher != null ? appliedVoucher.getCode() : null)
                .discountAmount(discountAmount)
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

        // 6. Ghi nhận dòng tiền (theo finalAmount)
        CashFlow cashFlow = CashFlow.builder()
                .totalMoney(finalAmount)
                .flowType(FlowTypeEnum.INCOME.name())
                .note("Thanh toán hóa đơn " + invoiceCode)
                .createdTime(Instant.now())
                .active(true)
                .account(cashier)
                .build();
        cashFlow = cashFlowRepository.save(cashFlow);

        // 7. Cập nhật DishOrder: status PAID + voucher fields
        DishOrderStatus paidStatus = dishOrderStatusRepository
                .findByDishOrderStatusCode(DishOrderStatusCodeEnum.PAID.getCode())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạng thái PAID"));
        order.setStatus(paidStatus);
        order.setDiscountAmount(discountAmount);
        order.setFinalTotal(finalAmount);
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

    /**
     * Tạo đơn order và thanh toán ngay lập tức trong một transaction duy nhất.
     * Luồng rút gọn dành cho POS: nhân viên chọn món → bấm "Thanh toán" → hoàn tất.
     *
     * <p>Các bước xử lý:
     * <ol>
     *   <li>Validate bàn và danh sách món</li>
     *   <li>Tạo DishOrder với trạng thái khởi tạo</li>
     *   <li>Tạo DishOrderDetail và tính totalBill</li>
     *   <li>Tạo Invoice + InvoiceDetail</li>
     *   <li>Ghi CashFlow thu tiền</li>
     *   <li>Cập nhật trạng thái DishOrder → PAID</li>
     *   <li>Đồng bộ trạng thái bàn</li>
     * </ol>
     */
    @Override
    @Transactional
    public PaymentResponse orderAndPay(OrderAndPayRequestDTO request) {
        PaymentMethodEnum paymentMethod = PaymentMethodEnum.fromCode(request.getPaymentMethod());

        // ── 1. Lấy thông tin bàn ──────────────────────────────────────────────
        TableEntity table = serviceSupport.getActiveTable(request.getTableId());

        // ── 2. Lấy trạng thái khởi tạo (PROCESSING) rồi set luôn PAID sau đó ──
        DishOrderStatus processingStatus = serviceSupport
                .getDishOrderStatusByCode(DishOrderStatusCodeEnum.PROCESSING.getCode());
        DishOrderStatus paidStatus = dishOrderStatusRepository
                .findByDishOrderStatusCode(DishOrderStatusCodeEnum.PAID.getCode())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạng thái PAID"));

        // ── 3. Lấy nhân viên đang đăng nhập ──────────────────────────────────
        Account cashier = serviceSupport.getCurrentAccount();

        // ── 4. Tạo và lưu DishOrder (trạng thái ban đầu: PROCESSING) ─────────
        DishOrder dishOrder = DishOrder.builder()
                .note(request.getDescription())
                .createdTime(Instant.now())
                .active(true)
                .status(processingStatus)
                .table(table)
                .account(cashier)
                .build();
        DishOrder savedOrder = dishOrderRepository.save(dishOrder);

        // ── 5. Tạo DishOrderDetail và tính tổng tiền ─────────────────────────
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<DishOrderDetail> details = new ArrayList<>();
        List<OrderItemSnapshot> snapshots = new ArrayList<>();

        for (DishOrderDetailPayloadDTO payload : request.getDishOrderDetails()) {
            Dish dish = serviceSupport.getActiveDish(payload.getDishId());
            BigDecimal unitPrice = dish.getPrice();
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
            details.add(detail);
            snapshots.add(new OrderItemSnapshot(dish, payload.getQuantity(), unitPrice));
            totalAmount = totalAmount.add(lineTotal);
        }
        dishOrderDetailRepository.saveAll(details);

        // ── 6. Lấy thông tin khách hàng từ booking đang hoạt động ─────────────
        CustomerInfoDto customerInfo = getActiveBookingCustomer(table.getId());

        // ── 6b. Áp dụng voucher trong cùng transaction (nếu có) ──────────────
        BigDecimal finalAmount = totalAmount;
        Voucher appliedVoucher = null;
        BigDecimal discountAmount = null;

        if (request.getVoucherCode() != null && !request.getVoucherCode().isBlank()) {
            var applied = voucherService.applyVoucher(request.getVoucherCode(), savedOrder.getId(), totalAmount);
            discountAmount = applied.getDiscountAmount();
            finalAmount = applied.getFinalAmount();
            appliedVoucher = voucherRepository.findById(applied.getVoucherId()).orElse(null);
            savedOrder.setVoucher(appliedVoucher);
        }

        // Cập nhật totalBill (gốc) + discount fields cho DishOrder
        savedOrder.setTotalBill(totalAmount);
        savedOrder.setDiscountAmount(discountAmount);
        savedOrder.setFinalTotal(finalAmount);
        dishOrderRepository.save(savedOrder);

        // ── 7. Tạo Invoice ────────────────────────────────────────────────────
        String invoiceCode = invoiceCodeService.generateInvoiceCode();
        Invoice invoice = Invoice.builder()
                .invoiceCode(invoiceCode)
                .totalMoney(finalAmount)             // ← finalAmount sau voucher
                .paymentStatus(PaymentStatusEnum.PAID.name())
                .paymentMethod(paymentMethod.getLabel())
                .createdTime(Instant.now())
                .active(true)
                .account(cashier)
                .table(table)
                .dishOrder(savedOrder)
                .booking(customerInfo.getBookingId() != null
                        ? tableBookingRepository.findById(customerInfo.getBookingId()).orElse(null)
                        : null)
                .customerName(customerInfo.getCustomerName())
                .customerPhone(customerInfo.getPhoneNumber())
                // ── Voucher snapshot ─────────────────────────────────────────
                .voucher(appliedVoucher)
                .voucherCode(appliedVoucher != null ? appliedVoucher.getCode() : null)
                .discountAmount(discountAmount)
                .build();
        invoice = invoiceRepository.save(invoice);

        // ── 8. Tạo InvoiceDetail (snapshot giá tại thời điểm thanh toán) ──────
        List<InvoiceDetail> invoiceDetails = new ArrayList<>();
        for (OrderItemSnapshot item : snapshots) {
            InvoiceDetail invDetail = InvoiceDetail.builder()
                    .quantity(item.quantity)
                    .unitPrice(item.unitPrice)
                    .createdTime(Instant.now())
                    .active(true)
                    .invoice(invoice)
                    .dish(item.dish)
                    .build();
            invoiceDetails.add(invoiceDetailRepository.save(invDetail));
        }

        // ── 9. Ghi nhận dòng tiền thu vào (theo finalAmount) ─────────────────
        CashFlow cashFlow = CashFlow.builder()
                .totalMoney(finalAmount)
                .flowType(FlowTypeEnum.INCOME.name())
                .note("Thanh toán hóa đơn " + invoiceCode)
                .createdTime(Instant.now())
                .active(true)
                .account(cashier)
                .build();
        cashFlow = cashFlowRepository.save(cashFlow);

        // ── 10. Cập nhật trạng thái DishOrder → PAID ─────────────────────────
        savedOrder.setStatus(paidStatus);
        dishOrderRepository.save(savedOrder);

        // ── 11. Đồng bộ trạng thái bàn ───────────────────────────────────────
        serviceSupport.recomputeAndSyncTableStatus(table.getId());

        // ── 12. Trả kết quả ───────────────────────────────────────────────────
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
                // ── Voucher snapshot ─────────────────────────────────────────
                .voucherId(invoice.getVoucher() != null ? invoice.getVoucher().getId() : null)
                .voucherCode(invoice.getVoucherCode())
                .discountAmount(invoice.getDiscountAmount())
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
