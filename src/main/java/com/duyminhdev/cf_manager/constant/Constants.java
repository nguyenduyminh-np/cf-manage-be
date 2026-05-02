package com.duyminhdev.cf_manager.constant;

public class Constants {
    // ==================== DATE FORMAT ====================
    public class DateFormatType {
        public static final String DD_MM_YYYY = "dd/MM/yyyy";
        public static final String DD_MM_YYYY_HH_MM_SS = "dd-MM-yyyy HH:mm:ss";
        public static final String DD_MM_YYYY_HH_MM_SS2 = "dd/MM/yyyy HH:mm:ss";
    }

    // ==================== TIME FORMAT ====================
    public class TimeFormat {
        public static final String HH_MM_SS = "HH:mm:ss";
    }

    // ==================== STRING FORMAT ====================
    public class StringFormat {
        public static final String ONLY_NUMBERS = "^\\d+$";
        public static final String ONLY_PHONE = "^(03|05|07|08|09)[0-9]{8}$";
        public static final String ONLY_DECIMAL_NUMBERS = "^\\d+(\\.\\d+)?$";
        public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    }
}
