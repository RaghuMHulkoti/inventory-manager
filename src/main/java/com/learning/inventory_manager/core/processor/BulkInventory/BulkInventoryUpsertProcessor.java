package com.learning.inventory_manager.core.processor.BulkInventory;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.bulkInventory.BulkInventoryUpsertUseCase;
import com.learning.inventory_manager.core.usecase.bulkInventory.BulkInventoryUpsertUsecaseRequest;
import com.learning.inventory_manager.core.usecase.bulkInventory.BulkInventoryUpsertUsecaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
public class BulkInventoryUpsertProcessor implements Processor<BulkInventoryUpsertProcessorRequest, BulkInventoryUpsertProcessorResponse> {
    private static final Logger logger = LoggerFactory.getLogger(BulkInventoryUpsertProcessor.class);
    private final BulkInventoryUpsertUseCase upsertUseCase;

    public BulkInventoryUpsertProcessor(BulkInventoryUpsertUseCase upsertUseCase) {
        this.upsertUseCase = upsertUseCase;
    }


    @Override
    public BulkInventoryUpsertProcessorResponse process(BulkInventoryUpsertProcessorRequest request) {
        logger.info("Processing bulk inventory upsert request from supplier: {}", request.supplierId());

        var usecaseResponse = upsertUseCase.execute(new BulkInventoryUpsertUsecaseRequest(
                request.supplierId(),
                request.items().stream()
                        .map(item -> new BulkInventoryUpsertUsecaseRequest.Item(
                                item.supplierProductId(),
                                item.warehouseId(),
                                item.quantityOnHand()
                        ))
                        .toList()
        ));

        var response = buildResponse(usecaseResponse);

        logger.info("Bulk inventory upsert request processed. Success: {}, Failure: {}",
                response.successCount(), response.failureCount());

        return response;
    }

    private BulkInventoryUpsertProcessorResponse buildResponse(BulkInventoryUpsertUsecaseResponse usecaseResponse) {
        var results = usecaseResponse.results().stream()
                .map(result -> new BulkInventoryUpsertProcessorResponse.ItemResult(
                        result.supplierProductId(),
                        result.warehouseId(),
                        result.status().name(),
                        result.operation() == null ? null : result.operation().name(),
                        result.errorMessage(),
                        result.quantityDelta()
                ))
                .toList();

        int successCount = (int) usecaseResponse.results().stream()
                .filter(r -> r.status() == BulkInventoryUpsertUsecaseResponse.Status.SUCCESS)
                .count();

        int failureCount = results.size() - successCount;

        return new BulkInventoryUpsertProcessorResponse(
                results.size(),
                successCount,
                failureCount,
                results
        );
    }
}


