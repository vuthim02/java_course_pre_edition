CREATE TABLE IF NOT EXISTS users (
    id              UUID            PRIMARY KEY,
    tenant_id       UUID            NOT NULL REFERENCES tenants(id),
    email           VARCHAR(255)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL DEFAULT 'USER',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at   TIMESTAMP,
    CONSTRAINT uk_user_email_tenant UNIQUE (tenant_id, email)
);

CREATE INDEX IF NOT EXISTS idx_users_tenant ON users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
