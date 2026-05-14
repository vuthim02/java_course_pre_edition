-- Seed data for trading platform

-- Portfolios
INSERT INTO portfolios (user_id, total_value, cash_balance, invested_amount, realized_pnl, unrealized_pnl)
VALUES
    (1, 125000.00, 25000.00,  95000.00,  5000.00,  5000.00),
    (2, 50000.00,  10000.00,  38000.00,  2000.00,  2000.00),
    (3, 250000.00, 50000.00,  180000.00, 15000.00, 20000.00);

-- Portfolio holdings
INSERT INTO portfolio_holdings (portfolio_id, symbol, quantity, avg_cost_basis, current_value)
VALUES
    (1, 'AAPL', 50,  185.50, 9500.00),
    (1, 'GOOGL', 30, 140.20, 4200.00),
    (1, 'MSFT', 40,  380.00, 15200.00),
    (1, 'TSLA', 20,  245.00, 4900.00),
    (2, 'AAPL', 20,  182.00, 3800.00),
    (2, 'NVDA', 15,  880.00, 13200.00),
    (3, 'AMZN', 100, 178.00, 17800.00),
    (3, 'GOOGL', 80, 138.00, 11040.00),
    (3, 'MSFT', 100, 370.00, 37000.00);

-- Orders
INSERT INTO orders (user_id, symbol, side, order_type, status, quantity, filled_quantity, price, total_amount, fee, time_in_force)
VALUES
    (1, 'AAPL',  'BUY',  'LIMIT', 'FILLED',    50,  50,  185.00, 9250.00,  9.25,  'GTC'),
    (1, 'TSLA',  'SELL', 'MARKET','FILLED',    10,  10,  248.50, 2485.00,  2.49,  'DAY'),
    (2, 'NVDA',  'BUY',  'MARKET','FILLED',    15,  15,  875.00, 13125.00, 13.13, 'DAY'),
    (3, 'AMZN',  'BUY',  'LIMIT', 'PARTIAL',   200, 100, 176.50, 17650.00, 17.65, 'GTC'),
    (3, 'MSFT',  'BUY',  'STOP',  'PENDING',   50,  0,   365.00, NULL,     0.00,   'GTC');

-- Trades
INSERT INTO trades (buy_order_id, sell_order_id, symbol, quantity, price, total_amount, buyer_fee, seller_fee)
VALUES
    (1, NULL, 'AAPL', 50,  185.00, 9250.00,  9.25,  9.25),
    (NULL, 2, 'TSLA', 10,  248.50, 2485.00,  2.49,  2.49),
    (3, NULL, 'NVDA', 15,  875.00, 13125.00, 13.13, 13.13),
    (4, NULL, 'AMZN', 100, 176.50, 17650.00, 17.65, 17.65);
