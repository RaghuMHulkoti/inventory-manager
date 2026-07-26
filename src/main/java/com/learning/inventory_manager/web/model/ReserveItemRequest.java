package com.learning.inventory_manager.web.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReserveItemRequest {
    @NotNull(message = "orderItemId is required")
    private UUID orderItemId;

    @NotNull(message = "supplierProductId is required")
    private UUID supplierProductId;

    @NotNull(message = "warehouseId is required")
    private UUID warehouseId;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be greater than 0")
    private Integer quantity;
}
