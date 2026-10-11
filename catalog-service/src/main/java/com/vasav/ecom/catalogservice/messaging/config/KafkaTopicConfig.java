package com.vasav.ecom.catalogservice.messaging.config;

import com.vasav.ecom.catalogservice.messaging.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.time.Duration;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.partitions}")
    private int partitions;

    @Value("${app.kafka.replicas}")
    private int replicas;

//   catalog, review and order consumed dlq events are produced by our producer

    @Bean
    public KafkaAdmin.NewTopics catalogTopics() {
        return new KafkaAdmin.NewTopics(
                compacted(KafkaTopics.CATALOG_PRODUCTS),
                compacted(KafkaTopics.CATALOG_PRODUCT_RATINGS),
                TopicBuilder.name(KafkaTopics.ORDER_EVENTS_DLT)
                        .partitions(partitions)
                        .replicas(replicas)
//                        2 week dlq retention
                        .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(Duration.ofDays(14).toMillis()))
                        .build());
    }

    private NewTopic compacted(String name) {
        return TopicBuilder.name(name)
                .partitions(partitions)
                .replicas(replicas)
                .compact()
                .config(TopicConfig.MIN_COMPACTION_LAG_MS_CONFIG, String.valueOf(Duration.ofHours(1).toMillis()))
                .build();
    }
}
