package com.vasav.ecom.catalogservice.consumer;

import com.vasav.ecom.catalogservice.exception.UnknownEventVersionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

@Slf4j
@Configuration
public class KafkaErrorHandlingConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {

        // 1. Where failed records go: <topic>.DLT. Partition -1 = chosen from the key,
        //    so the DLT can have fewer partitions than the main topic.
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", -1));

        // 2. Normal retries: 4 attempts, 1s → 2s → 4s → 8s, same partition (keeps per-key order)
        var backOff = new ExponentialBackOffWithMaxRetries(4);
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(8_000);

        var handler = new DefaultErrorHandler(recoverer, backOff);

        // 3. Bad messages: no point retrying → DLT immediately
        handler.addNotRetryableExceptions(
                JacksonException.class,              // invalid JSON (Jackson 3; JsonProcessingException on Jackson 2)
                IllegalArgumentException.class,      // missing header, bad payload
                UnknownEventVersionException.class); // your own exception for unsupported eventVersion

        // 4. Our own database is down: retry forever every 10 s, never dead-letter
        handler.setBackOffFunction((record, ex) ->
                isDatabaseDown(ex) ? new FixedBackOff(10_000, FixedBackOff.UNLIMITED_ATTEMPTS) : null); // null = use default

        handler.setCommitRecovered(true);            // commit the offset once a record is sent to the DLT
        handler.setRetryListeners((record, ex, attempt) ->
                log.warn("Retry {} for {}-{}@{}: {}", attempt, record.topic(), record.partition(),
                         record.offset(), ex.getMessage()));
        return handler;
    }

    private static boolean isDatabaseDown(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof CannotGetJdbcConnectionException
                    || t instanceof CannotCreateTransactionException
                    || t instanceof DataAccessResourceFailureException) {
                return true;
            }
        }
        return false;
    }
}