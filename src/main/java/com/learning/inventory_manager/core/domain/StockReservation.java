package com.learning.inventory_manager.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain object representing a stock reservation held for an order item.
 *
 * Lifecycle:
 *   HELD → COMMITTED (on PAYMENT_SUCCESS / deduct)
 *   HELD → RELEASED  (on PAYMENT_FAILED / ORDER_CANCELLED / timeout)
 *   HELD → EXPIRED   (TTL cleanup job)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    private UUID id;
    private UUID orderId;
    private UUID orderItemId;
    private UUID inventoryId;
    private UUID supplierProductId;
    private UUID warehouseId;
    private Integer quantity;
    private ReservationStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime committedAt;
    private LocalDateTime releasedAt;
    private String idempotencyKey;
    private Long version;
    private LocalDateTime createdAt;

    // -------------------------------------------------------------------------
    // Lifecycle status
    // -------------------------------------------------------------------------
    public enum ReservationStatus {
        HELD, COMMITTED, RELEASED, EXPIRED
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------


}
