package com.duyminhdev.cf_manager.dto.request.purchase_order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PurchaseOrderIngredientBySupplierRequestDTO {
    @NotNull(message = "Nhà cung cấp không được trống")
    private Integer supplierId;
}
