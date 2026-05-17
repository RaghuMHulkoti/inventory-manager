package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.core.domain.Inventory;
import java.util.Optional;
import java.util.UUID;

public interface InventoryGateway {
    Optional<Inventory> findBySupplierProductIdAndWarehouseId(UUID supplierId, UUID warehouseId);
    Inventory save(Inventory inventory);
    Inventory create(Inventory inventory);
}

