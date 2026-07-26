package com.learning.inventory_manager.web.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Bulk inventory upsert request DTO.
 * Represents the API request to update multiple inventory items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInventoryUpsertRequest {

    @NotNull(message = "Supplier ID is required")
    private UUID supplierId;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<InventoryItemRequest> items;
}
