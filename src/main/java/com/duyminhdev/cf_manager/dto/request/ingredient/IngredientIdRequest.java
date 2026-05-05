package com.duyminhdev.cf_manager.dto.request.ingredient;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IngredientIdRequest {
    @NotNull(message = "ID nguyên liệu không được để trống")
    private Integer id;
}