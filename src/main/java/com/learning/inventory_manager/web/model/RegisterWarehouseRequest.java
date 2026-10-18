package com.learning.inventory_manager.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/** {@code warehouseId} is the id Supplier Service gave the warehouse; Inventory reuses it instead of minting its own. */
@Data
public class RegisterWarehouseRequest {

    @NotNull(message = "warehouseId is required")
    private UUID warehouseId;

    @NotNull(message = "supplierId is required")
    private UUID supplierId;

    @NotBlank(message = "code is required")
    @Size(max = 50, message = "code must be at most 50 characters")
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    private String name;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    /** Defaults to true; false takes the warehouse out of use without deleting its stock. */
    private Boolean active;
}
