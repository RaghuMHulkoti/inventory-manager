package com.learning.inventory_manager.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stock reservation entity — holds stock during the payment window.
 * Lifecycle status: HELD → COMMITTED | RELEASED | EXPIRED
 */
@Entity
@Table(name = "stock_reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservationEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "inventory_id", nullable = false)
    private UUID inventoryId;

    @Column(name = "supplier_product_id", nullable = false)
    private UUID supplierProductId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "committed_at")
    private LocalDateTime committedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    /** Stable idempotency key — typically "{orderId}:{orderItemId}". */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 80)
    private String idempotencyKey;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
