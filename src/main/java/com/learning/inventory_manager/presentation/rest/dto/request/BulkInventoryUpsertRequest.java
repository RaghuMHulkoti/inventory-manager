package com.learning.inventory_manager.presentation.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Bulk inventory upsert request DTO.
 * Represents the API request to update multiple inventory items.
 *
 * @param supplierId the supplier ID initiating the push
 * @param items list of inventory items to upsert
 */
public record BulkInventoryUpsertRequest(
    @NotNull(message = "Supplier ID is required")
    UUID supplierId,

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    List<InventoryItemRequest> items
) {
}

