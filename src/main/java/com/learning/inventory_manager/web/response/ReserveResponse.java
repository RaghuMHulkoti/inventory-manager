package com.learning.inventory_manager.web.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReserveResponse {
    private UUID reservationId;
    private UUID orderId;
    private UUID orderItemId;
    private String status;
    private LocalDateTime expiresAt;
}
