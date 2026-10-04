package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.service.ServiceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductInventoryServiceResponse implements ServiceResponse {
    private List<UUID> deductedReservationIds;
}
