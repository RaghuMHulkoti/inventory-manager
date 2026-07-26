package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import com.learning.inventory_manager.exception.InventoryValidationException;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryServiceTest {

    private FakeInventoryGateway inventoryGateway;
    private RecordingLedgerGateway ledgerGateway;
    private InventoryService service;
    private UUID supplierId;
    private UUID supplierProductId;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        inventoryGateway = new FakeInventoryGateway();
        ledgerGateway = new RecordingLedgerGateway();
        service = new InventoryService(inventoryGateway, ledgerGateway);
        supplierId = UUID.randomUUID();
        supplierProductId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
    }

    @Test
    void executeCreatesInventoryWhenNoExistingInventoryIsFound() {
        // Incoming domain object: new inventory with quantity 100
        var incoming = new Inventory(java.util.UUID.randomUUID(), supplierProductId, warehouseId, 100, 0, 0L, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        var request = InventoryServiceRequest.builder()
                .inventory(incoming)
                .supplierId(supplierId)
                .build();

        var response = service.execute(request);

        assertEquals(InventoryServiceResponse.Operation.CREATE, response.getOperation());
        assertEquals(100, response.getQuantityDelta());
        assertEquals(100, response.getInventory().getQuantityOnHand());
        assertEquals(1, inventoryGateway.createCount);
        assertEquals(1, ledgerGateway.entries.size());
    }

    @Test
    void executeUpdatesInventoryAndReturnsActualDelta() {
        // Existing inventory already has 100
        inventoryGateway.inventory = Optional.of(inventoryWithQuantities(100, 0));
        // Incoming wants to set it to 150
        var incoming = new Inventory(java.util.UUID.randomUUID(), supplierProductId, warehouseId, 150, 0, 0L, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        var request = InventoryServiceRequest.builder()
                .inventory(incoming)
                .supplierId(supplierId)
                .build();

        var response = service.execute(request);

        assertEquals(InventoryServiceResponse.Operation.UPDATE, response.getOperation());
        assertEquals(50, response.getQuantityDelta());
        assertEquals(150, response.getInventory().getQuantityOnHand());
        assertEquals(1, inventoryGateway.saveCount);
        assertEquals(1, ledgerGateway.entries.size());
    }

    @Test
    void executeRejectsUpdateBelowReservedQuantity() {
        // Existing: 100 on hand, 80 reserved
        inventoryGateway.inventory = Optional.of(inventoryWithQuantities(100, 80));
        // Trying to reduce to 50 — below the 80 reserved
        var incoming = new Inventory(java.util.UUID.randomUUID(), supplierProductId, warehouseId, 50, 0, 0L, java.time.LocalDateTime.now(), java.time.LocalDateTime.now());
        var request = InventoryServiceRequest.builder()
                .inventory(incoming)
                .supplierId(supplierId)
                .build();

        assertThrows(InventoryValidationException.class, () -> service.execute(request));
        assertEquals(0, inventoryGateway.saveCount);
        assertEquals(0, ledgerGateway.entries.size());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Inventory inventoryWithQuantities(int quantityOnHand, int quantityReserved) {
        var now = LocalDateTime.now();
        return new Inventory(
                UUID.randomUUID(),
                supplierProductId,
                warehouseId,
                quantityOnHand,
                quantityReserved,
                0L,
                now,
                now
        );
    }

    // -------------------------------------------------------------------------
    // Test doubles
    // -------------------------------------------------------------------------

    private static final class FakeInventoryGateway implements InventoryGateway {
        private Optional<Inventory> inventory = Optional.empty();
        private int createCount;
        private int saveCount;

        @Override
        public Optional<Inventory> findBySupplierProductIdAndWarehouseId(UUID supplierProductId, UUID warehouseId) {
            return inventory;
        }

        @Override
        public Inventory save(Inventory inventory) {
            saveCount++;
            this.inventory = Optional.of(inventory);
            return inventory;
        }

        @Override
        public Inventory create(Inventory inventory) {
            createCount++;
            this.inventory = Optional.of(inventory);
            return inventory;
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
}
