package com.learning.inventory_manager.web.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class WarehouseResponse {
    private UUID warehouseId;
    private UUID supplierId;
    private String code;
    private String name;
    private String city;
    private String state;
    private String country;
    private boolean active;
}
