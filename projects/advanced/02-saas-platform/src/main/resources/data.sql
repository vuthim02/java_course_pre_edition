-- Seed data for SaaS platform

-- Subscription plans
INSERT INTO subscription_plans (name, slug, description, price_monthly, price_yearly, currency, max_users, max_storage_gb, max_projects, features, is_active)
VALUES
    ('Free',       'free',       'Basic features for individuals',                     0.00,   0.00,   'USD', 1,   1,   1,   '["1 project", "1 GB storage", "Community support"]',                                                          true),
    ('Starter',    'starter',    'Essential features for small teams',                 29.99,  299.99, 'USD', 10,  50,  10,  '["10 projects", "50 GB storage", "Email support", "API access", "Basic analytics"]',                          true),
    ('Enterprise', 'enterprise', 'Full platform with dedicated support and compliance', 199.99, 1999.99,'USD', 100, 500, 100, '["Unlimited projects", "500 GB storage", "Priority support", "Advanced analytics", "SSO", "Audit logs"]', true);

-- Tenants
INSERT INTO tenants (name, slug, domain, plan, status, max_users, max_storage_gb)
VALUES
    ('Acme Corp',      'acme-corp',      'acme.example.com',      'ENTERPRISE', 'ACTIVE', 100, 500),
    ('Startup Inc',    'startup-inc',    'startup.example.com',   'STARTER',    'ACTIVE', 10,  50),
    ('Personal Test',  'personal-test',  'personal.example.com',  'FREE',       'ACTIVE', 1,   1);

-- Tenant subscriptions
INSERT INTO tenant_subscriptions (tenant_id, plan_id, status, start_date, end_date, auto_renew)
VALUES
    (1, 3, 'ACTIVE',  '2025-01-01 00:00:00', '2026-01-01 00:00:00', true),
    (2, 2, 'ACTIVE',  '2025-03-15 00:00:00', '2026-03-15 00:00:00', true),
    (3, 1, 'ACTIVE',  '2025-06-01 00:00:00', NULL,                  false);

-- Usage records
INSERT INTO usage_records (tenant_id, metric, value, recorded_at)
VALUES
    (1, 'API_CALLS',      1250000, '2025-06-01 12:00:00'),
    (1, 'STORAGE_GB',     342,      '2025-06-01 12:00:00'),
    (1, 'ACTIVE_USERS',   47,       '2025-06-01 12:00:00'),
    (2, 'API_CALLS',      234000,  '2025-06-01 12:00:00'),
    (2, 'STORAGE_GB',     28,      '2025-06-01 12:00:00'),
    (2, 'ACTIVE_USERS',   7,       '2025-06-01 12:00:00'),
    (3, 'API_CALLS',      1200,    '2025-06-01 12:00:00'),
    (3, 'STORAGE_GB',     0.5,     '2025-06-01 12:00:00');
