package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from the InventoryService after a create or update operation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryServiceResponse implements ServiceResponse {

    /** The resulting Inventory domain object after the operation. */
    private Inventory inventory;

    /** Whether the operation was a CREATE or UPDATE. */
    private Operation operation;

    /**
     * The quantity change applied:
     * - For CREATE: equals the initial quantityOnHand.
     * - For UPDATE: equals (newQuantity - oldQuantity), can be negative.
     */
    private Integer quantityDelta;

    public enum Operation {
        CREATE,
        UPDATE
    }
}
