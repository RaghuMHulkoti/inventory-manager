package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the inventory table.
 *
 * The custom {@code @Modifying} queries implement the TDD's atomic conditional UPDATE pattern
 * (§10.3) for reserve, release, and deduct operations — bypassing Hibernate's 1st-level cache
 * to issue direct SQL.
 */
@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {

    Optional<InventoryEntity> findBySupplierProductIdAndWarehouseId(UUID supplierProductId, UUID warehouseId);

    boolean existsBySupplierProductIdAndWarehouseId(UUID supplierProductId, UUID warehouseId);

    /** All inventory rows for a supplier product (across all its warehouses). */
    List<InventoryEntity> findBySupplierProductId(UUID supplierProductId);

    // -------------------------------------------------------------------------
    // Atomic conditional UPDATE queries (TDD §10.3)
    // -------------------------------------------------------------------------

    /**
     * Atomically increment quantity_reserved IF sufficient stock is available.
     * Returns 1 if the update succeeded, 0 if insufficient stock (available < qty).
     *
     * SQL equivalent:
     *   UPDATE inventory
     *   SET quantity_reserved = quantity_reserved + :qty
     *   WHERE id = :id AND (quantity_on_hand - quantity_reserved) >= :qty
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE InventoryEntity i " +
           "SET i.quantityReserved = i.quantityReserved + :qty " +
           "WHERE i.id = :id AND (i.quantityOnHand - i.quantityReserved) >= :qty")
    int tryReserveStock(@Param("id") UUID id, @Param("qty") int qty);

    /**
     * Decrement quantity_reserved by qty (called on reservation release).
     * Returns 1 if updated, 0 if not found.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE InventoryEntity i " +
           "SET i.quantityReserved = i.quantityReserved - :qty " +
           "WHERE i.id = :id")
    int releaseStock(@Param("id") UUID id, @Param("qty") int qty);

    /**
     * Atomically deduct both on-hand and reserved (called on payment success / deduct).
     * Returns 1 if updated, 0 if not found.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE InventoryEntity i " +
           "SET i.quantityOnHand = i.quantityOnHand - :qty, " +
           "    i.quantityReserved = i.quantityReserved - :qty " +
           "WHERE i.id = :id")
    int deductStock(@Param("id") UUID id, @Param("qty") int qty);
}
