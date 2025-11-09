-- Insert initial products from mockData.js
-- This migration inserts the 5 products that are currently in the mock data

-- Product 1: BỘT NGŨ HẮC MÈ ĐEN
INSERT INTO products (id, name, price, old_price, short_desc, description, main_image, stock, category, quantity, benefits, ingredients, specifications, technology, storage, discount, rating, review_count, created_at, updated_at)
VALUES (
    1,
    'BỘT NGŨ HẮC MÈ ĐEN',
    299000,
    390000,
    'Bột Ngũ Hắc Mè Đen là bữa ăn thay thế tiện lợi...',
    'Bột Ngũ Hắc Mè Đen là bữa ăn thay thế tiện lợi, giàu dinh dưỡng, giúp bổ sung năng lượng và các dưỡng chất cần thiết cho cơ thể.',
    '/images/products/details/black/1.png',
    100,
    'ME_DEN',
    '500g/lon',
    'Bổ sung dinh dưỡng, tăng cường sức khỏe, hỗ trợ tiêu hóa.',
    'Mè đen, đậu đen, đậu xanh, đậu đỏ, đậu nành, gạo lứt, hạt sen, hạt điều, hạt óc chó, hạt hạnh nhân.',
    'Sản phẩm được sản xuất tại nhà máy đạt tiêu chuẩn ISO 22000:2018 và GMP Codex (TCVN 5603:2023)',
    'Công nghệ sấy lạnh giữ nguyên dinh dưỡng.',
    'Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp.',
    23,
    5.0,
    32,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- Insert gallery images for Product 1
DELETE FROM product_images WHERE product_id = 1;
INSERT INTO product_images (product_id, image_url) VALUES
(1, '/images/products/details/black/1.png'),
(1, '/images/products/details/black/2.png'),
(1, '/images/products/details/black/3.png'),
(1, '/images/products/details/black/4.png'),
(1, '/images/products/details/black/5.png'),
(1, '/images/products/details/black/6.png'),
(1, '/images/products/details/black/7.png'),
(1, '/images/products/details/black/8.png');

-- Product 2: COMBO 2 LON BỘT NGŨ HẮC MÈ ĐEN
INSERT INTO products (id, name, price, old_price, short_desc, description, main_image, stock, category, quantity, benefits, ingredients, specifications, technology, storage, discount, rating, review_count, created_at, updated_at)
VALUES (
    2,
    'COMBO 2 LON BỘT NGŨ HẮC MÈ ĐEN',
    499000,
    780000,
    'Combo tiết kiệm cho gia đình...',
    'Combo 2 lon Bột Ngũ Hắc Mè Đen - Tiết kiệm hơn khi mua số lượng lớn, phù hợp cho gia đình sử dụng lâu dài.',
    '/images/products/combo-black.png',
    100,
    'ME_DEN',
    '500g/lon x 2',
    'Bổ sung dinh dưỡng, tăng cường sức khỏe, hỗ trợ tiêu hóa.',
    'Mè đen, đậu đen, đậu xanh, đậu đỏ, đậu nành, gạo lứt, hạt sen, hạt điều, hạt óc chó, hạt hạnh nhân.',
    'Sản phẩm được sản xuất tại nhà máy đạt tiêu chuẩn ISO 22000:2018 và GMP Codex (TCVN 5603:2023)',
    'Công nghệ sấy lạnh giữ nguyên dinh dưỡng.',
    'Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp.',
    36,
    5.0,
    32,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- Insert gallery images for Product 2 (same as Product 1)
DELETE FROM product_images WHERE product_id = 2;
INSERT INTO product_images (product_id, image_url) VALUES
(2, '/images/products/details/black/1.png'),
(2, '/images/products/details/black/2.png'),
(2, '/images/products/details/black/3.png'),
(2, '/images/products/details/black/4.png'),
(2, '/images/products/details/black/5.png'),
(2, '/images/products/details/black/6.png'),
(2, '/images/products/details/black/7.png'),
(2, '/images/products/details/black/8.png');

-- Product 3: BỘT NGŨ SẮC HỒNG ĐẬU
INSERT INTO products (id, name, price, old_price, short_desc, description, main_image, stock, category, quantity, benefits, ingredients, specifications, technology, storage, discount, rating, review_count, created_at, updated_at)
VALUES (
    3,
    'BỘT NGŨ SẮC HỒNG ĐẬU',
    299000,
    390000,
    'Bột Ngũ Sắc Hồng Đậu là bữa ăn thay thế tiện lợi...',
    'Bột Ngũ Sắc Hồng Đậu là bữa ăn thay thế tiện lợi, giàu dinh dưỡng từ các loại đậu và hạt tự nhiên, giúp bổ sung năng lượng và các dưỡng chất cần thiết.',
    '/images/products/details/pink/1.png',
    100,
    'HONG_DAU',
    '500g/lon',
    'Bổ sung dinh dưỡng, tăng cường sức khỏe, hỗ trợ tiêu hóa.',
    'Đậu đỏ, đậu xanh, đậu nành, gạo lứt, hạt sen, hạt điều, hạt óc chó, hạt hạnh nhân, hạt macca.',
    'Sản phẩm được sản xuất tại nhà máy đạt tiêu chuẩn ISO 22000:2018 và GMP Codex (TCVN 5603:2023)',
    'Công nghệ sấy lạnh giữ nguyên dinh dưỡng.',
    'Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp.',
    23,
    5.0,
    32,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- Insert gallery images for Product 3
DELETE FROM product_images WHERE product_id = 3;
INSERT INTO product_images (product_id, image_url) VALUES
(3, '/images/products/details/pink/1.png'),
(3, '/images/products/details/pink/2.png'),
(3, '/images/products/details/pink/3.png'),
(3, '/images/products/details/pink/4.png'),
(3, '/images/products/details/pink/5.png'),
(3, '/images/products/details/pink/6.png'),
(3, '/images/products/details/pink/7.png'),
(3, '/images/products/details/pink/8.png'),
(3, '/images/products/details/pink/9.png');

-- Product 4: COMBO 2 LON BỘT NGŨ SẮC HỒNG ĐẬU
INSERT INTO products (id, name, price, old_price, short_desc, description, main_image, stock, category, quantity, benefits, ingredients, specifications, technology, storage, discount, rating, review_count, created_at, updated_at)
VALUES (
    4,
    'COMBO 2 LON BỘT NGŨ SẮC HỒNG ĐẬU',
    499000,
    780000,
    'Combo tiết kiệm cho gia đình...',
    'Combo 2 lon Bột Ngũ Sắc Hồng Đậu - Tiết kiệm hơn khi mua số lượng lớn, phù hợp cho gia đình sử dụng lâu dài.',
    '/images/products/combo-pink.png',
    100,
    'HONG_DAU',
    '500g/lon x 2',
    'Bổ sung dinh dưỡng, tăng cường sức khỏe, hỗ trợ tiêu hóa.',
    'Đậu đỏ, đậu xanh, đậu nành, gạo lứt, hạt sen, hạt điều, hạt óc chó, hạt hạnh nhân, hạt macca.',
    'Sản phẩm được sản xuất tại nhà máy đạt tiêu chuẩn ISO 22000:2018 và GMP Codex (TCVN 5603:2023)',
    'Công nghệ sấy lạnh giữ nguyên dinh dưỡng.',
    'Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp.',
    36,
    5.0,
    32,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- Insert gallery images for Product 4 (same as Product 3)
DELETE FROM product_images WHERE product_id = 4;
INSERT INTO product_images (product_id, image_url) VALUES
(4, '/images/products/details/pink/1.png'),
(4, '/images/products/details/pink/2.png'),
(4, '/images/products/details/pink/3.png'),
(4, '/images/products/details/pink/4.png'),
(4, '/images/products/details/pink/5.png'),
(4, '/images/products/details/pink/6.png'),
(4, '/images/products/details/pink/7.png'),
(4, '/images/products/details/pink/8.png'),
(4, '/images/products/details/pink/9.png');

-- Product 5: COMBO 2 (1 BỘT NGŨ HẮC MÈ ĐEN + 1 BỘT NGŨ SẮC HỒNG ĐẬU)
INSERT INTO products (id, name, price, old_price, short_desc, description, main_image, stock, category, quantity, benefits, ingredients, specifications, technology, storage, discount, rating, review_count, created_at, updated_at)
VALUES (
    5,
    'COMBO 2 (1 BỘT NGŨ HẮC MÈ ĐEN + 1 BỘT NGŨ SẮC HỒNG ĐẬU)',
    499000,
    780000,
    'Combo tiết kiệm cho gia đình...',
    'Combo 2 sản phẩm: 1 lon Bột Ngũ Hắc Mè Đen + 1 lon Bột Ngũ Sắc Hồng Đậu - Đa dạng hương vị, tiết kiệm hơn khi mua combo.',
    '/images/products/Combo-mix.png',
    100,
    'COMBO',
    '500g/lon x 2',
    'Bổ sung dinh dưỡng, tăng cường sức khỏe, hỗ trợ tiêu hóa.',
    'Mè đen, đậu đen, đậu xanh, đậu đỏ, đậu nành, gạo lứt, hạt sen, hạt điều, hạt óc chó, hạt hạnh nhân, hạt macca.',
    'Sản phẩm được sản xuất tại nhà máy đạt tiêu chuẩn ISO 22000:2018 và GMP Codex (TCVN 5603:2023)',
    'Công nghệ sấy lạnh giữ nguyên dinh dưỡng.',
    'Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp.',
    36,
    5.0,
    32,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- Insert gallery images for Product 52
DELETE FROM product_images WHERE product_id = 52;
INSERT INTO product_images (product_id, image_url) VALUES
(52, '/images/products/details/mix/0.png'),
(52, '/images/products/details/mix/1.png'),
(52, '/images/products/details/mix/2.png'),
(52, '/images/products/details/mix/3.png'),
(52, '/images/products/details/mix/4.png'),
(52, '/images/products/details/mix/5.png'),
(52, '/images/products/details/mix/6.png'),
(52, '/images/products/details/mix/7.png'),
(52, '/images/products/details/mix/8.png'),
(52, '/images/products/details/mix/9.png'),
(52, '/images/products/details/mix/10.png'),
(52, '/images/products/details/mix/11.png');

