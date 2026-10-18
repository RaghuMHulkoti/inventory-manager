package com.learning.inventory_manager.core.usecase.bulkinventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.service.inventory.InventoryService;
import com.learning.inventory_manager.core.service.inventory.InventoryServiceRequest;
import com.learning.inventory_manager.core.service.inventory.InventoryServiceResponse;
import com.learning.inventory_manager.core.usecase.Usecase;
import com.learning.inventory_manager.gateway.WarehouseGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Use case for bulk inventory upsert.
 *
 * Responsibility:
 * - Iterate over each Inventory domain object in the request
 * - Validate that the target warehouse exists, is active and belongs to the supplier (business rule)
 * - Delegate to InventoryService for the actual create/update logic
 * - Isolate per-item failures so one bad item never aborts the whole batch
 * - Return a result per item indicating success/failure and the operation performed
 *
 * This use case works exclusively with domain objects. No entity or web model classes
 * are referenced here.
 */
public class BulkInventoryUpsertUsecase implements Usecase<BulkInventoryUpsertUsecaseRequest, BulkInventoryUpsertUsecaseResponse> {

    private static final Logger log = LoggerFactory.getLogger(BulkInventoryUpsertUsecase.class);

    private final WarehouseGateway warehouseGateway;
    private final InventoryService inventoryService;

    public BulkInventoryUpsertUsecase(WarehouseGateway warehouseGateway, InventoryService inventoryService) {
        this.warehouseGateway = warehouseGateway;
        this.inventoryService = inventoryService;
    }

    @Override
    public BulkInventoryUpsertUsecaseResponse execute(BulkInventoryUpsertUsecaseRequest request) {
        log.info("Starting bulk inventory upsert for supplier: {} with {} items",
                request.getSupplierId(), request.getItems().size());

        var results = new ArrayList<BulkInventoryUpsertUsecaseResponse.ItemResult>();

        for (Inventory inventory : request.getItems()) {
            try {
                results.add(processInventoryItem(request.getSupplierId(), inventory));
            } catch (Exception e) {
                log.error("Error processing inventory item — product: {}, warehouse: {}",
                        inventory.getSupplierProductId(), inventory.getWarehouseId(), e);

                results.add(BulkInventoryUpsertUsecaseResponse.ItemResult.failure(
                        inventory.getSupplierProductId(),
                        inventory.getWarehouseId(),
                        e.getMessage()
                ));
            }
        }

        long successCount = results.stream()
                .filter(r -> r.getStatus() == BulkInventoryUpsertUsecaseResponse.Status.SUCCESS)
                .count();
        long failureCount = results.stream()
                .filter(r -> r.getStatus() == BulkInventoryUpsertUsecaseResponse.Status.FAILED)
                .count();

        log.info("Bulk inventory upsert completed. Successes: {}, Failures: {}", successCount, failureCount);

        return BulkInventoryUpsertUsecaseResponse.builder()
                .results(results)
                .build();
    }

    private BulkInventoryUpsertUsecaseResponse.ItemResult processInventoryItem(
            UUID supplierId,
            Inventory inventory
    ) {
        log.debug("Processing inventory item — product: {}, warehouse: {}, quantity: {}",
                inventory.getSupplierProductId(), inventory.getWarehouseId(), inventory.getQuantityOnHand());

        // Business validation: warehouse must exist, be active and belong to this supplier
        warehouseGateway.validateWarehouseBelongsToSupplier(inventory.getWarehouseId(), supplierId);
        log.debug("Warehouse validation passed for: {}", inventory.getWarehouseId());

        InventoryServiceRequest request = buildInventoryServiceRequest(inventory, supplierId);
        InventoryServiceResponse response = inventoryService.execute(request);

        if (response.getOperation() == InventoryServiceResponse.Operation.UPDATE) {
            return BulkInventoryUpsertUsecaseResponse.ItemResult.successUpdate(
                    inventory.getSupplierProductId(),
                    inventory.getWarehouseId(),
                    response.getQuantityDelta()
            );
        }

        return BulkInventoryUpsertUsecaseResponse.ItemResult.successCreate(
                inventory.getSupplierProductId(),
                inventory.getWarehouseId(),
                response.getQuantityDelta()
        );
    }

    private InventoryServiceRequest buildInventoryServiceRequest(Inventory inventory, UUID supplierId) {
        return InventoryServiceRequest.builder()
                .inventory(inventory)
                .supplierId(supplierId)
                .build();
    }
}
