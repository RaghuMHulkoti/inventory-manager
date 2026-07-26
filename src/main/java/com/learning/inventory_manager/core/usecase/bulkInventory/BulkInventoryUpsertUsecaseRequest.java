package com.learning.inventory_manager.core.usecase.bulkInventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request for {@link BulkInventoryUpsertUseCase}.
 *
 * Carries a list of fully-constructed {@link Inventory} domain objects (converted at the
 * controller boundary) together with the supplierId for attribution and ledger entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInventoryUpsertUsecaseRequest implements UsecaseRequest {

    /** The supplier initiating the inventory push. */
    private UUID supplierId;

    /**
     * Domain inventory objects — one per (supplierProductId, warehouseId) pair.
     * Each already has the desired quantityOnHand set by the caller.
     */
    private List<Inventory> items;
}
