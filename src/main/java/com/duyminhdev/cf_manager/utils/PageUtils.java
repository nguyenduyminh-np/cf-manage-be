package com.duyminhdev.cf_manager.utils;

public final class PageUtils {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private PageUtils() {
    }

    public static int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return DEFAULT_PAGE;
        }
        return page;
    }

    public static int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    public static int calculateTotalPages(long totalElements, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }
}
