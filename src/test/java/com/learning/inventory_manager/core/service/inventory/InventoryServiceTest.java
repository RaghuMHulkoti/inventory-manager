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
        var request = new InventoryServiceRequest(supplierProductId, warehouseId, 100, supplierId);

        var response = service.execute(request);

        assertEquals(InventoryServiceResponse.Operation.CREATE, response.operation());
        assertEquals(100, response.quantityDelta());
        assertEquals(100, response.inventory().getQuantityOnHand());
        assertEquals(1, inventoryGateway.createCount);
        assertEquals(1, ledgerGateway.entries.size());
    }

    @Test
    void executeUpdatesInventoryAndReturnsActualDelta() {
        inventoryGateway.inventory = Optional.of(inventoryWithQuantities(100, 0));
        var request = new InventoryServiceRequest(supplierProductId, warehouseId, 150, supplierId);

        var response = service.execute(request);

        assertEquals(InventoryServiceResponse.Operation.UPDATE, response.operation());
        assertEquals(50, response.quantityDelta());
        assertEquals(150, response.inventory().getQuantityOnHand());
        assertEquals(1, inventoryGateway.saveCount);
        assertEquals(1, ledgerGateway.entries.size());
    }

    @Test
    void executeRejectsUpdateBelowReservedQuantity() {
        inventoryGateway.inventory = Optional.of(inventoryWithQuantities(100, 80));
        var request = new InventoryServiceRequest(supplierProductId, warehouseId, 50, supplierId);

        assertThrows(InventoryValidationException.class, () -> service.execute(request));
        assertEquals(0, inventoryGateway.saveCount);
        assertEquals(0, ledgerGateway.entries.size());
    }

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
    }

    private static final class RecordingLedgerGateway implements InventoryLedgerGateway {
        private final List<InventoryLedgerEntry> entries = new ArrayList<>();

        @Override
        public void recordChange(InventoryLedgerEntry entry) {
            entries.add(entry);
        }
    }
}
