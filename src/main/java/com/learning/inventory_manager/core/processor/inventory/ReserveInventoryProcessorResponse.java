package com.learning.inventory_manager.core.processor.inventory;

import com.learning.inventory_manager.core.domain.StockReservation;
import com.learning.inventory_manager.core.processor.ProcessorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveInventoryProcessorResponse implements ProcessorResponse {
    private List<StockReservation> reservations;
}
