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
import com.learning.inventory_manager.core.usecase.inventory.HandleOrderCreatedUsecase;
import com.learning.inventory_manager.gateway.InventoryOutboxGateway;
import com.learning.inventory_manager.gateway.InventoryOutboxGatewayImpl;
import com.learning.inventory_manager.gateway.InventoryOutboxPoller;
import com.learning.inventory_manager.gateway.InventoryOutboxRepository;
import com.learning.inventory_manager.gateway.messaging.EventPublisher;
import com.learning.inventory_manager.gateway.messaging.KafkaEventPublisher;
import com.learning.inventory_manager.gateway.messaging.LoggingEventPublisher;
import com.learning.inventory_manager.gateway.messaging.OrderEventListener;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Wires the core layers by hand so that processor, use case and service classes stay
 * free of Spring stereotype annotations.
 */
@Configuration
@EnableScheduling
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
    public ReserveInventoryService reserveInventoryService(InventoryGateway inventoryGateway, StockReservationGateway reservationGateway, InventoryLedgerGateway ledgerGateway, InventoryOutboxGateway outboxGateway, JsonMapper json) {
        return new ReserveInventoryService(inventoryGateway, reservationGateway, ledgerGateway, outboxGateway, json);
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

    // =========================================================================
    // Events
    // =========================================================================

    @Bean
    public InventoryOutboxGateway inventoryOutboxGateway(InventoryOutboxRepository repository) {
        return new InventoryOutboxGatewayImpl(repository);
    }

    @Bean
    public HandleOrderCreatedUsecase handleOrderCreatedUsecase(ReserveInventoryUsecase reserve,
                                                               InventoryOutboxGateway outbox, JsonMapper json, Clock clock) {
        return new HandleOrderCreatedUsecase(reserve, outbox, json, clock);
    }

    /** {@code inventory.events.publisher=kafka} sends to the broker; the default only logs. */
    @Bean
    @ConditionalOnProperty(name = "inventory.events.publisher", havingValue = "kafka")
    public EventPublisher kafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                              @Value("${inventory.events.topic:domain.inventory.events}") String topic) {
        return new KafkaEventPublisher(kafkaTemplate, topic);
    }

    @Bean
    @ConditionalOnProperty(name = "inventory.events.publisher", havingValue = "log", matchIfMissing = true)
    public EventPublisher loggingEventPublisher() {
        return new LoggingEventPublisher();
    }

    @Bean
    @ConditionalOnProperty(name = "inventory.jobs.enabled", havingValue = "true", matchIfMissing = true)
    public InventoryOutboxPoller inventoryOutboxPoller(InventoryOutboxRepository outbox, EventPublisher publisher,
                                                       PlatformTransactionManager transactionManager, Clock clock,
                                                       @Value("${inventory.outbox.batch-size:100}") int batchSize) {
        return new InventoryOutboxPoller(outbox, publisher, new TransactionTemplate(transactionManager), clock, batchSize);
    }

    /** Off by default, so the service runs without a broker (the REST reserve endpoint keeps working). */
    @Bean
    @ConditionalOnProperty(name = "inventory.events.consumer-enabled", havingValue = "true")
    public OrderEventListener orderEventListener(HandleOrderCreatedUsecase usecase) {
        return new OrderEventListener(usecase);
    }

    /**
     * Creates the topics this service owns at startup, with explicit small partition counts so the cluster quota holds
     * (free tier: 10 topics, 64 partitions in total). Replication is left to the broker default.
     * Owns {@code domain.inventory.events} plus the dead-letter topic of the order events it consumes.
     */
    @Bean
    @ConditionalOnExpression("('${inventory.events.publisher:log}' == 'kafka' or '${inventory.events.consumer-enabled:false}' == 'true')"
            + " and '${inventory.events.create-topics:true}' == 'true'")
    public KafkaAdmin.NewTopics inventoryEventTopics(
            @Value("${inventory.events.topic:domain.inventory.events}") String inventoryTopic,
            @Value("${inventory.events.order-topic:domain.order.events}") String orderTopic,
            @Value("${inventory.events.partitions:3}") int partitions) {
        return new KafkaAdmin.NewTopics(ownedTopics(inventoryTopic, orderTopic, partitions).toArray(NewTopic[]::new));
    }

    static List<NewTopic> ownedTopics(String inventoryTopic, String orderTopic, int partitions) {
        return List.of(
                TopicBuilder.name(inventoryTopic).partitions(partitions).build(),
                TopicBuilder.name(orderTopic + ".dlq").partitions(1).build());
    }

    /** Retries 50ms, 250ms, 1.25s (TDD §13.7), then parks the event on {@code <topic>.dlq} and moves on. */
    @Bean
    @ConditionalOnProperty(name = "inventory.events.consumer-enabled", havingValue = "true")
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        var backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(50);
        backOff.setMultiplier(5);
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, e) -> new TopicPartition(record.topic() + ".dlq", -1));
        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
