package com.vasav.ecom.catalogservice;

import com.jayway.jsonpath.JsonPath;
import com.vasav.ecom.catalogservice.config.TestJwts;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * One shared Spring context (and one Postgres + Kafka pair) for all integration tests.
 * Tests isolate themselves with random ids/SKUs/users instead of truncating tables.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
public abstract class AbstractIntegrationTest {

    protected static final String PRODUCTS = "/api/v1/admin/products";

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected JdbcTemplate jdbc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected ConsumerFactory<?, ?> consumerFactory;

    protected static String uniqueSku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected static String json(MvcResult result, String path) {
        return String.valueOf((Object) JsonPath.read(body(result), path));
    }

    protected static String body(MvcResult result) {
        return new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    protected static String kafkaHeader(ConsumerRecord<?, ?> record, String name) {
        Header h = record.headers().lastHeader(name);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }

    protected String productJson(String sku, String status, List<String> imageRefs) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sku", sku);
        body.put("name", "Product " + sku);
        body.put("description", "desc");
        body.put("brand", "Acme");
        body.put("category", "tools");
        body.put("price", "19.99");
        body.put("status", status);
        body.put("imageRefs", imageRefs);
        return toJson(body);
    }

    protected String toJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    protected MvcResult createProduct(UUID id, String sku, String status) throws Exception {
        return mvc.perform(put(PRODUCTS + "/" + id).with(TestJwts.admin())
                        .header("If-None-Match", "*")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson(sku, status, List.of())))
                .andExpect(status().isCreated())
                .andReturn();
    }

    protected UUID activeProduct() throws Exception {
        UUID id = UUID.randomUUID();
        createProduct(id, uniqueSku(), "ACTIVE");
        return id;
    }

    protected void recordPurchase(String userId, UUID productId) {
        jdbc.update("INSERT INTO purchase (user_id, product_id, order_id, created_at) VALUES (?, ?, ?, now())",
                userId, productId, UUID.randomUUID());
    }

    protected List<Map<String, Object>> outboxRows(Object aggregateId) {
        return jdbc.queryForList("SELECT * FROM outbox_event WHERE aggregate_id = ? ORDER BY seq",
                aggregateId.toString());
    }

    protected int count(String sql, Object... args) {
        Integer n = jdbc.queryForObject(sql, Integer.class, args);
        return n == null ? 0 : n;
    }

    /**
     * Reads the topic from the beginning with a fresh consumer group until {@code expected} matching
     * records are seen (or the timeout passes), then keeps polling briefly to catch unexpected extras.
     */
    @SuppressWarnings("unchecked")
    protected List<ConsumerRecord<String, String>> readTopic(String topic, Predicate<ConsumerRecord<String, String>> filter,
                                                             int expected, Duration timeout) {
        List<ConsumerRecord<String, String>> matched = new ArrayList<>();
        try (Consumer<String, String> consumer = (Consumer<String, String>) consumerFactory
                .createConsumer("test-" + UUID.randomUUID(), "test-reader")) {
            consumer.subscribe(List.of(topic));
            long deadline = System.nanoTime() + timeout.toNanos();
            long settleUntil = Long.MAX_VALUE;
            while (System.nanoTime() < Math.min(deadline, settleUntil)) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(250))) {
                    if (filter.test(record)) {
                        matched.add(record);
                    }
                }
                if (matched.size() >= expected && settleUntil == Long.MAX_VALUE) {
                    settleUntil = System.nanoTime() + Duration.ofSeconds(1).toNanos();
                }
            }
        }
        return matched;
    }
}
