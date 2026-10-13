# outbox-lib

A small transactional outbox for Spring Boot 4, Postgres and Kafka. It polls the table in batches, keeps events in order, lets one instance publish at a time, and delivers at least once.

## 1. Add the dependency

```xml
<dependency>
    <groupId>com.vasav.ecom</groupId>
    <artifactId>outbox-lib</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## 2. Create the table in the service's own migrations

```sql
CREATE TABLE outbox_record (
    id           BIGSERIAL PRIMARY KEY,
    topic        VARCHAR(249) NOT NULL,
    record_key   VARCHAR(255),
    value        BYTEA NOT NULL,
    headers      JSONB NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at TIMESTAMPTZ
);
CREATE INDEX outbox_record_pending_idx ON outbox_record (id) WHERE processed_at IS NULL;
```

## 3. Save events in the same transaction as the business change

```java
@Transactional
public void reserve(...) {
    // ... JPA changes ...
    outboxService.save("inventory.events", sku, "StockReserved", serializer.toBytes(event));
}
```

- `save()` throws if no transaction is active.
- For a non-null key, `save()` locks that `topic:key` until commit, so for each key, id order matches commit order. Call it late in the transaction.
- Kafka headers on every record:
  - `event-id`: a UUID that stays the same on re-sends
  - `event-type`
  - `traceparent`, when tracing is on

## How publishing works

Each poll runs as one database transaction:

1. `pg_try_advisory_xact_lock`. If another instance holds the lock, this one skips the poll. If the holder dies, its connection closes and the lock is released.
2. `SELECT ... WHERE processed_at IS NULL ORDER BY id LIMIT batch-size`.
3. For each record: send, wait for the broker ack, then mark it processed.
4. The first failure is logged and stops the batch, so later records wait and order is kept. The next poll retries the same record.

## Config (library defaults, override in the service)

```yaml
outbox:
  enabled: true
  poll-interval: 1s
  batch-size: 100
  send-timeout: 30s
```

- The producer is built from `spring.kafka.*`, with these forced: byte[] values, `acks=all`, idempotence.
- The library registers no `KafkaTemplate`, `ProducerFactory` or transaction manager bean.

## Tracing

With `spring-boot-starter-opentelemetry` in the service, one trace covers the whole path:

1. **Client to service:** incoming HTTP requests continue the caller's W3C `traceparent` (frontend, Feign, RestClient).
2. **Service to Kafka:** `save()` stores the current `traceparent`, and the publisher sends it as a Kafka header.
3. **Kafka to consumer:** with `spring.kafka.listener.observation-enabled: true`, the consumer's span joins the same trace.

You can view traces in Jaeger, which receives OTLP on 4318.

## Consumers

Duplicates are possible, for example after a crash between the broker ack and the commit, or after a timed-out send that still landed.

To handle them:

1. Insert `event-id` into a processed-events table in the same transaction as the consumer's own changes.
2. Ack after commit.

## Operations

- **Stuck record:** a permanent failure (for example, a record that is too large) blocks the queue. The error is in the logs. To skip the record: `UPDATE outbox_record SET processed_at = now() WHERE id = :id;`
- **Cleanup:** processed rows are kept. Delete old ones on a schedule, for example: `DELETE FROM outbox_record WHERE processed_at < now() - interval '7 days';`

## Build

```bash
mvn clean install   # integration tests need Docker
```
