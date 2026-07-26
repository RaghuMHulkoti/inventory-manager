package com.learning.inventory_manager.web.model;

import lombok.Data;

import java.util.UUID;

@Data
public class ReleaseRequest {
    private UUID reservationId;
    private String idempotencyKey;
    private String reason;
}
