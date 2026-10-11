package com.vasav.ecom.catalogservice;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

//@Component
public class HelloConsumer {

//    @KafkaListener(topics = "hello")
    public void consume(String message) {
        System.out.println("Consumed: " + message);
    }
}