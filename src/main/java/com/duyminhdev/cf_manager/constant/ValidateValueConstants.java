package com.duyminhdev.cf_manager.constant;

public final class ValidateValueConstants {
    private ValidateValueConstants() {
    }

    /**
     * Chỉ cho phép field sort là chữ, số, underscore, không cho ký tự nguy hiểm.
     * Dùng cho request search.
     */
    public static final String SORT_FIELD = "^[a-zA-Z][a-zA-Z0-9_]*$";

    /**
     * Chuẩn asc / desc
     */
    public static final String SORT_DIR = "^(?i)(asc|desc)$";

    /**
     * Tên khách hàng / text ngắn thân thiện.
     * Có thể nới thêm nếu sau này cần.
     */
    public static final String HUMAN_NAME = "^[\\p{L}0-9 .,'_-]{1,255}$";

    /**
     * Số điện thoại VN cơ bản.
     */
    public static final String PHONE_NUMBER = "^(0|\\+84)[0-9]{9,10}$";

    /**
     * Mã code hệ thống như tableCode, invoiceCode...
     */
    public static final String CODE = "^[A-Za-z0-9_\\-]{1,100}$";

    /**
     * Tránh text quá bẩn ở các field note ngắn.
     */
    public static final String SHORT_TEXT = "^[\\p{L}0-9\\s.,;:()_\\-/%+&@#]{0,255}$";

    /**
     * Mã code chuẩn nghiệp vụ: chữ, số, underscore.
     */
    public static final String BUSINESS_CODE = "^[A-Z0-9_]+$";

    /**
     * Chuỗi text ngắn an toàn.
     */
    public static final String SAFE_TEXT = "^[\\p{L}0-9 _.,:/()\\-+]{0,255}$";
}
