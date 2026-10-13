package com.vasav.ecom.outbox;

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@AutoConfiguration(afterName = {
        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
        "org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration"})
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "outbox", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfiguration {

    @Bean
    OutboxService outboxService(DataSource dataSource, ObjectProvider<Tracer> tracer,
                                ObjectProvider<Propagator> propagator) {
        return new OutboxService(new JdbcTemplate(dataSource), tracer.getIfAvailable(), propagator.getIfAvailable());
    }

    @Bean
    OutboxPublisher outboxPublisher(DataSource dataSource, OutboxProperties props,
                                    ObjectProvider<KafkaProperties> kafkaProperties) {
        TransactionTemplate tx = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        KafkaProperties kafka = kafkaProperties.getIfAvailable(KafkaProperties::new);
        return new OutboxPublisher(props, new JdbcTemplate(dataSource), tx, kafka.buildProducerProperties());
    }
}
