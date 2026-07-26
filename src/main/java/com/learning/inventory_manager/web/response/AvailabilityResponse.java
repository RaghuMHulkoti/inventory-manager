package com.learning.inventory_manager.web.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class AvailabilityResponse {
    private UUID productId;
    private int totalAvailable;
    private Map<UUID, Map<UUID, Integer>> breakdown;
}
