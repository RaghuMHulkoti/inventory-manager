package com.learning.inventory_manager.core.service.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAvailabilityServiceResponse implements com.learning.inventory_manager.core.service.ServiceResponse {
    private UUID productId;
    private int totalAvailable;
    // Key: supplierProductId, Value: Map of warehouseId -> availableQuantity
    private Map<UUID, Map<UUID, Integer>> breakdown;
}
