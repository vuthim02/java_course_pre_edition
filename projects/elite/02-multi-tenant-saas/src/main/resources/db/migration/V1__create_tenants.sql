CREATE TABLE IF NOT EXISTS tenants (
    id              UUID            PRIMARY KEY,
    slug            VARCHAR(100)    NOT NULL UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    tier            VARCHAR(20)     NOT NULL DEFAULT 'FREE',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    schema_name     VARCHAR(100),
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activated_at    TIMESTAMP,
    suspended_at    TIMESTAMP,
    max_users       INT             NOT NULL DEFAULT 10,
    max_storage_gb  INT             NOT NULL DEFAULT 5,
    features_enabled BOOLEAN        NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_tenants_slug ON tenants(slug);
CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants(status);
