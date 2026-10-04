package com.learning.inventory_manager.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.util.UUID;

@Data
public class ReleaseRequest {
    private UUID reservationId;
    private String idempotencyKey;
    private String reason;

    @JsonIgnore
    @AssertTrue(message = "reservationId or idempotencyKey is required")
    public boolean isReservationReferencePresent() {
        return reservationId != null || (idempotencyKey != null && !idempotencyKey.isBlank());
    }
}
