package com.learning.inventory_manager.web.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ReleaseResponse {
    private UUID reservationId;
    private boolean success;
}
