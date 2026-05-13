package com.learning.inventory_manager.infrastructure.mapper;

import com.learning.inventory_manager.domain.model.Inventory;
import com.learning.inventory_manager.infrastructure.persistence.entity.InventoryEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between InventoryEntity (JPA) and Inventory (domain model).
 * Provides bidirectional mapping to isolate domain logic from persistence details.
 */
@Component
public class InventoryMapper {
    
    public Inventory toDomain(InventoryEntity entity) {
        if (entity == null) return null;
        
        return new Inventory(
            entity.getId(),
            entity.getSupplierProductId(),
            entity.getWarehouseId(),
            entity.getQuantityOnHand(),
            entity.getQuantityReserved(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public InventoryEntity toEntity(Inventory domain) {
        if (domain == null) return null;
        
        return InventoryEntity.builder()
            .id(domain.getId())
            .supplierProductId(domain.getSupplierProductId())
            .warehouseId(domain.getWarehouseId())
            .quantityOnHand(domain.getQuantityOnHand())
            .quantityReserved(domain.getQuantityReserved())
            .version(domain.getVersion())
            .createdAt(domain.getCreatedAt())
            .updatedAt(domain.getUpdatedAt())
            .build();
    }
}

