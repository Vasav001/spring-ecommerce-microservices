package com.vasav.ecom.searchservice.consumer;

import com.vasav.ecom.searchservice.events.KafkaTopics;
import com.vasav.ecom.searchservice.events.ProductRatingChanged;
import com.vasav.ecom.searchservice.events.ProductUpserted;
import com.vasav.ecom.searchservice.events.StockLevelChanged;
import com.vasav.ecom.searchservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final SearchService searchService;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = KafkaTopics.CATALOG_PRODUCTS, groupId = "search-service.products")
    public void onProduct(ConsumerRecord<String, String> record) {
        if (hasValue(record)) {
            searchService.onProduct(jsonMapper.readValue(record.value(), ProductUpserted.class));
        }
    }

    @KafkaListener(topics = KafkaTopics.INVENTORY_STOCK_LEVELS, groupId = "search-service.stock")
    public void onStock(ConsumerRecord<String, String> record) {
        if (hasValue(record)) {
            searchService.onStock(jsonMapper.readValue(record.value(), StockLevelChanged.class));
        }
    }

    @KafkaListener(topics = KafkaTopics.REVIEW_PRODUCT_RATINGS, groupId = "search-service.ratings")
    public void onRating(ConsumerRecord<String, String> record) {
        if (hasValue(record)) {
            searchService.onRating(jsonMapper.readValue(record.value(), ProductRatingChanged.class));
        }
    }

    private static boolean hasValue(ConsumerRecord<String, String> record) {
        if (record.value() == null) {
            log.warn("Skipping {} record without value, key {}", record.topic(), record.key());
            return false;
        }
        return true;
    }
}
