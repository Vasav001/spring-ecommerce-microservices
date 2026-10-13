-- table used by outbox lib
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
