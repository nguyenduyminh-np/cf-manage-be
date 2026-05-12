package com.duyminhdev.cf_manager.tools;

import com.duyminhdev.cf_manager.config.CafeInfoProperties;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientStockDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.SalesSummaryDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailabilityDTO;
import com.duyminhdev.cf_manager.dto.request.dish.DishListRequestDTO;
import com.duyminhdev.cf_manager.dto.request.table_booking.TableBookingCreateRequestDTO;
import com.duyminhdev.cf_manager.dto.response.dashboard.*;
import com.duyminhdev.cf_manager.dto.response.dish.DishResponseDTO;
import com.duyminhdev.cf_manager.dto.response.table_booking.TableBookingResponseDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlChatRepository;
import com.duyminhdev.cf_manager.service.DashboardService;
import com.duyminhdev.cf_manager.service.DishService;
import com.duyminhdev.cf_manager.service.TableBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Tập hợp tất cả @Tool method được đăng ký với Spring AI ChatClient.
 * Gemini sẽ tự quyết định gọi tool nào dựa trên mô tả (description).
 * <p>
 * Phân loại tool theo quyền truy cập:
 * - PUBLIC:  checkTableAvailability, queryMenu, getTopSellingDishes, getCafeInfo
 * - STAFF:   previewBooking, confirmBooking, getUpcomingBookings, getProcessingOrders
 * - ADMIN:   getSalesReport, checkInventory, getDashboardKpi, getStockAlerts
 * <p>
 * Việc lọc tool theo role được thực hiện bởi {@link ChatToolAuthFilter}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatToolFunctions {

    private final NativeSqlChatRepository chatRepo;
    private final DishService dishService;
    private final TableBookingService bookingService;
    private final DashboardService dashboardService;
    private final CafeInfoProperties cafeInfo;

    // ========================================================================
    // PUBLIC TOOLS — Khách vãng lai và nhân viên đều dùng được
    // ========================================================================

    @Tool(description = "Kiểm tra bàn trống theo tầng và khung giờ. "
            + "Trả về danh sách bàn không có booking chồng lịch. "
            + "Dùng khi khách hỏi 'còn bàn trống không', 'bàn nào trống tối nay', 'check bàn'.")
    public List<TableAvailabilityDTO> checkTableAvailability(
            @ToolParam(description = "Tầng cần kiểm tra. Truyền null nếu muốn xem tất cả tầng.") Integer floor,
            @ToolParam(description = "Thời gian bắt đầu, format ISO-8601 UTC (VD: 2026-05-08T12:00:00Z)") String startTime,
            @ToolParam(description = "Thời gian kết thúc, format ISO-8601 UTC (VD: 2026-05-08T14:00:00Z)") String endTime) {
        log.debug("Tool checkTableAvailability: floor={}, start={}, end={}", floor, startTime, endTime);
        return chatRepo.findAvailableTables(floor, Instant.parse(startTime), Instant.parse(endTime));
    }

    @Tool(description = "Lấy danh sách thực đơn quán cà phê gồm tên món, giá, danh mục. "
            + "Dùng khi khách hỏi 'có món gì', 'menu', 'giá bao nhiêu', 'gợi ý đồ uống', 'thực đơn'.")
    public List<DishResponseDTO> queryMenu() {
        log.debug("Tool queryMenu called");
        DishListRequestDTO req = new DishListRequestDTO();
        req.setActive(true);
        return dishService.getAll(req);
    }

    @Tool(description = "Lấy top món bán chạy nhất trong khoảng thời gian. "
            + "Dùng khi hỏi 'món nào bán chạy', 'gợi ý món phổ biến', 'món nào ngon'.")
    public List<Object[]> getTopSellingDishes(
            @ToolParam(description = "Thời gian bắt đầu ISO-8601 UTC") String from,
            @ToolParam(description = "Thời gian kết thúc ISO-8601 UTC") String to,
            @ToolParam(description = "Số lượng món muốn lấy (mặc định 5)") int limit) {
        log.debug("Tool getTopSellingDishes: from={}, to={}, limit={}", from, to, limit);
        return chatRepo.getTopSellingDishes(Instant.parse(from), Instant.parse(to),
                limit > 0 ? limit : 5);
    }

    @Tool(description = "Lấy thông tin quán cà phê: tên, địa chỉ, giờ mở cửa, wifi, đỗ xe. "
            + "Dùng khi khách hỏi FAQ: 'mấy giờ mở cửa', 'ở đâu', 'có wifi không', "
            + "'pass wifi', 'giờ đóng cửa', 'địa chỉ', 'đỗ xe'.")
    public String getCafeInfo() {
        log.debug("Tool getCafeInfo called");
        return cafeInfo.toSystemPromptSnippet();
    }

    // ========================================================================
    // STAFF TOOLS — Yêu cầu JWT với role ADMIN/QL/PC/PV
    // ========================================================================

    @Tool(description = "Tạo preview đặt bàn (CHƯA XÁC NHẬN). Trả về thông tin bàn đề xuất phù hợp. "
            + "Dùng khi nhân viên/khách nói 'đặt bàn cho X người lúc Y'. "
            + "SAU KHI user xác nhận thì mới được gọi confirmBooking().")
    public String previewBooking(
            @ToolParam(description = "Tên khách hàng") String customerName,
            @ToolParam(description = "Số điện thoại khách, null nếu không có") String phone,
            @ToolParam(description = "Số người") int guestCount,
            @ToolParam(description = "Tầng yêu thích, null nếu không chỉ định") Integer preferredFloor,
            @ToolParam(description = "Thời gian đến ISO-8601 UTC") String arriveTime,
            @ToolParam(description = "Thời gian trả bàn ISO-8601 UTC") String checkOutTime) {
        log.debug("Tool previewBooking: customer={}, guests={}, floor={}, arrive={}, checkout={}",
                customerName, guestCount, preferredFloor, arriveTime, checkOutTime);

        // Chỉ kiểm tra bàn trống, KHÔNG tạo booking
        List<TableAvailabilityDTO> available = chatRepo.findAvailableTables(
                preferredFloor, Instant.parse(arriveTime), Instant.parse(checkOutTime));

        if (available.isEmpty()) {
            return "KHÔNG CÒN BÀN TRỐNG cho khung giờ và tầng yêu cầu. "
                    + "Gợi ý khách chọn khung giờ hoặc tầng khác.";
        }

        // Chọn bàn phù hợp nhất (slot >= guestCount, ưu tiên slot nhỏ nhất phù hợp)
        TableAvailabilityDTO best = available.stream()
                .filter(t -> t.getSlot() != null && t.getSlot() >= guestCount)
                .min((a, b) -> Integer.compare(a.getSlot(), b.getSlot()))
                .orElse(available.get(0));

        return String.format(
                "PREVIEW_BOOKING|tableId=%d|tableName=%s|floor=%d|slot=%d|"
                        + "customer=%s|phone=%s|guests=%d|arrive=%s|checkout=%s. "
                        + "HÃY HỎI KHÁCH XÁC NHẬN trước khi gọi confirmBooking(). "
                        + "Nếu khách đồng ý, gọi confirmBooking() với tableId=%d.",
                best.getTableId(), best.getTableName(), best.getFloor(), best.getSlot(),
                customerName != null ? customerName : "Khách",
                phone != null ? phone : "N/A",
                guestCount, arriveTime, checkOutTime,
                best.getTableId());
    }

    @Tool(description = "Xác nhận đặt bàn SAU KHI khách đã đồng ý preview. "
            + "CHỈ ĐƯỢC GỌI KHI khách nói 'xác nhận', 'ok', 'đồng ý', 'được'. "
            + "TUYỆT ĐỐI KHÔNG tự ý gọi tool này mà chưa có sự đồng ý của khách.")
    public String confirmBooking(
            @ToolParam(description = "Tên khách hàng") String customerName,
            @ToolParam(description = "Số điện thoại khách") String phone,
            @ToolParam(description = "Số người") int guestCount,
            @ToolParam(description = "ID bàn đã chọn từ kết quả previewBooking()") Integer tableId,
            @ToolParam(description = "Thời gian đến ISO-8601 UTC") String arriveTime,
            @ToolParam(description = "Thời gian trả bàn ISO-8601 UTC") String checkOutTime) {
        log.info("Tool confirmBooking: customer={}, tableId={}, arrive={}", customerName, tableId, arriveTime);

        try {
            TableBookingCreateRequestDTO req = new TableBookingCreateRequestDTO();
            req.setTableId(tableId);
            req.setCustomerName(customerName);
            req.setPhoneNumber(phone);
            req.setExpectedArriveTime(Instant.parse(arriveTime));
            req.setExpectedCheckOut(Instant.parse(checkOutTime));

            TableBookingResponseDTO booking = bookingService.create(req).getData();
            return String.format("ĐẶT BÀN THÀNH CÔNG. Mã booking: %s. Bàn: %s. Thời gian: %s.",
                    booking.getBookingInvoiceCode(),
                    booking.getTableName(),
                    arriveTime);
        } catch (Exception e) {
            log.error("Booking failed via chat tool", e);
            return "ĐẶT BÀN THẤT BẠI: " + e.getMessage()
                    + ". Gợi ý khách thử lại hoặc chọn bàn/khung giờ khác.";
        }
    }

    @Tool(description = "Lấy danh sách booking sắp đến trong 2 giờ tới. "
            + "Dùng khi nhân viên hỏi 'booking sắp tới', 'khách nào sắp đến'.")
    public List<DashboardUpcomingBookingItemDTO> getUpcomingBookings() {
        log.debug("Tool getUpcomingBookings called");
        return dashboardService.getUpcomingBookings();
    }

    @Tool(description = "Lấy danh sách đơn hàng đang chế biến hôm nay. "
            + "Dùng khi nhân viên hỏi 'đơn nào đang làm', 'bếp đang nấu gì'.")
    public List<DashboardProcessingOrderItemDTO> getProcessingOrders() {
        log.debug("Tool getProcessingOrders called");
        return dashboardService.getProcessingOrdersToday();
    }

    // ========================================================================
    // ADMIN / QL TOOLS — Chỉ dành cho Quản lý và Admin
    // ========================================================================

    @Tool(description = "Xem tổng hợp doanh thu theo khoảng thời gian. "
            + "Trả về tổng tiền và số hóa đơn đã thanh toán. "
            + "Dùng khi hỏi 'doanh thu hôm nay', 'bán được bao nhiêu tuần này', 'doanh thu tháng 4'.")
    public SalesSummaryDTO getSalesReport(
            @ToolParam(description = "Thời gian bắt đầu ISO-8601 UTC") String from,
            @ToolParam(description = "Thời gian kết thúc ISO-8601 UTC") String to) {
        log.debug("Tool getSalesReport: from={}, to={}", from, to);
        return chatRepo.getSalesSummary(Instant.parse(from), Instant.parse(to));
    }

    @Tool(description = "Kiểm tra nguyên liệu sắp hết hoặc sắp hết hạn trong kho. "
            + "Dùng khi hỏi 'tồn kho', 'nguyên liệu sắp hết', 'hết hạn'.")
    public List<IngredientStockDTO> checkInventory() {
        log.debug("Tool checkInventory called");
        return chatRepo.getLowStockIngredients();
    }

    @Tool(description = "Lấy tổng quan KPI dashboard hôm nay: doanh thu, đơn hàng, booking, "
            + "bàn đang phục vụ, nhân viên, nợ nhà cung cấp, tồn kho sắp hết hạn. "
            + "Dùng khi hỏi 'tình hình hôm nay', 'tổng quan', 'dashboard'.")
    public DashboardKpiDTO getDashboardKpi() {
        log.debug("Tool getDashboardKpi called");
        return dashboardService.getKpi();
    }

    @Tool(description = "Lấy cảnh báo tồn kho chi tiết: nguyên liệu hết hạn sớm hoặc sắp hết số lượng. "
            + "Dùng khi hỏi 'cảnh báo kho', 'kho có vấn đề gì không'.")
    public List<DashboardStockAlertItemDTO> getStockAlerts() {
        log.debug("Tool getStockAlerts called");
        return dashboardService.getStockAlerts();
    }
}
