package com.duyminhdev.cf_manager.dto.request.dish_order;

import lombok.Data;

@Data
public class OrderHistorySearchRequestDTO {
    private Long diningTableId;    // Thêm field ID bàn
    private String tableName;      // Tìm kiếm tương đối theo tên bàn
    private String employeeName;   // Tìm kiếm tương đối theo tên nhân viên
    private String status;         // Tìm kiếm tương đối theo trạng thái
    private Integer page;
    private Integer limit;
    private String sortField;
    private String sortDir;
}