package com.learning.inventory_manager.web.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bulk inventory upsert API response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInventoryUpsertResponse {
    private Integer totalProcessed;
    private Integer successCount;
    private Integer failureCount;
    private List<InventoryUpsertItemResult> results;
}
