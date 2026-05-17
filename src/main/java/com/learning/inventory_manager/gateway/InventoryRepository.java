package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.gateway.entity.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Inventory entity.
 *
 * Responsibility:
 * - Provide JPA database access for inventory records
 * - Enable efficient lookups by composite key (supplier_product_id, warehouse_id)
 */
@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    /**
     * Find inventory by supplier product and warehouse combination.
     * This is the primary lookup for upsert operations.
     *
     * @param supplierProductId the supplier product ID
     * @param warehouseId the warehouse ID
     * @return Optional containing inventory if found
     */
    Optional<InventoryEntity> findBySupplierProductIdAndWarehouseId(
        UUID supplierProductId,
        UUID warehouseId
    );

    /**
     * Check if inventory exists for the given product-warehouse combination.
     *
     * @param supplierProductId the supplier product ID
     * @param warehouseId the warehouse ID
     * @return true if inventory record exists
     */
    boolean existsBySupplierProductIdAndWarehouseId(UUID supplierProductId, UUID warehouseId);
}

