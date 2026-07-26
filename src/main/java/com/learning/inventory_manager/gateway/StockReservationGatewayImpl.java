package com.learning.inventory_manager.gateway;

import com.learning.inventory_manager.core.domain.StockReservation;
import java.util.Optional;
import java.util.UUID;

public class StockReservationGatewayImpl implements StockReservationGateway {

    private final StockReservationRepository repository;
    private final InventoryEntityMapper mapper;

    public StockReservationGatewayImpl(StockReservationRepository repository, InventoryEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<StockReservation> findById(UUID reservationId) {
        return repository.findById(reservationId).map(mapper::toDomain);
    }

    @Override
    public Optional<StockReservation> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public StockReservation save(StockReservation reservation) {
        var entity = mapper.toEntity(reservation);
        return mapper.toDomain(repository.save(entity));
    }

    @Override
    public boolean release(UUID reservationId) {
        return repository.markReleased(reservationId, java.time.LocalDateTime.now()) == 1;
    }

    @Override
    public boolean commit(UUID reservationId) {
        return repository.markCommitted(reservationId, java.time.LocalDateTime.now()) == 1;
    }
}
