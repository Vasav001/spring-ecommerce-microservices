package com.vasav.ecom.catalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
@EnableKafka
public class CatalogServiceApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(CatalogServiceApplication.class, args);

        KafkaTemplate<String, String> kafkaTemplate = context.getBean(KafkaTemplate.class);

        kafkaTemplate.send("test-topic", "hello-kafka-123")
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("Kafka failed: " + ex.getMessage());
                    } else {
                        System.out.println("Kafka message sent successfully");
                    }
                });
    }

}
