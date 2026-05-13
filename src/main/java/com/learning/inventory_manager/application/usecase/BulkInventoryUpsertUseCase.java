package com.learning.inventory_manager.application.usecase;

import com.learning.inventory_manager.application.service.InventoryService;
import com.learning.inventory_manager.domain.exception.InventoryException;
import com.learning.inventory_manager.domain.gateway.WarehouseGateway;
import com.learning.inventory_manager.domain.gateway.InventoryGateway;
import com.learning.inventory_manager.presentation.rest.dto.request.InventoryItemRequest;
import com.learning.inventory_manager.presentation.rest.dto.response.InventoryUpsertItemResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bulk Inventory Upsert UseCase.
 * Orchestrates the workflow for bulk inventory updates with support for partial success.
 *
 * Responsibility:
 * - Business orchestration (workflow)
 * - Transaction boundaries (handles @Transactional)
 * - Coordinating services and gateways
 * - Partial success handling (invalid items don't fail entire operation)
 * - Comprehensive logging for debugging
 *
 * Workflow:
 * 1. Validate request (done by processor)
 * 2. For each item:
 *    a. Validate warehouse exists
 *    b. Check if inventory exists
 *    c. Create or update inventory
 *    d. Capture result or error
 * 3. Return response with success/failure details
 */
@Component
@RequiredArgsConstructor
public class BulkInventoryUpsertUseCase {
    private static final Logger log = LoggerFactory.getLogger(BulkInventoryUpsertUseCase.class);
    private final WarehouseGateway warehouseGateway;
    private final InventoryGateway inventoryGateway;
    private final InventoryService inventoryService;

    /**
     * Execute bulk inventory upsert.
     * Processes items independently, capturing both successes and failures.
     * Partial success is supported - failures in one item don't affect others.
     *
     * @param supplierId the supplier ID
     * @param items list of inventory items to upsert
     * @return list of results for each item (success or failure)
     */
    @Transactional
    public List<InventoryUpsertItemResult> execute(UUID supplierId, List<InventoryItemRequest> items) {
        log.info("Starting bulk inventory upsert for supplier: {} with {} items", 
            supplierId, items.size());
        
        List<InventoryUpsertItemResult> results = new ArrayList<>();
        
        for (InventoryItemRequest item : items) {
            try {
                var result = processInventoryItem(supplierId, item);
                results.add(result);
            } catch (Exception e) {
                log.error("Error processing inventory item - product: {}, warehouse: {}",
                    item.supplierProductId(), item.warehouseId(), e);
                
                var failureResult = InventoryUpsertItemResult.failure(
                    item.supplierProductId(),
                    item.warehouseId(),
                    e.getMessage()
                );
                results.add(failureResult);
            }
        }
        
        log.info("Bulk inventory upsert completed. Successes: {}, Failures: {}",
            results.stream().filter(r -> "SUCCESS".equals(r.status())).count(),
            results.stream().filter(r -> "FAILED".equals(r.status())).count());
        
        return results;
    }

    /**
     * Process a single inventory item.
     * Validates warehouse and decides whether to create or update.
     *
     * @param supplierId the supplier ID
     * @param item the inventory item to process
     * @return result of processing
     * @throws InventoryException if validation or operation fails
     */
    private InventoryUpsertItemResult processInventoryItem(UUID supplierId, InventoryItemRequest item) {
        log.debug("Processing inventory item - product: {}, warehouse: {}, quantity: {}",
            item.supplierProductId(), item.warehouseId(), item.quantityOnHand());
        
        // Step 1: Validate warehouse exists and is active
        warehouseGateway.validateWarehouseExists(item.warehouseId());
        log.debug("Warehouse validation passed for: {}", item.warehouseId());
        
        // Step 2: Check if inventory exists
        var existingInventory = inventoryGateway.findBySupplierProductIdAndWarehouseId(
            item.supplierProductId(),
            item.warehouseId()
        );
        
        if (existingInventory.isPresent()) {
            // Step 3a: Update existing inventory
            log.debug("Inventory exists, updating - ID: {}", existingInventory.get().getId());
            var updated = inventoryService.updateInventory(
                existingInventory.get(),
                item.quantityOnHand(),
                supplierId
            );
            
            int delta = item.quantityOnHand() - updated.getQuantityOnHand();
            return InventoryUpsertItemResult.successUpdate(
                item.supplierProductId(),
                item.warehouseId(),
                delta
            );
        } else {
            // Step 3b: Create new inventory
            log.debug("Inventory does not exist, creating - product: {}, warehouse: {}",
                item.supplierProductId(), item.warehouseId());
            var created = inventoryService.createInventory(
                item.supplierProductId(),
                item.warehouseId(),
                item.quantityOnHand(),
                supplierId
            );
            
            return InventoryUpsertItemResult.successCreate(
                item.supplierProductId(),
                item.warehouseId(),
                created.getQuantityOnHand()
            );
        }
    }
}

