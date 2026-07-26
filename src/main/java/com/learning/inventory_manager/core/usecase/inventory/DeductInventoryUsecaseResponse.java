package com.learning.inventory_manager.core.usecase.inventory;

import com.learning.inventory_manager.core.usecase.UsecaseResponse;
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
public class DeductInventoryUsecaseResponse implements UsecaseResponse {
    private List<UUID> deductedReservationIds;
}
