package com.vasav.ecom.catalogservice.messaging.config;

import com.vasav.ecom.catalogservice.messaging.PoisonMessageException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlingConfig {

    private static boolean isDbDown(Exception ex) {
        Throwable cause = ex.getCause();
        return cause instanceof CannotCreateTransactionException
                || cause instanceof DataAccessResourceFailureException;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(4);
        backOff.setInitialInterval(1_000);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(8_000);
//        backoff 1 then 2 then 4 then 8

        DefaultErrorHandler handler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate), backOff);
        // dont retry poison msgs
        handler.addNotRetryableExceptions(PoisonMessageException.class);
        // id db is down then unlimited retry with 10 sec gaps
        handler.setBackOffFunction((record, ex) -> isDbDown(ex)
                ? new FixedBackOff(10_000, FixedBackOff.UNLIMITED_ATTEMPTS) : null);
        return handler;
    }
}
