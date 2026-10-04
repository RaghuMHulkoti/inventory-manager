package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseInventoryServiceResponse implements ServiceResponse {
    private UUID reservationId;
    private boolean success;
}
