package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.ReservationItem;
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
public class ReserveInventoryServiceRequest implements com.learning.inventory_manager.core.service.ServiceRequest {
    private UUID orderId;
    private List<ReservationItem> items;
    private long ttlSeconds;
    private String idempotencyPrefix;
}
