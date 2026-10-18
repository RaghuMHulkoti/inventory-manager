package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Warehouse entity.
 *
 * Responsibility:
 * - Provide JPA database access for warehouses
 * - Enable custom queries for warehouse lookups
 */
@Repository
public interface WarehouseRepository extends JpaRepository<WarehouseEntity, UUID> {
    /**
     * Find active warehouse by ID.
     * Used to validate warehouse existence and status before inventory operations.
     *
     * @param id warehouse ID
     * @return Optional containing warehouse if found and active
     */
    Optional<WarehouseEntity> findByIdAndIsActiveTrue(UUID id);

    /**
     * Check if warehouse exists and is active.
     * Lightweight check for validation.
     *
     * @param id warehouse ID
     * @return true if warehouse exists and is active
     */
    boolean existsByIdAndIsActiveTrue(UUID id);

    /**
     * Check that an active warehouse with this id is owned by the given supplier.
     *
     * @param id         warehouse ID
     * @param supplierId owning supplier ID
     * @return true if the warehouse exists, is active and belongs to the supplier
     */
    boolean existsByIdAndSupplierIdAndIsActiveTrue(UUID id, UUID supplierId);
}

