package com.duyminhdev.cf_manager.dto.request.table;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableAvailableSearchRequestDTO {

    @JsonAlias("table_name")
    private String tableName;

    @Min(value = 1, message = "Số tầng phải từ 1 trở lên")
    @Max(value = 100, message = "Số tầng vượt quá giới hạn cho phép")
    private Integer floor;

    @JsonAlias({"slot"})
    @Min(value = 1, message = "Số chỗ ngồi phải từ 1 trở lên")
    @Max(value = 1000, message = "Số chỗ ngồi (seat) không được vượt quá 1000")
    private Integer seat;
}
