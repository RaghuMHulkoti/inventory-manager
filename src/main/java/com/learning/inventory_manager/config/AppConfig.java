package com.learning.inventory_manager.config;

import com.learning.inventory_manager.core.processor.bulkinventory.BulkInventoryUpsertProcessor;
import com.learning.inventory_manager.core.processor.inventory.DeductInventoryProcessor;
import com.learning.inventory_manager.core.processor.inventory.GetAvailabilityProcessor;
import com.learning.inventory_manager.core.processor.inventory.GetInventoryProcessor;
import com.learning.inventory_manager.core.processor.warehouse.RegisterWarehouseProcessor;
import com.learning.inventory_manager.core.service.warehouse.RegisterWarehouseService;
import com.learning.inventory_manager.core.usecase.warehouse.RegisterWarehouseUsecase;
import com.learning.inventory_manager.core.processor.inventory.ReleaseInventoryProcessor;
import com.learning.inventory_manager.core.processor.inventory.ReserveInventoryProcessor;
import com.learning.inventory_manager.core.processor.inventory.SetStockProcessor;
import com.learning.inventory_manager.core.service.inventory.DeductInventoryService;
import com.learning.inventory_manager.core.service.inventory.GetAvailabilityService;
import com.learning.inventory_manager.core.service.inventory.GetInventoryService;
import com.learning.inventory_manager.core.service.inventory.InventoryService;
import com.learning.inventory_manager.core.service.inventory.ReleaseInventoryService;
import com.learning.inventory_manager.core.service.inventory.ReserveInventoryService;
import com.learning.inventory_manager.core.usecase.bulkinventory.BulkInventoryUpsertUsecase;
import com.learning.inventory_manager.core.usecase.inventory.DeductInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.GetAvailabilityUsecase;
import com.learning.inventory_manager.core.usecase.inventory.GetInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.ReleaseInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.ReserveInventoryUsecase;
import com.learning.inventory_manager.core.usecase.inventory.SetStockUsecase;
import com.learning.inventory_manager.gateway.InventoryEntityMapper;
import com.learning.inventory_manager.gateway.InventoryGateway;
import com.learning.inventory_manager.gateway.InventoryGatewayImpl;
import com.learning.inventory_manager.gateway.InventoryLedgerGateway;
import com.learning.inventory_manager.gateway.InventoryLedgerGatewayImpl;
import com.learning.inventory_manager.gateway.InventoryLedgerRepository;
import com.learning.inventory_manager.gateway.InventoryRepository;
import com.learning.inventory_manager.gateway.StockReservationGateway;
import com.learning.inventory_manager.gateway.StockReservationGatewayImpl;
import com.learning.inventory_manager.gateway.StockReservationRepository;
import com.learning.inventory_manager.gateway.WarehouseGateway;
import com.learning.inventory_manager.gateway.WarehouseGatewayImpl;
import com.learning.inventory_manager.gateway.WarehouseRepository;
import com.learning.inventory_manager.web.mapper.InventoryWebMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the core layers by hand so that processor, use case and service classes stay
 * free of Spring stereotype annotations.
 */
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
    public BulkInventoryUpsertProcessor bulkInventoryUpsertProcessor(BulkInventoryUpsertUsecase upsertUsecase) {
        return new BulkInventoryUpsertProcessor(upsertUsecase);
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
    public BulkInventoryUpsertUsecase bulkInventoryUpsertUsecase(WarehouseGateway warehouseGateway, InventoryService inventoryService) {
        return new BulkInventoryUpsertUsecase(warehouseGateway, inventoryService);
    }

    @Bean
    public InventoryGateway inventoryGateway(InventoryRepository repository, InventoryEntityMapper mapper) {
        return new InventoryGatewayImpl(repository, mapper);
    }

    @Bean
    public StockReservationGateway stockReservationGateway(StockReservationRepository repository, InventoryEntityMapper mapper) {
        return new StockReservationGatewayImpl(repository, mapper);
    }

    @Bean
    public InventoryLedgerGateway inventoryLedgerGateway(InventoryLedgerRepository repository, InventoryEntityMapper mapper) {
        return new InventoryLedgerGatewayImpl(repository, mapper);
    }

    @Bean
    public InventoryEntityMapper inventoryEntityMapper() {
        return new InventoryEntityMapper();
    }

    @Bean
    public RegisterWarehouseProcessor registerWarehouseProcessor(RegisterWarehouseUsecase usecase) {
        return new RegisterWarehouseProcessor(usecase);
    }

    @Bean
    public RegisterWarehouseUsecase registerWarehouseUsecase(RegisterWarehouseService service) {
        return new RegisterWarehouseUsecase(service);
    }

    @Bean
    public RegisterWarehouseService registerWarehouseService(WarehouseGateway warehouseGateway) {
        return new RegisterWarehouseService(warehouseGateway);
    }

    @Bean
    public WarehouseGateway warehouseGateway(WarehouseRepository repository) {
        return new WarehouseGatewayImpl(repository);
    }

    @Bean
    public InventoryWebMapper inventoryWebMapper() {
        return new InventoryWebMapper();
    }
}
