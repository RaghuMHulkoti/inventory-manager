package com.learning.inventory_manager.core.processor.BulkInventory;

import com.learning.inventory_manager.core.processor.Processor;
import com.learning.inventory_manager.core.usecase.bulkInventory.BulkInventoryUpsertUseCase;
import com.learning.inventory_manager.core.usecase.bulkInventory.BulkInventoryUpsertUsecaseRequest;
import com.learning.inventory_manager.core.usecase.bulkInventory.BulkInventoryUpsertUsecaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Bulk Inventory Upsert Processor.
 *
 * Bridges the controller and the use case layer.
 *
 * Responsibility:
 * - Pass the domain Inventory objects from the processor request to the use case
 * - Aggregate use case results into a processor response
 * - Convert domain enums (Status, Operation) to String values for the web layer
 * - Request-level logging
 *
 * No business logic lives here — that belongs exclusively in the use case.
 */
public class BulkInventoryUpsertProcessor implements Processor<BulkInventoryUpsertProcessorRequest, BulkInventoryUpsertProcessorResponse> {

    private static final Logger log = LoggerFactory.getLogger(BulkInventoryUpsertProcessor.class);

    private final BulkInventoryUpsertUseCase upsertUseCase;

    public BulkInventoryUpsertProcessor(BulkInventoryUpsertUseCase upsertUseCase) {
        this.upsertUseCase = upsertUseCase;
    }

    @Override
    public BulkInventoryUpsertProcessorResponse process(BulkInventoryUpsertProcessorRequest request) {
        log.info("Processing bulk inventory upsert request from supplier: {}", request.getSupplierId());

        var usecaseRequest = buildBulkInventoryUpsertUsecaseRequest(request);
        var usecaseResponse = upsertUseCase.execute(usecaseRequest);

        var response = buildResponse(usecaseResponse);

        log.info("Bulk inventory upsert request processed. Success: {}, Failure: {}",
                response.getSuccessCount(), response.getFailureCount());

        return response;
    }

    private BulkInventoryUpsertUsecaseRequest buildBulkInventoryUpsertUsecaseRequest(BulkInventoryUpsertProcessorRequest request) {
        return BulkInventoryUpsertUsecaseRequest.builder()
                .supplierId(request.getSupplierId())
                .items(request.getItems())
                .build();
    }

    private BulkInventoryUpsertProcessorResponse buildResponse(BulkInventoryUpsertUsecaseResponse usecaseResponse) {
        var results = usecaseResponse.getResults().stream()
                .map(result -> BulkInventoryUpsertProcessorResponse.ItemResult.builder()
                        .supplierProductId(result.getSupplierProductId())
                        .warehouseId(result.getWarehouseId())
                        .status(result.getStatus().name())
                        .operation(result.getOperation() == null ? null : result.getOperation().name())
                        .errorMessage(result.getErrorMessage())
                        .quantityDelta(result.getQuantityDelta())
                        .build())
                .toList();

        int successCount = (int) usecaseResponse.getResults().stream()
                .filter(r -> r.getStatus() == BulkInventoryUpsertUsecaseResponse.Status.SUCCESS)
                .count();
        int failureCount = results.size() - successCount;

        return BulkInventoryUpsertProcessorResponse.builder()
                .totalProcessed(results.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .build();
    }
}
