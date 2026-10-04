package com.learning.inventory_manager.web.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReserveRequest {
    @NotNull(message = "orderId is required")
    private UUID orderId;

    @NotEmpty(message = "items list cannot be empty")
    @Valid
    private List<ReserveItemRequest> items;

    @Positive(message = "ttlSeconds must be greater than 0")
    private long ttlSeconds = 600;
}
