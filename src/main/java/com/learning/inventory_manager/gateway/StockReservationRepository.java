package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {

    List<StockReservationEntity> findByInventoryId(UUID inventoryId);

    List<StockReservationEntity> findByOrderId(UUID orderId);

    Optional<StockReservationEntity> findByIdempotencyKey(String idempotencyKey);

    /** All HELD reservations that have passed their expiry — used by the cleanup job. */
    @Query("SELECT r FROM StockReservationEntity r WHERE r.status = 'HELD' AND r.expiresAt < :now")
    List<StockReservationEntity> findExpiredHeldReservations(@Param("now") LocalDateTime now);

    /** Transition a reservation to RELEASED. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StockReservationEntity r SET r.status = 'RELEASED', r.releasedAt = :releasedAt WHERE r.id = :id AND r.status = 'HELD'")
    int markReleased(@Param("id") UUID id, @Param("releasedAt") LocalDateTime releasedAt);

    /** Transition a reservation to COMMITTED. */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE StockReservationEntity r SET r.status = 'COMMITTED', r.committedAt = :committedAt WHERE r.id = :id AND r.status = 'HELD'")
    int markCommitted(@Param("id") UUID id, @Param("committedAt") LocalDateTime committedAt);
}
