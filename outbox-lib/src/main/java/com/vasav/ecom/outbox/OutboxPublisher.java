package com.vasav.ecom.outbox;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OutboxPublisher implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final long LOCK_KEY = 0x6f7574626f78L;

    private record Pending(long id, String topic, String key, byte[] value, String headers) {
    }

    private final OutboxProperties props;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final DefaultKafkaProducerFactory<String, byte[]> producerFactory;
    private final KafkaTemplate<String, byte[]> kafka;
    private final JsonMapper json = JsonMapper.builder().build();
    private volatile ScheduledExecutorService executor;

    public OutboxPublisher(OutboxProperties props, JdbcTemplate jdbc, TransactionTemplate tx,
                           Map<String, Object> producerProps) {
        this.props = props;
        this.jdbc = jdbc;
        this.tx = tx;
        Map<String, Object> config = new HashMap<>(producerProps);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        this.producerFactory = new DefaultKafkaProducerFactory<>(config);
        this.kafka = new KafkaTemplate<>(producerFactory);
    }

    public int pollOnce() {
        Integer published = tx.execute(status -> {
            if (!jdbc.queryForObject("SELECT pg_try_advisory_xact_lock(?)", Boolean.class, LOCK_KEY)) {
                return 0;
            }
            List<Pending> batch = jdbc.query("""
                            SELECT id, topic, record_key, value, headers::text FROM outbox_record
                            WHERE processed_at IS NULL ORDER BY id LIMIT ?""",
                    (rs, n) -> new Pending(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBytes(4), rs.getString(5)),
                    props.getBatchSize());
            int sent = 0;
            for (Pending p : batch) {
                try {
                    send(p);
                } catch (Exception e) {
                    log.error("Outbox send failed for record {} (topic {}); later records wait until it succeeds",
                            p.id(), p.topic(), e);
                    break;
                }
                jdbc.update("UPDATE outbox_record SET processed_at = now() WHERE id = ?", p.id());
                sent++;
            }
            return sent;
        });
        return published == null ? 0 : published;
    }

    private void send(Pending p) throws Exception {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(p.topic(), p.key(), p.value());
        json.readValue(p.headers(), new TypeReference<Map<String, String>>() {})
                .forEach((k, v) -> record.headers().add(k, v.getBytes(StandardCharsets.UTF_8)));
        kafka.send(record).get(props.getSendTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor(Thread.ofPlatform().name("outbox-publisher").daemon().factory());
        executor.scheduleWithFixedDelay(() -> {
            try {
                pollOnce();
            } catch (Exception e) {
                log.error("Outbox poll failed", e);
            }
        }, 0, props.getPollInterval().toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(props.getSendTimeout().toSeconds() + 5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executor = null;
        }
        producerFactory.destroy();
    }

    @Override
    public boolean isRunning() {
        return executor != null;
    }
}
