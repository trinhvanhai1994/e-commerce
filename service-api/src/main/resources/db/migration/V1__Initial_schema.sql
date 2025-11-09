-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    old_price DECIMAL(19, 2),
    short_desc TEXT,
    description TEXT,
    main_image VARCHAR(500),
    stock INTEGER NOT NULL DEFAULT 0,
    category VARCHAR(100),
    quantity VARCHAR(50),
    benefits TEXT,
    ingredients TEXT,
    specifications TEXT,
    technology TEXT,
    storage TEXT,
    discount INTEGER,
    rating DOUBLE PRECISION,
    review_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create product_images table
CREATE TABLE IF NOT EXISTS product_images (
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(50) NOT NULL UNIQUE,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_address TEXT NOT NULL,
    province_code VARCHAR(10),
    district_code VARCHAR(10),
    ward_code VARCHAR(10),
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    payment_method VARCHAR(50),
    sub_total DECIMAL(19, 2) NOT NULL,
    shipping_fee DECIMAL(19, 2),
    total DECIMAL(19, 2) NOT NULL,
    order_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create order_items table
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(19, 2) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create provinces table
CREATE TABLE IF NOT EXISTS provinces (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255),
    type VARCHAR(50),
    name_with_type VARCHAR(255)
);

-- Create districts table
CREATE TABLE IF NOT EXISTS districts (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    slug VARCHAR(255),
    name_with_type VARCHAR(255),
    path VARCHAR(500),
    path_with_type VARCHAR(500),
    parent_code VARCHAR(10) NOT NULL,
    FOREIGN KEY (parent_code) REFERENCES provinces(code)
);

-- Create wards table
CREATE TABLE IF NOT EXISTS wards (
    code VARCHAR(10) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    slug VARCHAR(255),
    name_with_type VARCHAR(255),
    path VARCHAR(500),
    path_with_type VARCHAR(500),
    parent_code VARCHAR(10) NOT NULL,
    FOREIGN KEY (parent_code) REFERENCES districts(code)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_orders_customer_phone ON orders(customer_phone);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_order_id ON orders(order_id);
CREATE INDEX IF NOT EXISTS idx_districts_parent_code ON districts(parent_code);
CREATE INDEX IF NOT EXISTS idx_wards_parent_code ON wards(parent_code);

-- Insert default admin user (password: thiyen1 - should be hashed in production)
-- Default password hash for 'thiyen1' using BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (username, password, email, role) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@thiyen.com', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;


