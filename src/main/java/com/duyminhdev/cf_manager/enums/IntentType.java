package com.duyminhdev.cf_manager.enums;

/**
 * Phân loại ý định (intent) của tin nhắn chatbot.
 * Được detect bằng rule-based (regex) trước khi fallback lên AI.
 */
public enum IntentType {

    /** Hỏi bàn nào còn trống, theo tầng / thời gian */
    TABLE_AVAILABILITY,

    /** Đặt bàn qua chat — yêu cầu xác thực */
    BOOK_TABLE,

    /** Hỏi thực đơn, gợi ý món */
    MENU_QUERY,

    /** Hỏi doanh thu — chỉ ADMIN/QL */
    SALES_REPORT,

    /** Kiểm tra tồn kho nguyên liệu */
    INVENTORY_CHECK,

    /** Tạo đơn nhập hàng — chỉ ADMIN/QL */
    CREATE_PURCHASE_ORDER,

    /** Câu hỏi FAQ: giờ, địa chỉ, wifi, đỗ xe... */
    FAQ,

    /** Không xác định — fallback lên Gemini AI */
    UNKNOWN
}
