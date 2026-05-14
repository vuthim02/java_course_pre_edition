CREATE TABLE IF NOT EXISTS outbox (
    id              VARCHAR(36)     PRIMARY KEY,
    aggregate_id    VARCHAR(36)     NOT NULL,
    aggregate_type  VARCHAR(100)    NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    payload         TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at    TIMESTAMP,
    retry_count     INT             NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox(status);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox(created_at);
