package com.vasav.ecom.catalogservice;

import com.jayway.jsonpath.JsonPath;
import com.vasav.ecom.catalogservice.config.CorrelationIdFilter;
import com.vasav.ecom.catalogservice.config.TestJwts;
import com.vasav.ecom.catalogservice.events.EventHeaders;
import com.vasav.ecom.catalogservice.events.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OutboxPublishingTests extends AbstractIntegrationTest {

    @Test
    void pollerPublishesInSeqOrderWithHeadersSameKeyAndPartition() throws Exception {
        UUID id = UUID.randomUUID();
        String sku = uniqueSku();
        String correlationId = "corr-" + id;

        mvc.perform(put(PRODUCTS + "/" + id).with(TestJwts.admin()).header("If-None-Match", "*")
                        .header(CorrelationIdFilter.HEADER, correlationId)
                        .contentType(MediaType.APPLICATION_JSON).content(productJson(sku, "DRAFT", List.of())))
                .andExpect(status().isCreated());
        List<String> nextStatus = List.of("ACTIVE", "ARCHIVED");
        for (int version = 0; version < 2; version++) {
            mvc.perform(put(PRODUCTS + "/" + id).with(TestJwts.admin()).header("If-Match", "\"" + version + "\"")
                            .header(CorrelationIdFilter.HEADER, correlationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(productJson(sku, nextStatus.get(version), List.of())))
                    .andExpect(status().isOk());
        }

        List<Map<String, Object>> rows = outboxRows(id);
        assertThat(rows).hasSize(3);
        assertThat(rows).allSatisfy(row -> assertThat(row.get("correlation_id")).isEqualTo(correlationId));

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(outboxRows(id)).allSatisfy(row -> assertThat(row.get("published_at")).isNotNull()));

        List<ConsumerRecord<String, String>> records = readTopic(KafkaTopics.CATALOG_PRODUCTS,
                r -> id.toString().equals(r.key()), 3, Duration.ofSeconds(30));

        assertThat(records).hasSize(3);
        assertThat(records).extracting(ConsumerRecord::partition).containsOnly(records.getFirst().partition());
        for (int i = 0; i < 3; i++) {
            ConsumerRecord<String, String> record = records.get(i);
            Map<String, Object> row = rows.get(i);
            assertThat(record.key()).isEqualTo(id.toString());
            assertThat(record.value()).isEqualTo(row.get("payload"));
            assertThat((Object) JsonPath.read(record.value(), "$.version")).isEqualTo(i);   // seq order kept
            assertThat(kafkaHeader(record, EventHeaders.EVENT_ID)).isEqualTo(row.get("id").toString());
            assertThat(kafkaHeader(record, EventHeaders.EVENT_TYPE)).isEqualTo("ProductUpserted");
            assertThat(kafkaHeader(record, EventHeaders.EVENT_VERSION)).isEqualTo("1");
            assertThat(Instant.parse(kafkaHeader(record, EventHeaders.OCCURRED_AT))).isNotNull();
            assertThat(kafkaHeader(record, EventHeaders.CORRELATION_ID)).isEqualTo(correlationId);
        }
    }

    @Test
    void writeWithoutCorrelationHeaderStillGetsGeneratedCorrelationId() throws Exception {
        UUID id = activeProduct();
        Object correlationId = outboxRows(id).getFirst().get("correlation_id");
        assertThat(correlationId).asString().matches("[0-9a-f-]{36}");
    }
}
