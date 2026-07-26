package com.learning.inventory_manager.web.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DeductRequest {
    @NotNull(message = "orderId is required")
    private UUID orderId;

    @NotEmpty(message = "items list cannot be empty")
    @Valid
    private List<DeductItemRequest> items;
}
