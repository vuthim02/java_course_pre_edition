-- Seed data for multi-tenant SaaS (database-per-tenant model)
-- Uses UUID primary keys as defined in migration schemas

INSERT INTO tenants (id, slug, name, tier, status, schema_name, max_users, max_storage_gb, features_enabled)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'megacorp',      'MegaCorp International', 'ENTERPRISE', 'ACTIVE',  'tenant_megacorp',  500,  1000, true),
    ('b2c3d4e5-f6a7-8901-bcde-f12345678901', 'smallbiz',       'SmallBiz Solutions',    'STARTER',    'ACTIVE',  'tenant_smallbiz',   25,   50,   true);

INSERT INTO users (id, tenant_id, email, password_hash, full_name, role, status)
VALUES
    ('c3d4e5f6-a7b8-9012-cdef-123456789012', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'admin@megacorp.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane Doe',      'ADMIN', 'ACTIVE'),
    ('d4e5f6a7-b8c9-0123-defa-234567890123', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'user@megacorp.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John Smith',    'USER',  'ACTIVE'),
    ('e5f6a7b8-c9d0-1234-efab-345678901234', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'admin@smallbiz.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice Brown',   'ADMIN', 'ACTIVE'),
    ('f6a7b8c9-d0e1-2345-fabc-456789012345', 'b2c3d4e5-f6a7-8901-bcde-f12345678901', 'employee@smallbiz.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob Wilson',    'USER',  'ACTIVE');
