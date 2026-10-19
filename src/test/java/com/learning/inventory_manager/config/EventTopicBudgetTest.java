package com.learning.inventory_manager.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Keeps this service inside the free cluster quota (10 topics, 64 partitions across all services). */
class EventTopicBudgetTest {

    @Test
    void createsOnlyTheTopicsItOwnsWithSmallPartitionCounts() {
        var created = AppConfig.ownedTopics("domain.inventory.events", "domain.order.events", 3);
        assertThat(created).extracting(t -> t.name()).containsExactlyInAnyOrder("domain.inventory.events", "domain.order.events.dlq");
        assertThat(created.stream().mapToInt(t -> t.numPartitions()).sum()).isEqualTo(4);
    }
}
