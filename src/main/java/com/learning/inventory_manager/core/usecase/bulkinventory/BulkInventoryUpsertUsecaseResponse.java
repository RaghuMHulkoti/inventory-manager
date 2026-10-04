package com.learning.inventory_manager.core.usecase.bulkinventory;

import com.learning.inventory_manager.core.usecase.UsecaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response from {@link BulkInventoryUpsertUsecase}.
 * Contains a result for each inventory item that was processed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInventoryUpsertUsecaseResponse implements UsecaseResponse {

    private List<ItemResult> results;

    // -------------------------------------------------------------------------
    // Enums
    // -------------------------------------------------------------------------

    public enum Status {
        SUCCESS,
        FAILED
    }

    public enum Operation {
        CREATE,
        UPDATE
    }

    // -------------------------------------------------------------------------
    // Inner result class
    // -------------------------------------------------------------------------

    /**
     * Result for a single inventory item in the bulk operation.
     * Use the static factory methods to construct instances.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResult {

        private UUID supplierProductId;
        private UUID warehouseId;
        private Status status;
        private Operation operation;
        private String errorMessage;
        private Integer quantityDelta;

        public static ItemResult successCreate(
                UUID supplierProductId,
                UUID warehouseId,
                Integer quantityDelta
        ) {
            return ItemResult.builder()
                    .supplierProductId(supplierProductId)
                    .warehouseId(warehouseId)
                    .status(Status.SUCCESS)
                    .operation(Operation.CREATE)
                    .quantityDelta(quantityDelta)
                    .build();
        }

        public static ItemResult successUpdate(
                UUID supplierProductId,
                UUID warehouseId,
                Integer quantityDelta
        ) {
            return ItemResult.builder()
                    .supplierProductId(supplierProductId)
                    .warehouseId(warehouseId)
                    .status(Status.SUCCESS)
                    .operation(Operation.UPDATE)
                    .quantityDelta(quantityDelta)
                    .build();
        }

        public static ItemResult failure(
                UUID supplierProductId,
                UUID warehouseId,
                String errorMessage
        ) {
            return ItemResult.builder()
                    .supplierProductId(supplierProductId)
                    .warehouseId(warehouseId)
                    .status(Status.FAILED)
                    .errorMessage(errorMessage)
                    .build();
        }
    }
}
