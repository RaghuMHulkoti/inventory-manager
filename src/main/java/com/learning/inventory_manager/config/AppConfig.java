package com.learning.inventory_manager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.learning.inventory_manager.gateway.*;

import com.learning.inventory_manager.core.processor.inventory.GetInventoryProcessor;
import com.learning.inventory_manager.core.processor.inventory.ReleaseInventoryProcessor;
import com.learning.inventory_manager.core.processor.inventory.SetStockProcessor;
import com.learning.inventory_manager.core.processor.inventory.DeductInventoryProcessor;
import com.learning.inventory_manager.core.processor.inventory.ReserveInventoryProcessor;
import com.learning.inventory_manager.core.processor.inventory.GetAvailabilityProcessor;
import com.learning.inventory_manager.core.processor.BulkInventory.BulkInventoryUpsertProcessor;
import com.learning.inventory_manager.core.service.inventory.DeductInventoryService;
import com.learning.inventory_manager.core.service.inventory.ReleaseInventoryService;
import com.learning.inventory_manager.core.service.inventory.GetAvailabilityService;
import com.learning.inventory_manager.core.service.inventory.ReserveInventoryService;
import com.learning.inventory_manager.core.service.inventory.InventoryService;
import com.learning.inventory_manager.core.service.inventory.GetInventoryService;
import com.learning.inventory_manager.core.usecase.inventory.ReleaseInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.GetAvailabilityUsecase;
import com.learning.inventory_manager.core.usecase.inventory.DeductInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.SetStockUsecase;
import com.learning.inventory_manager.core.usecase.inventory.GetInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.ReserveInventoryUsecase;
import com.learning.inventory_manager.core.usecase.bulkInventory.BulkInventoryUpsertUseCase;
import com.learning.inventory_manager.gateway.InventoryGatewayImpl;
import com.learning.inventory_manager.gateway.StockReservationGatewayImpl;
import com.learning.inventory_manager.gateway.InventoryLedgerGatewayImpl;
import com.learning.inventory_manager.gateway.InventoryEntityMapper;
import com.learning.inventory_manager.gateway.WarehouseGatewayImpl;
import com.learning.inventory_manager.web.mapper.InventoryWebMapper;

@Configuration
public class AppConfig {

    @Bean
    public GetInventoryProcessor getInventoryProcessor(GetInventoryUsecase getInventoryUsecase) {
        return new GetInventoryProcessor(getInventoryUsecase);
    }

    @Bean
    public ReleaseInventoryProcessor releaseInventoryProcessor(ReleaseInventoryUsecase releaseInventoryUsecase) {
        return new ReleaseInventoryProcessor(releaseInventoryUsecase);
    }

    @Bean
    public SetStockProcessor setStockProcessor(SetStockUsecase setStockUsecase) {
        return new SetStockProcessor(setStockUsecase);
    }

    @Bean
    public DeductInventoryProcessor deductInventoryProcessor(DeductInventoryUsecase deductInventoryUsecase) {
        return new DeductInventoryProcessor(deductInventoryUsecase);
    }

    @Bean
    public ReserveInventoryProcessor reserveInventoryProcessor(ReserveInventoryUsecase reserveInventoryUsecase) {
        return new ReserveInventoryProcessor(reserveInventoryUsecase);
    }

    @Bean
    public GetAvailabilityProcessor getAvailabilityProcessor(GetAvailabilityUsecase getAvailabilityUsecase) {
        return new GetAvailabilityProcessor(getAvailabilityUsecase);
    }

    @Bean
    public BulkInventoryUpsertProcessor bulkInventoryUpsertProcessor(BulkInventoryUpsertUseCase upsertUseCase) {
        return new BulkInventoryUpsertProcessor(upsertUseCase);
    }

    @Bean
    public DeductInventoryService deductInventoryService(StockReservationGateway reservationGateway, InventoryGateway inventoryGateway, InventoryLedgerGateway ledgerGateway) {
        return new DeductInventoryService(reservationGateway, inventoryGateway, ledgerGateway);
    }

    @Bean
    public ReleaseInventoryService releaseInventoryService(StockReservationGateway reservationGateway, InventoryGateway inventoryGateway, InventoryLedgerGateway ledgerGateway) {
        return new ReleaseInventoryService(reservationGateway, inventoryGateway, ledgerGateway);
    }

    @Bean
    public GetAvailabilityService getAvailabilityService(InventoryGateway inventoryGateway) {
        return new GetAvailabilityService(inventoryGateway);
    }

    @Bean
    public ReserveInventoryService reserveInventoryService(InventoryGateway inventoryGateway, StockReservationGateway reservationGateway, InventoryLedgerGateway ledgerGateway) {
        return new ReserveInventoryService(inventoryGateway, reservationGateway, ledgerGateway);
    }

    @Bean
    public InventoryService inventoryService(InventoryGateway inventoryGateway, InventoryLedgerGateway ledgerGateway) {
        return new InventoryService(inventoryGateway, ledgerGateway);
    }

    @Bean
    public GetInventoryService getInventoryService(InventoryGateway inventoryGateway) {
        return new GetInventoryService(inventoryGateway);
    }

    @Bean
    public ReleaseInventoryUsecase releaseInventoryUsecase(ReleaseInventoryService releaseInventoryService) {
        return new ReleaseInventoryUsecase(releaseInventoryService);
    }

    @Bean
    public GetAvailabilityUsecase getAvailabilityUsecase(GetAvailabilityService getAvailabilityService) {
        return new GetAvailabilityUsecase(getAvailabilityService);
    }

    @Bean
    public DeductInventoryUsecase deductInventoryUsecase(DeductInventoryService deductInventoryService) {
        return new DeductInventoryUsecase(deductInventoryService);
    }

    @Bean
    public SetStockUsecase setStockUsecase(InventoryService inventoryService) {
        return new SetStockUsecase(inventoryService);
    }

    @Bean
    public GetInventoryUsecase getInventoryUsecase(GetInventoryService getInventoryService) {
        return new GetInventoryUsecase(getInventoryService);
    }

    @Bean
    public ReserveInventoryUsecase reserveInventoryUsecase(ReserveInventoryService reserveInventoryService) {
        return new ReserveInventoryUsecase(reserveInventoryService);
    }

    @Bean
    public BulkInventoryUpsertUseCase bulkInventoryUpsertUseCase(WarehouseGateway warehouseGateway, InventoryService inventoryService) {
        return new BulkInventoryUpsertUseCase(warehouseGateway, inventoryService);
    }

    @Bean
    public InventoryGatewayImpl inventoryGatewayImpl(InventoryRepository repository, InventoryEntityMapper mapper) {
        return new InventoryGatewayImpl(repository, mapper);
    }

    @Bean
    public StockReservationGatewayImpl stockReservationGatewayImpl(StockReservationRepository repository, InventoryEntityMapper mapper) {
        return new StockReservationGatewayImpl(repository, mapper);
    }

    @Bean
    public InventoryLedgerGatewayImpl inventoryLedgerGatewayImpl(InventoryLedgerRepository repository, InventoryEntityMapper mapper) {
        return new InventoryLedgerGatewayImpl(repository, mapper);
    }

    @Bean
    public InventoryEntityMapper inventoryEntityMapper() {
        return new InventoryEntityMapper();
    }

    @Bean
    public WarehouseGatewayImpl warehouseGatewayImpl() {
        return new WarehouseGatewayImpl();
    }

    @Bean
    public InventoryWebMapper inventoryWebMapper() {
        return new InventoryWebMapper();
    }

}
