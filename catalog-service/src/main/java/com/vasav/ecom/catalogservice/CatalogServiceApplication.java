package com.vasav.ecom.catalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CatalogServiceApplication {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }

}
