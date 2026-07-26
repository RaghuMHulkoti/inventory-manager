package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request object for the InventoryService.
 *
 * Carries the fully-constructed {@link Inventory} domain object (created at the controller boundary)
 * along with the supplierId needed for ledger entries.
 *
 * The service uses the domain object's fields to look up existing inventory and decide
 * whether to create or update.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryServiceRequest implements ServiceRequest {

    /**
     * The Inventory domain object containing supplierProductId, warehouseId, and quantityOnHand.
     * Created by the controller via {@link Inventory#create} before entering the domain layers.
     */
    private Inventory inventory;

    /**
     * The supplier ID used as the reference identifier in ledger entries.
     */
    private UUID supplierId;
}
