package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import com.learning.inventory_manager.core.domain.InventoryLedgerEntry.ChangeType;
import com.learning.inventory_manager.core.service.Service;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.exception.InventoryValidationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public class InventoryService implements Service<InventoryServiceRequest, InventoryServiceResponse> {

    private final InventoryGateway inventoryGateway;
    private final InventoryLedgerGateway ledgerGateway;

    public InventoryService(InventoryGateway inventoryGateway,
                            InventoryLedgerGateway ledgerGateway) {
        this.inventoryGateway = inventoryGateway;
        this.ledgerGateway = ledgerGateway;
    }

    @Override
    @Transactional
    public InventoryServiceResponse execute(InventoryServiceRequest request) {
        var incoming = request.getInventory();

        var existing = inventoryGateway.findBySupplierProductIdAndWarehouseId(
                incoming.getSupplierProductId(),
                incoming.getWarehouseId()
        ).orElse(null);

        if (existing == null) {
            Inventory newInventory = new Inventory(
                    UUID.randomUUID(),
                    incoming.getSupplierProductId(),
                    incoming.getWarehouseId(),
                    incoming.getQuantityOnHand(),
                    0,
                    incoming.getSafetyStock() != null ? incoming.getSafetyStock() : 0,
                    incoming.getReorderPoint() != null ? incoming.getReorderPoint() : 0,
                    null, // null version = new row; JPA sets it on insert
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    null
            );

            var created = inventoryGateway.create(newInventory);

            var ledgerEntry = InventoryLedgerEntry.builder()
                    .inventoryId(created.getId())
                    .changeType(ChangeType.SUPPLIER_PUSH)
                    .quantityDelta(created.getQuantityOnHand())
                    .quantityAfter(created.getQuantityOnHand())
                    .referenceType("SUPPLIER")
                    .referenceId("initial")
                    .actor("supplier-service")
                    .note("Initial stock")
                    .build();

            ledgerGateway.recordChange(ledgerEntry);

            return InventoryServiceResponse.builder()
                    .inventory(created)
                    .operation(InventoryServiceResponse.Operation.CREATE)
                    .quantityDelta(created.getQuantityOnHand())
                    .build();
        }

        int oldQty = existing.getQuantityOnHand();
        int delta = incoming.getQuantityOnHand() - oldQty;

        if (incoming.getQuantityOnHand() < existing.getQuantityReserved()) {
            throw new InventoryValidationException("Cannot update quantity on hand below reserved quantity");
        }

        existing.setQuantityOnHand(incoming.getQuantityOnHand());

        // Null means "not sent by caller" — keep the stored threshold
        if (incoming.getSafetyStock() != null || incoming.getReorderPoint() != null) {
            existing.setThresholds(incoming.getSafetyStock(), incoming.getReorderPoint());
        }

        var updated = inventoryGateway.save(existing);

        if (delta != 0) {
            var ledgerEntry = InventoryLedgerEntry.builder()
                    .inventoryId(updated.getId())
                    .changeType(ChangeType.SUPPLIER_PUSH)
                    .quantityDelta(delta)
                    .quantityAfter(updated.getQuantityOnHand())
                    .referenceType("SUPPLIER")
                    .referenceId("update")
                    .actor("supplier-service")
                    .note("Stock upsert update")
                    .build();

            ledgerGateway.recordChange(ledgerEntry);
        }

        return InventoryServiceResponse.builder()
                .inventory(updated)
                .operation(InventoryServiceResponse.Operation.UPDATE)
                .quantityDelta(delta)
                .build();
    }
}
