-- Update order status values from lowercase to uppercase
-- This migration updates existing order status values to match the new enum format

UPDATE orders 
SET status = 'PENDING' 
WHERE status = 'pending';

UPDATE orders 
SET status = 'CONFIRMED' 
WHERE status = 'confirmed';

UPDATE orders 
SET status = 'SHIPPING' 
WHERE status = 'shipping';

UPDATE orders 
SET status = 'DELIVERED' 
WHERE status = 'delivered';

UPDATE orders 
SET status = 'CANCELLED' 
WHERE status = 'cancelled';

