package com.learning.inventory_manager.web.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Individual inventory item request DTO.
 * Represents a single inventory update within a bulk request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemRequest {

    @NotNull(message = "Supplier product ID is required")
    private UUID supplierProductId;

    @NotNull(message = "Warehouse ID is required")
    private UUID warehouseId;

    @NotNull(message = "Quantity on hand is required")
    @Min(value = 0, message = "Quantity on hand cannot be negative")
    private Integer quantityOnHand;
}
