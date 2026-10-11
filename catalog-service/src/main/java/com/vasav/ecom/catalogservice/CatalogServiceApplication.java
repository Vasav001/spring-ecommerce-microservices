package com.vasav.ecom.catalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CatalogServiceApplication {

    public static void main(String[] args) throws InterruptedException {
//        uncomment HelloConsumer for testing kafka producer and consumer

//        ConfigurableApplicationContext context =
        SpringApplication.run(CatalogServiceApplication.class, args);

//        KafkaTemplate<String, String> kafkaTemplate =
//                context.getBean(KafkaTemplate.class);
//
//        for (int i = 1; i <= 5; i++) {
//            kafkaTemplate.send("hello", "hello " + i);
//            System.out.println("Sent: hello " + i);
//
//            Thread.sleep(4000);
//        }
    }

}
