package com.learning.inventory_manager.core.service.warehouse;

import com.learning.inventory_manager.core.service.Service;
import com.learning.inventory_manager.exception.InventoryValidationException;
import com.learning.inventory_manager.gateway.WarehouseGateway;

/**
 * Creates the warehouse or refreshes its details; registering the same warehouse twice is harmless. A warehouse
 * can never move to another supplier, because stock and reservations are owned through it.
 */
public class RegisterWarehouseService implements Service<RegisterWarehouseServiceRequest, RegisterWarehouseServiceResponse> {

    private final WarehouseGateway warehouseGateway;

    public RegisterWarehouseService(WarehouseGateway warehouseGateway) {
        this.warehouseGateway = warehouseGateway;
    }

    @Override
    public RegisterWarehouseServiceResponse execute(RegisterWarehouseServiceRequest request) {
        var warehouse = request.getWarehouse();
        var existing = warehouseGateway.findById(warehouse.id());
        if (existing.isPresent() && !existing.get().supplierId().equals(warehouse.supplierId())) {
            throw new InventoryValidationException("Warehouse " + warehouse.id() + " already belongs to another supplier");
        }
        return RegisterWarehouseServiceResponse.builder()
                .warehouse(warehouseGateway.save(warehouse))
                .created(existing.isEmpty())
                .build();
    }
}
