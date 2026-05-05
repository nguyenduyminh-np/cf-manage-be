package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientStockDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.SalesSummaryDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailabilityDTO;

import java.time.Instant;
import java.util.List;

/**
 * Native query repository cho module AI Chatbot.
 * Cung cấp các truy vấn phức tạp mà JPA không thể biểu diễn gọn.
 */
public interface NativeSqlChatRepository {

    /**
     * Tìm danh sách bàn trống theo tầng và khoảng thời gian.
     *
     * @param floor  tầng cần kiểm tra (null = tất cả tầng)
     * @param start  thời điểm bắt đầu (UTC Instant)
     * @param end    thời điểm kết thúc (UTC Instant)
     * @return danh sách bàn không có booking chồng lịch
     */
    List<TableAvailabilityDTO> findAvailableTables(Integer floor, Instant start, Instant end);

    /**
     * Tổng hợp doanh thu trong khoảng thời gian.
     *
     * @param from thời điểm bắt đầu (UTC)
     * @param to   thời điểm kết thúc (UTC)
     * @return tổng doanh thu + số hóa đơn
     */
    SalesSummaryDTO getSalesSummary(Instant from, Instant to);

    /**
     * Lấy danh sách nguyên liệu sắp hết hoặc sắp hết hạn.
     * Ngưỡng: số lượng < 10 HOẶC hết hạn trong 7 ngày tới.
     *
     * @return danh sách nguyên liệu cần chú ý, sắp xếp theo tổng số lượng tăng dần
     */
    List<IngredientStockDTO> getLowStockIngredients();

    /**
     * Lấy top N món bán chạy nhất trong khoảng thời gian.
     *
     * @param from  thời điểm bắt đầu (UTC)
     * @param to    thời điểm kết thúc (UTC)
     * @param limit số lượng món muốn lấy
     * @return danh sách [dishName, totalSold]
     */
    List<Object[]> getTopSellingDishes(Instant from, Instant to, int limit);
}
