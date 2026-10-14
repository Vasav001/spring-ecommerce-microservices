package com.vasav.ecom.catalogservice.producer;

import com.vasav.ecom.catalogservice.events.EventTypes;
import com.vasav.ecom.catalogservice.events.KafkaTopics;
import com.vasav.ecom.catalogservice.events.ProductRatingChanged;
import com.vasav.ecom.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RatingEventProducer {

    private final OutboxService outboxService;
    private final JsonMapper jsonMapper;

    public void changed(UUID productId, long reviewCount, Double averageRating) {
        BigDecimal average = averageRating == null ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP);
        outboxService.save(KafkaTopics.REVIEW_PRODUCT_RATINGS, productId.toString(), EventTypes.PRODUCT_RATING_CHANGED,
                jsonMapper.writeValueAsBytes(new ProductRatingChanged(productId, average, reviewCount)));
    }
}
