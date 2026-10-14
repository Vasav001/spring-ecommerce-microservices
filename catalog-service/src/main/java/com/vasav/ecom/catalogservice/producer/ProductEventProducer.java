package com.vasav.ecom.catalogservice.producer;

import com.vasav.ecom.catalogservice.entity.Product;
import com.vasav.ecom.catalogservice.events.EventTypes;
import com.vasav.ecom.catalogservice.events.KafkaTopics;
import com.vasav.ecom.catalogservice.events.ProductUpserted;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    public void upserted(Product product) {
        ProductUpserted event = new ProductUpserted(product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getImageUrl(), product.getStatus());
        outboxService.save(KafkaTopics.CATALOG_PRODUCTS, product.getId().toString(), EventTypes.PRODUCT_UPSERTED,
                jsonMapper.writeValueAsBytes(event));
    }
}
