-- task-005: email the customer typed at checkout (optional), used for the order confirmation mail.
-- Older orders and orders placed without an email keep NULL.
ALTER TABLE orders ADD COLUMN customer_email VARCHAR(255);
