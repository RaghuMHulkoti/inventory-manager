package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.core.domain.Inventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gateway interface for inventory persistence operations.
 * The domain/service layers depend only on this interface, never on JPA directly.
 */
public interface InventoryGateway {

    Optional<Inventory> findBySupplierProductIdAndWarehouseId(UUID supplierProductId, UUID warehouseId);

    /** All inventory rows for a supplier product (across warehouses). */
    List<Inventory> findBySupplierProductId(UUID supplierProductId);

    /** Save (update) an existing inventory row via standard JPA dirty-check. */
    Inventory save(Inventory inventory);

    /** Create a new inventory row. */
    Inventory create(Inventory inventory);

    /**
     * Atomically attempt to reserve {@code qty} units.
     * Uses a conditional UPDATE — returns {@code true} if rows_affected = 1,
     * {@code false} if insufficient stock.
     */
    boolean tryReserveStock(UUID inventoryId, int qty);

    /**
     * Decrement quantity_reserved by {@code qty} (reservation release).
     */
    void releaseStock(UUID inventoryId, int qty);

    /**
     * Atomically deduct both quantity_on_hand and quantity_reserved by {@code qty}
     * (called on payment success).
     */
    void deductStock(UUID inventoryId, int qty);

    Optional<Inventory> findById(UUID inventoryId);
}
