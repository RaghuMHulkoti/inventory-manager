package com.learning.inventory_manager.web.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DeductItemRequest {
    @NotNull(message = "orderItemId is required")
    private UUID orderItemId;

    @NotNull(message = "reservationId is required")
    private UUID reservationId;
}
