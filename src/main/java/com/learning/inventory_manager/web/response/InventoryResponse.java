package com.learning.inventory_manager.web.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API view of one inventory row. Keeps the domain {@code Inventory} out of the HTTP contract.
 */
@Data
@Builder
public class InventoryResponse {
    private UUID id;
    private UUID supplierProductId;
    private UUID warehouseId;
    private Integer quantityOnHand;
    private Integer quantityReserved;
    private Integer availableQuantity;
    private Integer safetyStock;
    private Integer reorderPoint;
    private boolean belowSafetyStock;
    private boolean belowReorderPoint;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRestockedAt;
}
