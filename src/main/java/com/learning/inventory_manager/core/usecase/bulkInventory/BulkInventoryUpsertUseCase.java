package com.learning.inventory_manager.core.usecase.bulkInventory;

import com.learning.inventory_manager.core.service.inventory.InventoryService;
import com.learning.inventory_manager.core.service.inventory.InventoryServiceRequest;
import com.learning.inventory_manager.core.service.inventory.InventoryServiceResponse;
import com.learning.inventory_manager.core.usecase.Usecase;
import com.learning.inventory_manager.gateway.WarehouseGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Component
public class BulkInventoryUpsertUseCase implements Usecase<BulkInventoryUpsertUsecaseRequest, BulkInventoryUpsertUsecaseResponse> {
    private static final Logger log = LoggerFactory.getLogger(BulkInventoryUpsertUseCase.class);
    private final WarehouseGateway warehouseGateway;
    private final InventoryService inventoryService;

    public BulkInventoryUpsertUseCase(WarehouseGateway warehouseGateway, InventoryService inventoryService) {
        this.warehouseGateway = warehouseGateway;
        this.inventoryService = inventoryService;
    }

    @Override
    public BulkInventoryUpsertUsecaseResponse execute(BulkInventoryUpsertUsecaseRequest request) {
        log.info("Starting bulk inventory upsert for supplier: {} with {} items",
                request.supplierId(), request.items().size());

        var results = new ArrayList<BulkInventoryUpsertUsecaseResponse.ItemResult>();

        for (BulkInventoryUpsertUsecaseRequest.Item item : request.items()) {
            try {
                var result = processInventoryItem(request.supplierId(), item);
                results.add(result);
            } catch (Exception e) {
                log.error("Error processing inventory item - product: {}, warehouse: {}",
                        item.supplierProductId(), item.warehouseId(), e);

                var failureResult = BulkInventoryUpsertUsecaseResponse.ItemResult.failure(
                        item.supplierProductId(),
                        item.warehouseId(),
                        e.getMessage()
                );
                results.add(failureResult);
            }
        }

        log.info("Bulk inventory upsert completed. Successes: {}, Failures: {}",
                results.stream()
                        .filter(r -> r.status() == BulkInventoryUpsertUsecaseResponse.Status.SUCCESS)
                        .count(),
                results.stream()
                        .filter(r -> r.status() == BulkInventoryUpsertUsecaseResponse.Status.FAILED)
                        .count());

        return new BulkInventoryUpsertUsecaseResponse(results);
    }

    private BulkInventoryUpsertUsecaseResponse.ItemResult processInventoryItem(
            UUID supplierId,
            BulkInventoryUpsertUsecaseRequest.Item item
    ) {
        log.debug("Processing inventory item - product: {}, warehouse: {}, quantity: {}",
                item.supplierProductId(), item.warehouseId(), item.quantityOnHand());

        // Step 1: Validate warehouse exists and is active
        warehouseGateway.validateWarehouseExists(item.warehouseId());
        log.debug("Warehouse validation passed for: {}", item.warehouseId());

        var response = inventoryService.execute(new InventoryServiceRequest(
                item.supplierProductId(),
                item.warehouseId(),
                item.quantityOnHand(),
                supplierId
        ));

        if (response.operation() == InventoryServiceResponse.Operation.UPDATE) {
            return BulkInventoryUpsertUsecaseResponse.ItemResult.successUpdate(
                    item.supplierProductId(),
                    item.warehouseId(),
                    response.quantityDelta()
            );
        }

        return BulkInventoryUpsertUsecaseResponse.ItemResult.successCreate(
                item.supplierProductId(),
                item.warehouseId(),
                response.quantityDelta()
        );
    }
}
