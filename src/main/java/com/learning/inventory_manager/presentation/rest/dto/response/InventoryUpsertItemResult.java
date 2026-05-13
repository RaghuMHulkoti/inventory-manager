package com.learning.inventory_manager.presentation.rest.dto.response;

import java.util.UUID;

/**
 * Individual inventory upsert result DTO.
 * Represents the result of processing a single inventory item.
 * Contains status, operation type, and error details if applicable.
 *
 * @param supplierProductId the supplier product ID
 * @param warehouseId the warehouse ID
 * @param status the result status (SUCCESS or FAILED)
 * @param operation the operation performed (CREATE or UPDATE)
 * @param errorMessage error message if status is FAILED
 * @param quantityDelta the change in quantity (negative for decrease, positive for increase)
 */
public record InventoryUpsertItemResult(
    UUID supplierProductId,
    UUID warehouseId,
    String status,
    String operation,
    String errorMessage,
    Integer quantityDelta
) {
    /**
     * Creates a successful result for a CREATE operation.
     */
    public static InventoryUpsertItemResult successCreate(
        UUID supplierProductId,
        UUID warehouseId,
        Integer quantityOnHand
    ) {
        return new InventoryUpsertItemResult(
            supplierProductId,
            warehouseId,
            "SUCCESS",
            "CREATE",
            null,
            quantityOnHand
        );
    }

    /**
     * Creates a successful result for an UPDATE operation.
     */
    public static InventoryUpsertItemResult successUpdate(
        UUID supplierProductId,
        UUID warehouseId,
        Integer quantityDelta
    ) {
        return new InventoryUpsertItemResult(
            supplierProductId,
            warehouseId,
            "SUCCESS",
            "UPDATE",
            null,
            quantityDelta
        );
    }

    /**
     * Creates a failed result with error message.
     */
    public static InventoryUpsertItemResult failure(
        UUID supplierProductId,
        UUID warehouseId,
        String errorMessage
    ) {
        return new InventoryUpsertItemResult(
            supplierProductId,
            warehouseId,
            "FAILED",
            null,
            errorMessage,
            null
        );
    }
}

