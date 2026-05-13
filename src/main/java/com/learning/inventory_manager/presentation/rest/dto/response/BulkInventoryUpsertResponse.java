package com.learning.inventory_manager.presentation.rest.dto.response;

import java.util.List;

/**
 * Bulk inventory upsert response DTO.
 * Contains results of the bulk operation with summary and detailed results per item.
 *
 * @param totalProcessed total number of items processed
 * @param successCount number of items successfully processed
 * @param failureCount number of items that failed
 * @param results detailed results for each item
 */
public record BulkInventoryUpsertResponse(
    Integer totalProcessed,
    Integer successCount,
    Integer failureCount,
    List<InventoryUpsertItemResult> results
) {
}

