package com.learning.inventory_manager.application.service;

import com.learning.inventory_manager.domain.exception.InventoryValidationException;
import com.learning.inventory_manager.domain.gateway.InventoryGateway;
import com.learning.inventory_manager.domain.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.domain.model.Inventory;
import com.learning.inventory_manager.domain.model.InventoryLedgerEntry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Inventory Service - Core business logic for inventory operations.
 * Handles inventory calculations, validations, and state transitions.
 *
 * Responsibility:
 * - Core inventory business rules
 * - Quantity calculations
 * - Delta calculations
 * - Ledger operations
 * - No transaction boundaries (handled at UseCase level)
 */
@Service
@RequiredArgsConstructor
public class InventoryService {
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryGateway inventoryGateway;
    private final InventoryLedgerGateway ledgerGateway;

    /**
     * Create new inventory with initial stock quantity.
     * Creates ledger entry recording the initial stock push.
     *
     * @param supplierProductId the supplier product ID
     * @param warehouseId the warehouse ID
     * @param quantityOnHand the initial quantity
     * @param supplierId the supplier ID (for audit trail)
     * @return created inventory
     */
    public Inventory createInventory(UUID supplierProductId, UUID warehouseId, 
                                     Integer quantityOnHand, UUID supplierId) {
        log.info("Creating new inventory for product {} in warehouse {}", supplierProductId, warehouseId);
        
        validateQuantity(quantityOnHand);
        
        var inventory = Inventory.create(supplierProductId, warehouseId, quantityOnHand);
        var created = inventoryGateway.create(inventory);
        
        // Record initial push in ledger
        var ledgerEntry = InventoryLedgerEntry.supplierPush(
            created.getId(),
            quantityOnHand,
            quantityOnHand,
            supplierId
        );
        ledgerGateway.recordChange(ledgerEntry);
        
        log.info("Inventory created successfully: {}", created.getId());
        return created;
    }

    /**
     * Update existing inventory with new quantity.
     * Calculates delta and creates appropriate ledger entry.
     * Validates quantity and reservation constraints.
     *
     * @param inventory the existing inventory to update
     * @param newQuantityOnHand the new quantity on hand
     * @param supplierId the supplier ID (for audit trail)
     * @return updated inventory
     * @throws InventoryValidationException if quantity would result in invalid state
     */
    public Inventory updateInventory(Inventory inventory, Integer newQuantityOnHand, UUID supplierId) {
        log.info("Updating inventory {} with new quantity {}", inventory.getId(), newQuantityOnHand);
        
        validateQuantity(newQuantityOnHand);
        
        int oldQuantity = inventory.getQuantityOnHand();
        int delta = newQuantityOnHand - oldQuantity;
        
        // Validate that reserved quantity doesn't exceed new quantity
        if (inventory.getQuantityReserved() > newQuantityOnHand) {
            log.error("Cannot update inventory: reserved quantity {} exceeds new quantity {}", 
                inventory.getQuantityReserved(), newQuantityOnHand);
            throw new InventoryValidationException(
                "Cannot reduce quantity below reserved amount. Reserved: " + 
                inventory.getQuantityReserved() + ", New quantity: " + newQuantityOnHand
            );
        }
        
        inventory.setQuantityOnHand(newQuantityOnHand);
        var updated = inventoryGateway.save(inventory);
        
        // Record in ledger
        var ledgerEntry = InventoryLedgerEntry.supplierPush(
            updated.getId(),
            delta,
            newQuantityOnHand,
            supplierId
        );
        ledgerGateway.recordChange(ledgerEntry);
        
        log.info("Inventory updated successfully. Delta: {}", delta);
        return updated;
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

