package com.vasav.ecom.inventoryservice.consumer;

import com.vasav.ecom.inventoryservice.events.KafkaTopics;
import com.vasav.ecom.inventoryservice.events.ProductUpserted;
import com.vasav.ecom.inventoryservice.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final StockService stockService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = KafkaTopics.CATALOG_PRODUCTS, groupId = "inventory-service.catalog")
    public void onProduct(ConsumerRecord<String, String> record) {
        if (record.value() == null) {
            log.warn("Skipping catalog record without value, key {}", record.key());
            return;
        }
        stockService.track(jsonMapper.readValue(record.value(), ProductUpserted.class).productId());
    }
}
