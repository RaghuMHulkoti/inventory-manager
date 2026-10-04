package com.learning.inventory_manager.core.usecase.bulkinventory;

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

class BulkInventoryUpsertUsecaseTest {

    private RecordingWarehouseGateway warehouseGateway;
    private FakeInventoryGateway inventoryGateway;
    private InventoryService inventoryService;
    private BulkInventoryUpsertUsecase useCase;
    private UUID supplierId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        warehouseGateway = new RecordingWarehouseGateway();
        inventoryGateway = new FakeInventoryGateway();
        inventoryService = new InventoryService(inventoryGateway, new RecordingLedgerGateway());
        useCase = new BulkInventoryUpsertUsecase(warehouseGateway, inventoryService);
        supplierId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
    }

    @Test
    void executeMapsServiceUpdateResponseIntoUsecaseResult() {
        var supplierProductId = UUID.randomUUID();
        // Pre-load existing inventory at 100
        inventoryGateway.put(existingInventory(supplierProductId, warehouseId, 100));

        // Request to push 150 — should be an UPDATE with delta = +50
        var incomingItem = new Inventory(java.util.UUID.randomUUID(), supplierProductId, warehouseId, 150, 0, 0L, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        var request = BulkInventoryUpsertUsecaseRequest.builder()
                .supplierId(supplierId)
                .items(List.of(incomingItem))
                .build();

        var response = useCase.execute(request);

        assertEquals(1, response.getResults().size());
        assertEquals(BulkInventoryUpsertUsecaseResponse.Status.SUCCESS, response.getResults().getFirst().getStatus());
        assertEquals(BulkInventoryUpsertUsecaseResponse.Operation.UPDATE, response.getResults().getFirst().getOperation());
        assertEquals(50, response.getResults().getFirst().getQuantityDelta());
    }

    @Test
    void executeKeepsProcessingAfterOneItemFails() {
        var validProductId = UUID.randomUUID();
        var invalidProductId = UUID.randomUUID();
        var invalidWarehouseId = UUID.randomUUID();
        warehouseGateway.invalidWarehouses.add(invalidWarehouseId);

        var validItem = new Inventory(java.util.UUID.randomUUID(), validProductId, warehouseId, 100, 0, 0L, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        var invalidItem = new Inventory(java.util.UUID.randomUUID(), invalidProductId, invalidWarehouseId, 50, 0, 0L, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());

        var request = BulkInventoryUpsertUsecaseRequest.builder()
                .supplierId(supplierId)
                .items(List.of(validItem, invalidItem))
                .build();

        var response = useCase.execute(request);

        assertEquals(2, response.getResults().size());
        assertEquals(BulkInventoryUpsertUsecaseResponse.Status.SUCCESS, response.getResults().get(0).getStatus());
        assertEquals(BulkInventoryUpsertUsecaseResponse.Status.FAILED, response.getResults().get(1).getStatus());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Inventory existingInventory(UUID supplierProductId, UUID warehouseId, int quantityOnHand) {
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

    // -------------------------------------------------------------------------
    // Test doubles
    // -------------------------------------------------------------------------

    private record InventoryKey(UUID supplierProductId, UUID warehouseId) {}

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

        void put(Inventory inventory) {
            inventories.put(
                    new InventoryKey(inventory.getSupplierProductId(), inventory.getWarehouseId()),
                    inventory
            );
        }

        @Override
        public java.util.List<Inventory> findBySupplierProductId(UUID supplierProductId) { return java.util.Collections.emptyList(); }

        @Override
        public boolean tryReserveStock(UUID inventoryId, int qty) { return true; }

        @Override
        public void releaseStock(UUID inventoryId, int qty) {}

        @Override
        public void deductStock(UUID inventoryId, int qty) {}

        @Override
        public Optional<Inventory> findById(UUID inventoryId) { return Optional.empty(); }
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
