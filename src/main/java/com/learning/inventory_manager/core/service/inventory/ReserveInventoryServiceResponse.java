package com.learning.inventory_manager.core.service.inventory;

import com.learning.inventory_manager.core.service.ServiceResponse;
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
public class ReserveInventoryServiceResponse implements ServiceResponse {
    private List<StockReservation> reservations;
}
