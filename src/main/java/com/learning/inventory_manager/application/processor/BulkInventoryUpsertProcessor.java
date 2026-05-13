package com.learning.inventory_manager.application.processor;

import com.learning.inventory_manager.application.usecase.BulkInventoryUpsertUseCase;
import com.learning.inventory_manager.presentation.rest.dto.request.BulkInventoryUpsertRequest;
import com.learning.inventory_manager.presentation.rest.dto.response.BulkInventoryUpsertResponse;
import com.learning.inventory_manager.presentation.rest.dto.response.InventoryUpsertItemResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * Bulk Inventory Upsert Processor.
 * Bridges controller and usecase layers.
 *
 * Responsibility:
 * - Orchestrate request flow
 * - Convert DTOs if needed (here minimal conversion)
 * - Call appropriate usecase
 * - Build response DTOs
 * - Request-level logging
 *
 * Processing Flow:
 * 1. Accept request from controller
 * 2. Call usecase
 * 3. Aggregate results
 * 4. Build response
 * 5. Return to controller
 */
@Component
public class BulkInventoryUpsertProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BulkInventoryUpsertProcessor.class);
    private final BulkInventoryUpsertUseCase upsertUseCase;

    public BulkInventoryUpsertProcessor(BulkInventoryUpsertUseCase upsertUseCase) {
        this.upsertUseCase = upsertUseCase;
    }

    /**
     * Process bulk inventory upsert request.
     *
     * @param request the bulk upsert request
     * @return response with results for each item
     */
    public BulkInventoryUpsertResponse process(BulkInventoryUpsertRequest request) {
        logger.info("Processing bulk inventory upsert request from supplier: {}", request.supplierId());
        
        // Execute usecase
        List<InventoryUpsertItemResult> results = upsertUseCase.execute(
            request.supplierId(),
            request.items()
        );
        
        // Build response
        var response = buildResponse(results);
        
        logger.info("Bulk inventory upsert request processed. Success: {}, Failure: {}",
            response.successCount(), response.failureCount());
        
        return response;
    }

    /**
     * Build response from individual item results.
     *
     * @param results list of item results
     * @return aggregated response
     */
    private BulkInventoryUpsertResponse buildResponse(List<InventoryUpsertItemResult> results) {
        int successCount = (int) results.stream()
            .filter(r -> "SUCCESS".equals(r.status()))
            .count();
        
        int failureCount = results.size() - successCount;
        
        return new BulkInventoryUpsertResponse(
            results.size(),
            successCount,
            failureCount,
            results
        );
    }
}



