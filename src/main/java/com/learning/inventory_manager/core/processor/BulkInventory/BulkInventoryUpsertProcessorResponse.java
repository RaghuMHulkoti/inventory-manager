package com.learning.inventory_manager.core.processor.BulkInventory;

import com.learning.inventory_manager.core.processor.ProcessorResponse;

import java.util.List;
import java.util.UUID;

public record BulkInventoryUpsertProcessorResponse(
        Integer totalProcessed,
        Integer successCount,
        Integer failureCount,
        List<ItemResult> results
) implements ProcessorResponse {
    public BulkInventoryUpsertProcessorResponse {
        results = List.copyOf(results);
    }

    public record ItemResult(
            UUID supplierProductId,
            UUID warehouseId,
            String status,
            String operation,
            String errorMessage,
            Integer quantityDelta
    ) {
    }
}
