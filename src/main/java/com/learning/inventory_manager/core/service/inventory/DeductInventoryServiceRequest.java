package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.DeductItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductInventoryServiceRequest implements com.learning.inventory_manager.core.service.ServiceRequest {
    private UUID orderId;
    private List<DeductItem> items;
}
