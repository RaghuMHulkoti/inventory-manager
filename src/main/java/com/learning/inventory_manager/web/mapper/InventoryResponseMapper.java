package com.learning.inventory_manager.web.mapper;

import com.learning.inventory_manager.core.domain.Inventory;
import com.learning.inventory_manager.core.processor.bulkinventory.BulkInventoryUpsertProcessorResponse;
import com.learning.inventory_manager.core.processor.inventory.*;
import com.learning.inventory_manager.web.response.*;

public final class InventoryResponseMapper {

    private InventoryResponseMapper() {}

    public static BulkInventoryUpsertResponse mapBulkUpsertResponse(BulkInventoryUpsertProcessorResponse processorResponse) {
        return BulkInventoryUpsertResponse.builder()
                .totalProcessed(processorResponse.getTotalProcessed())
                .successCount(processorResponse.getSuccessCount())
                .failureCount(processorResponse.getFailureCount())
                .results(processorResponse.getResults().stream()
                        .map(item -> InventoryUpsertItemResult.builder()
                                .supplierProductId(item.getSupplierProductId())
                                .warehouseId(item.getWarehouseId())
                                .status(item.getStatus())
                                .operation(item.getOperation())
                                .errorMessage(item.getErrorMessage())
                                .quantityDelta(item.getQuantityDelta())
                                .build())
                        .toList())
                .build();
    }

    public static GetInventoryResponse mapGetInventoryResponse(GetInventoryProcessorResponse response) {
        return GetInventoryResponse.builder()
                .inventories(response.getInventories().stream()
                        .map(InventoryResponseMapper::mapInventory)
                        .toList())
                .build();
    }

    private static InventoryResponse mapInventory(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .supplierProductId(inventory.getSupplierProductId())
                .warehouseId(inventory.getWarehouseId())
                .quantityOnHand(inventory.getQuantityOnHand())
                .quantityReserved(inventory.getQuantityReserved())
                .availableQuantity(inventory.getAvailableQuantity())
                .safetyStock(inventory.getSafetyStock())
                .reorderPoint(inventory.getReorderPoint())
                .belowSafetyStock(inventory.isBelowSafetyStock())
                .belowReorderPoint(inventory.isBelowReorderPoint())
                .version(inventory.getVersion())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .lastRestockedAt(inventory.getLastRestockedAt())
                .build();
    }

    public static ReserveBatchResponse mapReserveBatchResponse(ReserveInventoryProcessorResponse response) {
        var mappedResponse = response.getReservations().stream()
                .map(res -> ReserveResponse.builder()
                        .reservationId(res.getId())
                        .orderId(res.getOrderId())
                        .orderItemId(res.getOrderItemId())
                        .status(res.getStatus().name())
                        .expiresAt(res.getExpiresAt())
                        .build())
                .toList();
        return ReserveBatchResponse.builder().reservations(mappedResponse).build();
    }

    public static ReleaseResponse mapReleaseResponse(ReleaseInventoryProcessorResponse response) {
        return ReleaseResponse.builder()
                .reservationId(response.getReservationId())
                .success(response.isSuccess())
                .build();
    }

    public static DeductResponse mapDeductResponse(DeductInventoryProcessorResponse response) {
        return DeductResponse.builder()
                .deductedReservationIds(response.getDeductedReservationIds())
                .build();
    }

    public static AvailabilityResponse mapAvailabilityResponse(GetAvailabilityProcessorResponse response) {
        return AvailabilityResponse.builder()
                .productId(response.getProductId())
                .totalAvailable(response.getTotalAvailable())
                .breakdown(response.getBreakdown())
                .build();
    }
}
