package com.learning.inventory_manager.web.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Result for a single item in a bulk inventory upsert operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpsertItemResult {
    private UUID supplierProductId;
    private UUID warehouseId;
    private String status;
    private String operation;
    private String errorMessage;
    private Integer quantityDelta;
}
