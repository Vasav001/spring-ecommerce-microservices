package com.vasav.ecom.catalogservice;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TestKafkaConsumer {

    @KafkaListener(
        topics = "test-topic",
        groupId = "test-consumer-group"
    )
    public void consume(String message) {
        System.out.println("Received Kafka message: " + message);
    }
}