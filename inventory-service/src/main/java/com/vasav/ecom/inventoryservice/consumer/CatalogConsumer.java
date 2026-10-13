package com.vasav.ecom.inventoryservice.consumer;

import com.vasav.ecom.inventoryservice.events.CatalogProductEvent;
import com.vasav.ecom.inventoryservice.events.KafkaTopics;
import com.vasav.ecom.inventoryservice.service.CatalogProjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogConsumer {

    private final CatalogProjectionService catalogProjectionService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = KafkaTopics.CATALOG_PRODUCTS, groupId = "inventory-service.catalog")
    public void onProduct(ConsumerRecord<String, String> record) {
        if (record.value() == null) {
            log.warn("Skipping catalog record without value, key {}", record.key());
            return;
        }

        UUID productId = UUID.fromString(record.key());
//      inventory keeps track of product status
        CatalogProductEvent event = jsonMapper.readValue(record.value(), CatalogProductEvent.class);
        catalogProjectionService.apply(productId, event.isActive());
    }
}
