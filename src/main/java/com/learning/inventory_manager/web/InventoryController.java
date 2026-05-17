package com.learning.inventory_manager.web;

import com.learning.inventory_manager.core.processor.BulkInventory.BulkInventoryUpsertProcessor;
import com.learning.inventory_manager.core.processor.BulkInventory.BulkInventoryUpsertProcessorRequest;
import com.learning.inventory_manager.web.model.BulkInventoryUpsertRequest;
import com.learning.inventory_manager.web.response.BulkInventoryUpsertResponse;
import com.learning.inventory_manager.web.response.InventoryUpsertItemResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Inventory REST Controller.
 * Exposes inventory management APIs to external consumers.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final BulkInventoryUpsertProcessor processor;

    public InventoryController(BulkInventoryUpsertProcessor processor) {
        this.processor = processor;
    }

    /**
     * Bulk inventory upsert endpoint.
     */
    @PostMapping("/bulk-upsert")
    public ResponseEntity<BulkInventoryUpsertResponse> bulkUpsert(
        @Valid @RequestBody BulkInventoryUpsertRequest request
    ) {
        log.info("Received bulk inventory upsert request from supplier: {}", request.supplierId());
        log.debug("Request contains {} items", request.items().size());
        
        var processorResponse = processor.process(new BulkInventoryUpsertProcessorRequest(
                request.supplierId(),
                request.items().stream()
                        .map(item -> new BulkInventoryUpsertProcessorRequest.Item(
                                item.supplierProductId(),
                                item.warehouseId(),
                                item.quantityOnHand()
                        ))
                        .toList()
        ));
        var response = new BulkInventoryUpsertResponse(
                processorResponse.totalProcessed(),
                processorResponse.successCount(),
                processorResponse.failureCount(),
                processorResponse.results().stream()
                        .map(item -> new InventoryUpsertItemResult(
                                item.supplierProductId(),
                                item.warehouseId(),
                                item.status(),
                                item.operation(),
                                item.errorMessage(),
                                item.quantityDelta()
                        ))
                        .toList()
        );

        log.info("Bulk upsert completed successfully. Success: {}, Failure: {}",
            response.successCount(), response.failureCount());

        return ResponseEntity.ok(response);
    }
}
