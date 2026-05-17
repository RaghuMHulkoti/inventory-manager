package com.learning.inventory_manager.web.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Individual inventory item request DTO.
 * Represents a single inventory update within a bulk request.
 *
 * @param supplierProductId the supplier product ID
 * @param warehouseId the warehouse ID
 * @param quantityOnHand the quantity on hand to set
 */
public record InventoryItemRequest(
    @NotNull(message = "Supplier product ID is required")
    UUID supplierProductId,

    @NotNull(message = "Warehouse ID is required")
    UUID warehouseId,

    @NotNull(message = "Quantity on hand is required")
    @Min(value = 0, message = "Quantity on hand cannot be negative")
    Integer quantityOnHand
) {
}

