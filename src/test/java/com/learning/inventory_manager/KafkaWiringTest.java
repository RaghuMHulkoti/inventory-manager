package com.learning.inventory_manager;

import com.learning.inventory_manager.gateway.messaging.EventPublisher;
import com.learning.inventory_manager.gateway.messaging.KafkaEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Kafka mode switched on, listener container not started: proves the producer, consumer and dead-letter wiring assemble. */
@SpringBootTest(properties = {
        "inventory.events.publisher=kafka",
        "inventory.events.consumer-enabled=true",
        "inventory.events.create-topics=false",
        "spring.kafka.listener.auto-startup=false"
})
@ActiveProfiles("test")
class KafkaWiringTest {

    @Autowired
    EventPublisher publisher;
    @Autowired
    CommonErrorHandler errorHandler;
    @Autowired
    KafkaListenerEndpointRegistry registry;

    @Test
    void kafkaModeWiresPublisherOrderEventListenerAndDeadLetterHandler() {
        assertThat(publisher).isInstanceOf(KafkaEventPublisher.class);
        assertThat(errorHandler).isInstanceOf(DefaultErrorHandler.class);
        assertThat(registry.getListenerContainers()).hasSize(1);
        assertThat(registry.getListenerContainers().iterator().next().getContainerProperties().getTopics())
                .containsExactly("domain.order.events");
    }
}
