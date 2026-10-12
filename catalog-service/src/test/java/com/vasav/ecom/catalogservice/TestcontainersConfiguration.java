package com.vasav.ecom.catalogservice;

import com.vasav.ecom.catalogservice.events.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Throwaway Postgres + Kafka for every test context. @ServiceConnection replaces the datasource,
 * Flyway and Kafka connection settings, so nothing reaches catalog_db or Confluent Cloud.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:17.10-alpine"));
    }

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:4.3.1"));
    }

    // several partitions so "same key -> same partition" is actually meaningful
    @Bean
    NewTopic catalogProductsTopic() {
        return TopicBuilder.name(KafkaTopics.CATALOG_PRODUCTS).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic catalogProductRatingsTopic() {
        return TopicBuilder.name(KafkaTopics.CATALOG_PRODUCT_RATINGS).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic orderEventsTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    NewTopic orderEventsDltTopic() {
        return TopicBuilder.name(KafkaTopics.ORDER_EVENTS_DLT).partitions(1).replicas(1).build();
    }
}
