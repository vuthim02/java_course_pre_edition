-- Seed data for e-commerce platform

-- Users
INSERT INTO users (email, password_hash, first_name, last_name, phone, role)
VALUES
    ('alice@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice', 'Johnson', '+1-555-0101', 'CUSTOMER'),
    ('bob@example.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob',   'Smith',   '+1-555-0102', 'CUSTOMER');

-- Categories
INSERT INTO categories (name, slug, description, parent_id, display_order)
VALUES
    ('Electronics',    'electronics',    'Devices, gadgets, and accessories',          NULL,  1),
    ('Clothing',       'clothing',       'Apparel, footwear, and accessories',         NULL,  2),
    ('Home & Garden',  'home-garden',    'Furniture, decor, and outdoor supplies',     NULL,  3),
    ('Books',          'books',          'Physical and digital books',                 NULL,  4),
    ('Smartphones',    'smartphones',    'Mobile phones and accessories',              1,    5),
    ('Laptops',        'laptops',        'Notebooks and ultrabooks',                   1,    6),
    ('Men''s Clothing','mens-clothing',  'Clothing for men',                           2,    7),
    ('Women''s Clothing','womens-clothing','Clothing for women',                       2,    8);

-- Products
INSERT INTO products (category_id, name, slug, description, price, compare_price, sku, stock_quantity, image_url)
VALUES
    (5, 'iPhone 15 Pro',       'iphone-15-pro',      'Latest Apple smartphone with A17 Pro chip',                              1299.00, 1399.00, 'ELEC-SM-001',  50,  '/images/iphone15.jpg'),
    (5, 'Samsung Galaxy S24',  'samsung-galaxy-s24', 'Flagship Android phone with AI features',                                   999.00,  1099.00, 'ELEC-SM-002',  75,  '/images/galaxys24.jpg'),
    (6, 'MacBook Air M3',      'macbook-air-m3',     'Apple laptop with M3 chip, 13.6-inch display',                              1299.00, NULL,    'ELEC-LP-001',  30,  '/images/macbookair.jpg'),
    (6, 'Dell XPS 15',         'dell-xps-15',        'Premium Windows laptop with OLED display',                                  1799.00, 1999.00, 'ELEC-LP-002',  25,  '/images/dellxps.jpg'),
    (7, 'Classic Leather Jacket','classic-leather-jacket','Genuine leather jacket, timeless design',                              299.00,  399.00,  'CLTH-M-001',   40,  '/images/leather-jacket.jpg'),
    (7, 'Slim Fit Chinos',     'slim-fit-chinos',    'Cotton chino pants, slim fit, multiple colors',                              69.99,  89.99,   'CLTH-M-002',   120, '/images/chinos.jpg'),
    (8, 'Floral Summer Dress', 'floral-summer-dress','Lightweight floral print dress for summer',                                   89.99,  119.99,  'CLTH-W-001',   60,  '/images/floral-dress.jpg'),
    (3, 'Standing Desk',       'standing-desk',      'Adjustable height electric standing desk, 60x30 inches',                     499.00,  649.00,  'HOME-F-001',   20,  '/images/standing-desk.jpg'),
    (3, 'Indoor Herb Garden Kit','indoor-herb-garden','Self-watering indoor herb garden with LED grow light',                        49.99,  NULL,    'HOME-G-001',   100, '/images/herb-garden.jpg'),
    (4, 'Clean Code',          'clean-code',         'Robert C. Martin, A Handbook of Agile Software Craftsmanship',               39.99,  49.99,   'BOOK-DEV-001', 200, '/images/clean-code.jpg');

-- Cart for Alice with items
INSERT INTO carts (user_id, status)
VALUES (1, 'ACTIVE');

INSERT INTO cart_items (cart_id, product_id, quantity)
VALUES
    (1, 1, 1),
    (1, 9, 2),
    (1, 10, 1);
