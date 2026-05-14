-- Seed data for auth system
-- Passwords are bcrypt hashes of "password"

INSERT INTO users (username, email, password_hash, full_name, role, enabled, email_verified)
VALUES
    ('admin',   'admin@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User',   'ADMIN',   true, true),
    ('user',    'user@example.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Regular User', 'USER',    true, true),
    ('mod',     'mod@example.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Moderator',    'MODERATOR', true, true);
