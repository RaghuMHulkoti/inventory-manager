package com.learning.inventory_manager.gateway.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory outbox entity — supports the Transactional Outbox pattern.
 * A CDC poller (future work) reads PENDING rows and publishes events to Kafka,
 * then marks them SENT.
 */
@Entity
@Table(name = "inventory_outbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The id of the aggregate that produced this event (e.g., inventoryId, reservationId). */
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    /** Event type key, e.g. INVENTORY_RESERVED, INVENTORY_DEDUCTED. */
    @Column(name = "event_type", nullable = false, length = 60)
    private String eventType;

    /** JSON-serialised event payload. */
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    /** PENDING → SENT | FAILED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "PENDING";
    }
}
