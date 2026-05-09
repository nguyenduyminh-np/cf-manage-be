package com.duyminhdev.cf_manager.tools;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Bộ lọc tool dựa trên role của Authentication.
 * <p>
 * Quy tắc phân quyền:
 * <ul>
 *   <li>PUBLIC (không cần auth): checkTableAvailability, queryMenu, getTopSellingDishes, getCafeInfo</li>
 *   <li>STAFF (ADMIN/QL/PC/PV): previewBooking, confirmBooking, getUpcomingBookings, getProcessingOrders</li>
 *   <li>ADMIN/QL: getSalesReport, getDashboardKpi</li>
 *   <li>ADMIN/QL/PC: checkInventory, getStockAlerts</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class ChatToolAuthFilter {

    private static final Set<String> PUBLIC_TOOLS = Set.of(
            "checkTableAvailability", "queryMenu", "getTopSellingDishes", "getCafeInfo"
    );

    private static final Set<String> STAFF_TOOLS = Set.of(
            "previewBooking", "confirmBooking", "getUpcomingBookings", "getProcessingOrders"
    );

    private static final Set<String> ADMIN_QL_TOOLS = Set.of(
            "getSalesReport", "getDashboardKpi"
    );

    private static final Set<String> INVENTORY_TOOLS = Set.of(
            "checkInventory", "getStockAlerts"
    );

    /**
     * Trả về tập tên tool mà user hiện tại được phép sử dụng.
     *
     * @param auth Authentication từ SecurityContext, có thể null (public endpoint)
     * @return Set tên tool được phép
     */
    public Set<String> getAllowedToolNames(Authentication auth) {
        Set<String> allowed = new HashSet<>(PUBLIC_TOOLS);

        if (isAuthenticated(auth)) {
            // Tất cả nhân viên đã đăng nhập đều dùng được staff tools
            if (hasAnyRole(auth, "ADMIN", "QL", "PC", "PV")) {
                allowed.addAll(STAFF_TOOLS);
            }
            // Chỉ Admin và Quản lý mới xem doanh thu, KPI
            if (hasAnyRole(auth, "ADMIN", "QL")) {
                allowed.addAll(ADMIN_QL_TOOLS);
            }
            // Admin, Quản lý, Phụ trách kho mới kiểm tra tồn kho
            if (hasAnyRole(auth, "ADMIN", "QL", "PC")) {
                allowed.addAll(INVENTORY_TOOLS);
            }
        }

        return Collections.unmodifiableSet(allowed);
    }

    /**
     * Kiểm tra user đã đăng nhập hợp lệ (không phải anonymousUser).
     */
    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());
    }

    /**
     * Kiểm tra user có ít nhất một trong các role được chỉ định.
     * Hỗ trợ cả format "ROLE_XXX" và "XXX".
     */
    private boolean hasAnyRole(Authentication auth, String... roles) {
        if (auth == null || auth.getAuthorities() == null) return false;
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String grantedRole = authority.getAuthority();
            for (String role : roles) {
                if (grantedRole.equals(role) || grantedRole.equals("ROLE_" + role)) {
                    return true;
                }
            }
        }
        return false;
    }
}
