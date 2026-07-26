package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.domain.StockReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveInventoryServiceResponse implements com.learning.inventory_manager.core.service.ServiceResponse {
    private List<StockReservation> reservations;
}
