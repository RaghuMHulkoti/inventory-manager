package com.learning.inventory_manager.core.service.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetInventoryServiceRequest implements com.learning.inventory_manager.core.service.ServiceRequest {
    private UUID supplierProductId;
}
