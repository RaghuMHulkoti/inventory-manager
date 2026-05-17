package com.learning.inventory_manager.core.usecase.bulkInventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import com.learning.inventory_manager.core.service.inventory.InventoryService;
import com.learning.inventory_manager.exception.WarehouseNotFoundException;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.gateway.WarehouseGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkInventoryUpsertUseCaseTest {
    private RecordingWarehouseGateway warehouseGateway;
    private FakeInventoryGateway inventoryGateway;
    private InventoryService inventoryService;
    private BulkInventoryUpsertUseCase useCase;
    private UUID supplierId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        warehouseGateway = new RecordingWarehouseGateway();
        inventoryGateway = new FakeInventoryGateway();
        inventoryService = new InventoryService(inventoryGateway, new RecordingLedgerGateway());
        useCase = new BulkInventoryUpsertUseCase(warehouseGateway, inventoryService);
        supplierId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
    }

    @Test
    void executeMapsServiceUpdateResponseIntoUsecaseResult() {
        var supplierProductId = UUID.randomUUID();
        inventoryGateway.put(inventory(supplierProductId, warehouseId, 100));
        var request = new BulkInventoryUpsertUsecaseRequest(
                supplierId,
                List.of(new BulkInventoryUpsertUsecaseRequest.Item(supplierProductId, warehouseId, 150))
        );

        var response = useCase.execute(request);

        assertEquals(1, response.results().size());
        assertEquals(BulkInventoryUpsertUsecaseResponse.Status.SUCCESS, response.results().getFirst().status());
        assertEquals(BulkInventoryUpsertUsecaseResponse.Operation.UPDATE, response.results().getFirst().operation());
        assertEquals(50, response.results().getFirst().quantityDelta());
    }

    @Test
    void executeKeepsProcessingAfterOneItemFails() {
        var validProductId = UUID.randomUUID();
        var invalidProductId = UUID.randomUUID();
        var invalidWarehouseId = UUID.randomUUID();
        warehouseGateway.invalidWarehouses.add(invalidWarehouseId);
        var request = new BulkInventoryUpsertUsecaseRequest(
                supplierId,
                List.of(
                        new BulkInventoryUpsertUsecaseRequest.Item(validProductId, warehouseId, 100),
                        new BulkInventoryUpsertUsecaseRequest.Item(invalidProductId, invalidWarehouseId, 50)
                )
        );

        var response = useCase.execute(request);

        assertEquals(2, response.results().size());
        assertEquals(BulkInventoryUpsertUsecaseResponse.Status.SUCCESS, response.results().get(0).status());
        assertEquals(BulkInventoryUpsertUsecaseResponse.Status.FAILED, response.results().get(1).status());
    }

    private Inventory inventory(UUID supplierProductId, UUID warehouseId, int quantityOnHand) {
        var now = LocalDateTime.now();
        return new Inventory(
                UUID.randomUUID(),
                supplierProductId,
                warehouseId,
                quantityOnHand,
                0,
                0L,
                now,
                now
        );
    }

    private record InventoryKey(UUID supplierProductId, UUID warehouseId) {
    }

    private static final class FakeInventoryGateway implements InventoryGateway {
        private final Map<InventoryKey, Inventory> inventories = new HashMap<>();

        @Override
        public Optional<Inventory> findBySupplierProductIdAndWarehouseId(UUID supplierProductId, UUID warehouseId) {
            return Optional.ofNullable(inventories.get(new InventoryKey(supplierProductId, warehouseId)));
        }

        @Override
        public Inventory save(Inventory inventory) {
            put(inventory);
            return inventory;
        }

        @Override
        public Inventory create(Inventory inventory) {
            put(inventory);
            return inventory;
        }

        private void put(Inventory inventory) {
            inventories.put(
                    new InventoryKey(inventory.getSupplierProductId(), inventory.getWarehouseId()),
                    inventory
            );
        }
    }

    private static final class RecordingLedgerGateway implements InventoryLedgerGateway {
        private final List<InventoryLedgerEntry> entries = new ArrayList<>();

        @Override
        public void recordChange(InventoryLedgerEntry entry) {
            entries.add(entry);
        }
    }

    private static final class RecordingWarehouseGateway implements WarehouseGateway {
        private final Set<UUID> invalidWarehouses = new HashSet<>();

        @Override
        public void validateWarehouseExists(UUID warehouseId) {
            if (invalidWarehouses.contains(warehouseId)) {
                throw new WarehouseNotFoundException(warehouseId);
            }
        }
    }
}
