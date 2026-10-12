package com.vasav.ecom.catalogservice;

import com.jayway.jsonpath.JsonPath;
import com.vasav.ecom.catalogservice.config.TestJwts;
import com.vasav.ecom.catalogservice.events.KafkaTopics;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class ReviewConcurrencyTests extends AbstractIntegrationTest {

    private static final int REVIEWERS = 20;

    @Test
    void parallelReviewsKeepAggregateExact() throws Exception {
        UUID product = activeProduct();
        List<String> users = new ArrayList<>();
        int expectedSum = 0;
        for (int i = 0; i < REVIEWERS; i++) {
            String user = "parallel-" + i + "-" + UUID.randomUUID();
            users.add(user);
            recordPurchase(user, product);
            expectedSum += i % 5 + 1;
        }

        ExecutorService pool = Executors.newFixedThreadPool(REVIEWERS);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> statuses = new ArrayList<>();
        try {
            for (int i = 0; i < REVIEWERS; i++) {
                String user = users.get(i);
                int rating = i % 5 + 1;
                statuses.add(pool.submit(() -> {
                    start.await();
                    return mvc.perform(post("/api/v1/reviews").with(TestJwts.customer(user))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(toJson(Map.of("productId", product, "rating", rating))))
                            .andReturn().getResponse().getStatus();
                }));
            }
            start.countDown();
            for (Future<Integer> status : statuses) {
                assertThat(status.get(60, TimeUnit.SECONDS)).isEqualTo(201);
            }
        } finally {
            pool.shutdownNow();
        }

        Map<String, Object> rating = jdbc.queryForMap(
                "SELECT rating_count, rating_sum, version FROM product_rating WHERE product_id = ?", product);
        assertThat(rating).containsEntry("rating_count", REVIEWERS)
                .containsEntry("rating_sum", expectedSum)
                .containsEntry("version", (long) REVIEWERS);

        List<Map<String, Object>> events = outboxRows(product).stream()
                .filter(r -> KafkaTopics.CATALOG_PRODUCT_RATINGS.equals(r.get("topic")))
                .toList();
        assertThat(events).hasSize(REVIEWERS);
        // outbox seq order == ratingVersion order: 1..20, no gaps or duplicates
        assertThat(events).extracting(r -> (Integer) JsonPath.read((String) r.get("payload"), "$.ratingVersion"))
                .containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(1, REVIEWERS).boxed().toList());
        assertThat((Object) JsonPath.read((String) events.getLast().get("payload"), "$.ratingCount"))
                .isEqualTo(REVIEWERS);
    }
}
