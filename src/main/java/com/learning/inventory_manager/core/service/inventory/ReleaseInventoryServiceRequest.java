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
public class ReleaseInventoryServiceRequest implements ServiceRequest {
    private UUID reservationId;
    private String idempotencyKey;
    private String reason;
}
