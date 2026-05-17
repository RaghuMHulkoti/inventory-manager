package com.learning.inventory_manager.web.response;

import java.util.List;

public record BulkInventoryUpsertResponse(
        Integer totalProcessed,
        Integer successCount,
        Integer failureCount,
        List<InventoryUpsertItemResult> results
) {
    public BulkInventoryUpsertResponse {
        results = List.copyOf(results);
    }
}
