package com.learning.inventory_manager.core.usecase.bulkInventory;

import com.learning.inventory_manager.core.usecase.UsecaseResponse;

import java.util.List;
import java.util.UUID;

public record BulkInventoryUpsertUsecaseResponse(
        List<ItemResult> results
) implements UsecaseResponse {
    public BulkInventoryUpsertUsecaseResponse {
        results = List.copyOf(results);
    }

    public record ItemResult(
            UUID supplierProductId,
            UUID warehouseId,
            Status status,
            Operation operation,
            String errorMessage,
            Integer quantityDelta
    ) {
        public static ItemResult successCreate(
                UUID supplierProductId,
                UUID warehouseId,
                Integer quantityDelta
        ) {
            return new ItemResult(
                    supplierProductId,
                    warehouseId,
                    Status.SUCCESS,
                    Operation.CREATE,
                    null,
                    quantityDelta
            );
        }

        public static ItemResult successUpdate(
                UUID supplierProductId,
                UUID warehouseId,
                Integer quantityDelta
        ) {
            return new ItemResult(
                    supplierProductId,
                    warehouseId,
                    Status.SUCCESS,
                    Operation.UPDATE,
                    null,
                    quantityDelta
            );
        }

        public static ItemResult failure(
                UUID supplierProductId,
                UUID warehouseId,
                String errorMessage
        ) {
            return new ItemResult(
                    supplierProductId,
                    warehouseId,
                    Status.FAILED,
                    null,
                    errorMessage,
                    null
            );
        }
    }

    public enum Status {
        SUCCESS,
        FAILED
    }

    public enum Operation {
        CREATE,
        UPDATE
    }
}
