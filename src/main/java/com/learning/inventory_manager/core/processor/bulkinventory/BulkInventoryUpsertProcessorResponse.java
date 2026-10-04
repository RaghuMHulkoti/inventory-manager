package com.learning.inventory_manager.core.processor.bulkinventory;

import com.learning.inventory_manager.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Processor response for the bulk inventory upsert operation.
 * Aggregates per-item results along with overall success/failure counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInventoryUpsertProcessorResponse implements ProcessorResponse {

    private Integer totalProcessed;
    private Integer successCount;
    private Integer failureCount;
    private List<ItemResult> results;

    /**
     * Result for a single inventory item, expressed as strings for the web layer.
     * Status and operation are serialised as their enum name strings here
     * so that no domain enums leak into the HTTP response model.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResult {
        private UUID supplierProductId;
        private UUID warehouseId;
        private String status;
        private String operation;
        private String errorMessage;
        private Integer quantityDelta;
    }
}
