package com.learning.inventory_manager.presentation.rest.controller;

import com.learning.inventory_manager.application.processor.BulkInventoryUpsertProcessor;
import com.learning.inventory_manager.presentation.rest.dto.request.BulkInventoryUpsertRequest;
import com.learning.inventory_manager.presentation.rest.dto.response.BulkInventoryUpsertResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class InventoryController {
    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);
    private final BulkInventoryUpsertProcessor processor;

    /**
     * Bulk inventory upsert endpoint.
     */
    @PostMapping("/bulk-upsert")
    public ResponseEntity<BulkInventoryUpsertResponse> bulkUpsert(
        @Valid @RequestBody BulkInventoryUpsertRequest request
    ) {
        log.info("Received bulk inventory upsert request from supplier: {}", request.supplierId());
        log.debug("Request contains {} items", request.items().size());
        
        try {
            var response = processor.process(request);
            
            log.info("Bulk upsert completed successfully. Success: {}, Failure: {}",
                response.successCount(), response.failureCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Unexpected error during bulk inventory upsert", e);
            throw e;
        }
    }
}
