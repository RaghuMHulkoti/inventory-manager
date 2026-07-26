package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.usecase.UsecaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInventoryUsecaseRequest implements UsecaseRequest {
    private UUID reservationId;
    private String idempotencyKey;
    private String reason;
}
