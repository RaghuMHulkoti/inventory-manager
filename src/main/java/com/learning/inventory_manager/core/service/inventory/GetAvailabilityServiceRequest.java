package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.service.ServiceRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAvailabilityServiceRequest implements ServiceRequest {
    private UUID productId;
    // NOTE: TDD says to map productId -> multiple supplierProductIds via Supplier Service.
    // Since we are only implementing Inventory Service, we will accept a list of supplierProductIds directly.
    private java.util.List<UUID> supplierProductIds;
}
