package com.learning.inventory_manager.domain.gateway;

import com.learning.inventory_manager.domain.model.Inventory;
import java.util.Optional;
import java.util.UUID;

public interface InventoryGateway {
    Optional<Inventory> findBySupplierProductIdAndWarehouseId(UUID supplierId, UUID warehouseId);
    Inventory save(Inventory inventory);
    Inventory create(Inventory inventory);
}

