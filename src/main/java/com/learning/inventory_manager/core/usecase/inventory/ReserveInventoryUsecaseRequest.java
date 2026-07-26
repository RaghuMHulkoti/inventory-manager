package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.domain.ReservationItem;
import com.learning.inventory_manager.core.usecase.UsecaseRequest;
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
public class ReserveInventoryUsecaseRequest implements UsecaseRequest {
    private UUID orderId;
    private List<ReservationItem> items;
    private long ttlSeconds;
    private String idempotencyPrefix;
}
