package com.vasav.ecom.catalogservice;

import com.vasav.ecom.catalogservice.events.EventHeaders;
import com.vasav.ecom.catalogservice.events.KafkaTopics;
import com.vasav.ecom.catalogservice.exception.UnknownEventVersionException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class OrderConsumerTests extends AbstractIntegrationTest {

    private static final String CONSUMER = KafkaTopics.GROUP_ORDERS;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    private void send(String key, String value, Map<String, String> headers) throws Exception {
        ProducerRecord<String, String> record = new ProducerRecord<>(KafkaTopics.ORDER_EVENTS, key, value);
        headers.forEach((k, v) -> record.headers().add(k, v.getBytes(StandardCharsets.UTF_8)));
        kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);
    }

    private static Map<String, String> headers(String type, String version, UUID eventId) {
        Map<String, String> h = new LinkedHashMap<>();
        h.put(EventHeaders.EVENT_TYPE, type);
        h.put(EventHeaders.EVENT_VERSION, version);
        h.put(EventHeaders.EVENT_ID, eventId.toString());
        return h;
    }

    private String delivered(UUID orderId, String userId, UUID... productIds) {
        return toJson(Map.of("orderId", orderId, "userId", userId, "productIds", List.of(productIds)));
    }

    private boolean purchased(String userId, UUID productId) {
        return count("SELECT count(*) FROM purchase WHERE user_id = ? AND product_id = ?", userId, productId) == 1;
    }

    private int inboxRows(UUID eventId) {
        return count("SELECT count(*) FROM processed_event WHERE consumer = ? AND event_id = ?", CONSUMER, eventId);
    }

    /** Same key -> same partition -> processed in order, so once the sentinel is done, earlier records are too. */
    private void sendSentinelAndWait(String key, String userId) throws Exception {
        UUID product = activeProduct();
        UUID eventId = UUID.randomUUID();
        send(key, delivered(UUID.randomUUID(), userId, product), headers("OrderDelivered", "1", eventId));
        await().atMost(Duration.ofSeconds(30)).until(() -> purchased(userId, product));
    }

    @Test
    void orderDeliveredRecordsPurchasesAndInboxRow() throws Exception {
        UUID p1 = activeProduct();
        UUID p2 = activeProduct();
        UUID orderId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        String user = "buyer-" + UUID.randomUUID();

        send(orderId.toString(), delivered(orderId, user, p1, p2), headers("OrderDelivered", "1", eventId));

        await().atMost(Duration.ofSeconds(30)).until(() -> purchased(user, p1) && purchased(user, p2));
        assertThat(inboxRows(eventId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT order_id FROM purchase WHERE user_id = ? AND product_id = ?",
                UUID.class, user, p1)).isEqualTo(orderId);
    }

    @Test
    void duplicateEventIdIsProcessedOnce() throws Exception {
        UUID p1 = activeProduct();
        UUID p2 = activeProduct();
        UUID eventId = UUID.randomUUID();
        String user = "buyer-" + UUID.randomUUID();
        String key = UUID.randomUUID().toString();

        send(key, delivered(UUID.randomUUID(), user, p1), headers("OrderDelivered", "1", eventId));
        // redelivery with the same eventId; different body so a second processing would be visible
        send(key, delivered(UUID.randomUUID(), user, p2), headers("OrderDelivered", "1", eventId));
        sendSentinelAndWait(key, user);

        assertThat(purchased(user, p1)).isTrue();
        assertThat(purchased(user, p2)).isFalse();
        assertThat(count("SELECT count(*) FROM purchase WHERE user_id = ?", user)).isEqualTo(2);   // p1 + sentinel
        assertThat(inboxRows(eventId)).isEqualTo(1);
    }

    @Test
    void otherEventTypesAreIgnoredWithoutInboxRow() throws Exception {
        UUID p1 = activeProduct();
        UUID eventId = UUID.randomUUID();
        String user = "buyer-" + UUID.randomUUID();
        String key = UUID.randomUUID().toString();

        send(key, delivered(UUID.randomUUID(), user, p1), headers("OrderCreated", "1", eventId));
        sendSentinelAndWait(key, user);

        assertThat(purchased(user, p1)).isFalse();
        assertThat(inboxRows(eventId)).isZero();
    }

    @Test
    void poisonMessagesGoToDltWithoutRetriesOrSideEffects() throws Exception {
        UUID product = activeProduct();
        String user = "buyer-" + UUID.randomUUID();

        UUID v2Event = UUID.randomUUID();
        String v2Key = "v2-" + v2Event;
        send(v2Key, delivered(UUID.randomUUID(), user, product), headers("OrderDelivered", "2", v2Event));

        UUID badJsonEvent = UUID.randomUUID();
        String badJsonKey = "badjson-" + badJsonEvent;
        send(badJsonKey, "{not json", headers("OrderDelivered", "1", badJsonEvent));

        String missingHeaderKey = "noid-" + UUID.randomUUID();
        Map<String, String> noEventId = headers("OrderDelivered", "1", UUID.randomUUID());
        noEventId.remove(EventHeaders.EVENT_ID);
        send(missingHeaderKey, delivered(UUID.randomUUID(), user, product), noEventId);

        String noTypeKey = "notype-" + UUID.randomUUID();
        Map<String, String> noType = headers("OrderDelivered", "1", UUID.randomUUID());
        noType.remove(EventHeaders.EVENT_TYPE);
        send(noTypeKey, delivered(UUID.randomUUID(), user, product), noType);

        List<String> keys = List.of(v2Key, badJsonKey, missingHeaderKey, noTypeKey);
        // 4 non-retryable failures; retries would take 1+2+4+8 s each, so 10 s proves they were skipped
        List<ConsumerRecord<String, String>> dlt = readTopic(KafkaTopics.ORDER_EVENTS_DLT,
                r -> keys.contains(r.key()), 4, Duration.ofSeconds(10));

        assertThat(dlt).extracting(ConsumerRecord::key).containsExactlyInAnyOrderElementsOf(keys);
        Map<String, ConsumerRecord<String, String>> byKey = new LinkedHashMap<>();
        dlt.forEach(r -> byKey.put(r.key(), r));

        assertThat(kafkaHeader(byKey.get(v2Key), KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN))
                .isEqualTo(UnknownEventVersionException.class.getName());
        assertThat(kafkaHeader(byKey.get(badJsonKey), KafkaHeaders.DLT_EXCEPTION_CAUSE_FQCN)).startsWith("tools.jackson.");
        assertThat(kafkaHeader(byKey.get(missingHeaderKey), KafkaHeaders.DLT_EXCEPTION_MESSAGE)).contains("Missing header eventId");
        assertThat(kafkaHeader(byKey.get(noTypeKey), KafkaHeaders.DLT_EXCEPTION_MESSAGE)).contains("Missing header eventType");
        for (ConsumerRecord<String, String> r : dlt) {
            assertThat(kafkaHeader(r, KafkaHeaders.DLT_ORIGINAL_TOPIC)).isEqualTo(KafkaTopics.ORDER_EVENTS);
        }
        assertThat(byKey.get(badJsonKey).value()).isEqualTo("{not json");   // original payload preserved

        // nothing was committed for the failed records (inbox insert rolled back with the listener tx)
        assertThat(inboxRows(v2Event)).isZero();
        assertThat(inboxRows(badJsonEvent)).isZero();
        assertThat(purchased(user, product)).isFalse();
    }
}
