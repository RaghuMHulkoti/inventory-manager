package com.learning.inventory_manager.gateway.messaging;

import com.learning.inventory_manager.core.usecase.inventory.HandleOrderCreatedUsecase;
import org.springframework.kafka.annotation.KafkaListener;

/** Feeds Order Service's events into stock holding. Failures propagate so the error handler retries, then dead-letters. */
public class OrderEventListener {

    private final HandleOrderCreatedUsecase usecase;

    public OrderEventListener(HandleOrderCreatedUsecase usecase) {
        this.usecase = usecase;
    }

    @KafkaListener(topics = "${inventory.events.order-topic:domain.order.events}", groupId = "${spring.kafka.consumer.group-id:inventory-manager}")
    public void onOrderEvent(String payload) {
        usecase.execute(new HandleOrderCreatedUsecase.Request(payload));
    }
}
