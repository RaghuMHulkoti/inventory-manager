package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.StockReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Stock Reservation entity.
 *
 * Responsibility:
 * - Provide JPA database access for reservations
 * - Enable queries for reservation lookups and reporting
 */
@Repository
public interface StockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {
    /**
     * Find all reservations for an inventory.
     * Used to calculate total reserved quantity.
     *
     * @param inventoryId the inventory ID
     * @return list of reservation records
     */
    List<StockReservationEntity> findByInventoryId(UUID inventoryId);

    /**
     * Find all reservations for an order.
     * Used to manage order-level reservation operations.
     *
     * @param orderId the order ID
     * @return list of reservation records for the order
     */
    List<StockReservationEntity> findByOrderId(UUID orderId);
}

