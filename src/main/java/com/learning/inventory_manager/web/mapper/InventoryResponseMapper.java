package com.learning.inventory_manager.web.mapper;

import com.learning.inventory_manager.core.processor.BulkInventory.BulkInventoryUpsertProcessorResponse;
import com.learning.inventory_manager.core.processor.inventory.*;
import com.learning.inventory_manager.web.response.*;

public class InventoryResponseMapper {

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
                .inventories(response.getInventories())
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
