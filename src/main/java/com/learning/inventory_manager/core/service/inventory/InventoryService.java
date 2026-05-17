package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.exception.InventoryValidationException;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.domain.InventoryLedgerEntry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inventory Service - Core business logic for inventory operations.
 * Handles inventory calculations, validations, and state transitions.
 *
 * Responsibility:
 * - Core inventory business rules
 * - Quantity calculations
 * - Delta calculations
 * - Ledger operations
 * - One transaction per inventory item update
 */
@Service
@RequiredArgsConstructor
public class InventoryService implements com.learning.inventory_manager.core.service.Service<InventoryServiceRequest, InventoryServiceResponse> {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryGateway inventoryGateway;
    private final InventoryLedgerGateway ledgerGateway;

    @Override
    @Transactional
    public InventoryServiceResponse execute(InventoryServiceRequest request) {
        validateQuantity(request.quantityOnHand());

        return inventoryGateway.findBySupplierProductIdAndWarehouseId(
                        request.supplierProductId(),
                        request.warehouseId()
                )
                .map(existing -> updateInventory(existing, request))
                .orElseGet(() -> createInventory(request));
    }

    /**
     * Create new inventory with initial stock quantity.
     * Creates ledger entry recording the initial stock push.
     */
    private InventoryServiceResponse createInventory(InventoryServiceRequest request) {
        log.info("Creating new inventory for product {} in warehouse {}",
                request.supplierProductId(), request.warehouseId());

        var inventory = Inventory.create(
                request.supplierProductId(),
                request.warehouseId(),
                request.quantityOnHand()
        );
        var created = inventoryGateway.create(inventory);

        // Record initial push in ledger
        var ledgerEntry = InventoryLedgerEntry.supplierPush(
                created.getId(),
                request.quantityOnHand(),
                request.quantityOnHand(),
                request.supplierId()
        );
        ledgerGateway.recordChange(ledgerEntry);

        log.info("Inventory created successfully: {}", created.getId());
        return new InventoryServiceResponse(
                created,
                InventoryServiceResponse.Operation.CREATE,
                request.quantityOnHand()
        );
    }

    /**
     * Update existing inventory with new quantity.
     * Calculates delta and creates appropriate ledger entry.
     * Validates quantity and reservation constraints.
     */
    private InventoryServiceResponse updateInventory(Inventory inventory, InventoryServiceRequest request) {
        log.info("Updating inventory {} with new quantity {}", inventory.getId(), request.quantityOnHand());

        int oldQuantity = inventory.getQuantityOnHand();
        int delta = request.quantityOnHand() - oldQuantity;

        // Validate that reserved quantity doesn't exceed new quantity
        if (inventory.getQuantityReserved() > request.quantityOnHand()) {
            log.error("Cannot update inventory: reserved quantity {} exceeds new quantity {}",
                    inventory.getQuantityReserved(), request.quantityOnHand());
            throw new InventoryValidationException(
                    "Cannot reduce quantity below reserved amount. Reserved: "
                            + inventory.getQuantityReserved()
                            + ", New quantity: "
                            + request.quantityOnHand()
            );
        }

        inventory.setQuantityOnHand(request.quantityOnHand());
        var updated = inventoryGateway.save(inventory);

        // Record in ledger
        var ledgerEntry = InventoryLedgerEntry.supplierPush(
                updated.getId(),
                delta,
                request.quantityOnHand(),
                request.supplierId()
        );
        ledgerGateway.recordChange(ledgerEntry);

        log.info("Inventory updated successfully. Delta: {}", delta);
        return new InventoryServiceResponse(
                updated,
                InventoryServiceResponse.Operation.UPDATE,
                delta
        );
    }

    /**
     * Validate that quantity is non-negative.
     * Throws exception if validation fails.
     *
     * @param quantity the quantity to validate
     * @throws InventoryValidationException if quantity is negative
     */
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new InventoryValidationException("Quantity cannot be null or negative");
        }
    }
}
