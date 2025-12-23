-- ========================================
-- per runnare lo script in locale:
-- mysql -u root -proot -h localhost -P 3307 --protocol=TCP isysnap < /Users/macbookprodiantonino/Desktop/Personal/IsySnap/packages/backend/src/main/resources/db/initialize_data_if_empty_DB.sql
-- se si vuole cambiare ambiente bisogna modificare i dati della porta ecc
-- IsySnap - Test Data Initialization
-- ========================================
-- This script safely initializes test data without modifying existing data
-- It checks for existence before inserting any record
-- ========================================

-- ========================================
-- 1. CREATE ADMIN USER (if not exists)
-- ========================================
INSERT INTO users (id, email, password, first_name, last_name, phone, role, created_at, updated_at)
SELECT 'admin-001', 'admin@gmail.com', '$2a$10$99NPmtPknpgvkgmP1/RbOeGWIfpZfULcE5dwNLhmQarz78OU3FT4u',
       'Admin', 'User', '+39 333 1111111', 'ADMIN', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@gmail.com');

-- ========================================
-- 2. CREATE RESTAURANTS (if not exists)
-- ========================================

-- Restaurant 1: La Bella Vita (Italian)
INSERT INTO restaurants (id, name, description, address, phone, logo, status, created_at, updated_at)
SELECT 'rest-001', 'La Bella Vita',
       'Authentic Italian cuisine in the heart of Milan. Family recipes passed down through generations.',
       'Via Roma 123, 20121 Milano (MI)', '+39 02 1234 5678',
       'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=300&h=300&fit=crop',
       'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE id = 'rest-001');

-- Restaurant 2: Sushi Tokyo (Japanese)
INSERT INTO restaurants (id, name, description, address, phone, logo, status, created_at, updated_at)
SELECT 'rest-002', 'Sushi Tokyo',
       'Traditional Japanese sushi and sashimi. Fresh fish delivered daily from Tokyo fish market.',
       'Corso Buenos Aires 45, 20124 Milano (MI)', '+39 02 9876 5432',
       'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=300&h=300&fit=crop',
       'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE id = 'rest-002');

-- Restaurant 3: Burger House (American)
INSERT INTO restaurants (id, name, description, address, phone, logo, status, created_at, updated_at)
SELECT 'rest-003', 'Burger House',
       'Gourmet burgers made with premium Angus beef. Craft beers and artisanal sodas.',
       'Piazza Gae Aulenti 10, 20124 Milano (MI)', '+39 02 5555 7777',
       'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300&h=300&fit=crop',
       'ACTIVE', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM restaurants WHERE id = 'rest-003');

-- ========================================
-- 3. CREATE MENU CATEGORIES (if not exists)
-- ========================================

-- La Bella Vita - Categories
INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-001', 'rest-001', 'Antipasti', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-001');

INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-002', 'rest-001', 'Pizza', 2, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-002');

INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-003', 'rest-001', 'Pasta', 3, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-003');

INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-004', 'rest-001', 'Dolci', 4, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-004');

INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-005', 'rest-001', 'Bevande', 5, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-005');

-- Sushi Tokyo - Categories
INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-101', 'rest-002', 'Nigiri', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-101');

INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-102', 'rest-002', 'Maki', 2, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-102');

INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-103', 'rest-002', 'Sashimi', 3, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-103');

-- Burger House - Categories
INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-201', 'rest-003', 'Burgers', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-201');

INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-202', 'rest-003', 'Sides', 2, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-202');

INSERT INTO menu_categories (id, restaurant_id, name, sort_order, created_at, updated_at)
SELECT 'cat-203', 'rest-003', 'Drinks', 3, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_categories WHERE id = 'cat-203');

-- ========================================
-- 4. CREATE MENU ITEMS (if not exists)
-- ========================================

-- LA BELLA VITA - Menu Items

-- Antipasti
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-001', 'rest-001', 'cat-001', 'Bruschetta Classica',
       'Toasted bread with fresh tomatoes, garlic, basil and extra virgin olive oil',
       7.50, 'https://images.unsplash.com/photo-1572695157366-5e585ab2b69f?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-001');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-002', 'rest-001', 'cat-001', 'Caprese Salad',
       'Fresh mozzarella, tomatoes, basil and balsamic glaze',
       9.00, 'https://images.unsplash.com/photo-1592417817098-8fd3d9eb14a5?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-002');

-- Pizza
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-003', 'rest-001', 'cat-002', 'Margherita',
       'Tomato sauce, fresh mozzarella, basil and extra virgin olive oil',
       12.00, 'https://images.unsplash.com/photo-1604068549290-dea0e4a305ca?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-003');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-004', 'rest-001', 'cat-002', 'Quattro Formaggi',
       'Four cheese pizza: mozzarella, gorgonzola, parmesan and fontina',
       15.00, 'https://images.unsplash.com/photo-1571997478779-2adcbbe9ab2f?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-004');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-005', 'rest-001', 'cat-002', 'Diavola',
       'Spicy salami, mozzarella, tomato sauce and hot peppers',
       14.00, 'https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-005');

-- Pasta
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-006', 'rest-001', 'cat-003', 'Carbonara',
       'Eggs, pecorino cheese, guanciale and black pepper',
       13.50, 'https://images.unsplash.com/photo-1612874742237-6526221588e3?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-006');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-007', 'rest-001', 'cat-003', 'Amatriciana',
       'Tomato sauce, guanciale and pecorino cheese',
       12.50, 'https://images.unsplash.com/photo-1621996346565-e3dbc646d9a9?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-007');

-- Dolci
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-008', 'rest-001', 'cat-004', 'Tiramisu',
       'Classic Italian dessert with coffee-soaked ladyfingers and mascarpone cream',
       6.50, 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-008');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-009', 'rest-001', 'cat-004', 'Panna Cotta',
       'Creamy Italian dessert with berry compote',
       5.50, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-009');

-- Bevande
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-010', 'rest-001', 'cat-005', 'Espresso',
       'Strong Italian coffee',
       2.50, 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-010');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-011', 'rest-001', 'cat-005', 'Prosecco (glass)',
       'Italian sparkling wine',
       6.00, 'https://images.unsplash.com/photo-1547595628-c61a29f496f0?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-011');

-- SUSHI TOKYO - Menu Items

-- Nigiri
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-101', 'rest-002', 'cat-101', 'Salmon Nigiri (2pcs)',
       'Fresh Norwegian salmon on sushi rice',
       5.50, 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-101');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-102', 'rest-002', 'cat-101', 'Tuna Nigiri (2pcs)',
       'Bluefin tuna on sushi rice',
       6.50, 'https://images.unsplash.com/photo-1583623025817-d180a2221d0a?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-102');

-- Maki
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-103', 'rest-002', 'cat-102', 'California Roll (8pcs)',
       'Crab, avocado, cucumber and sesame',
       8.00, 'https://images.unsplash.com/photo-1617196035796-4bdb1b30634e?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-103');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-104', 'rest-002', 'cat-102', 'Spicy Tuna Roll (8pcs)',
       'Tuna, spicy mayo, cucumber and chili',
       9.50, 'https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-104');

-- Sashimi
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-105', 'rest-002', 'cat-103', 'Mixed Sashimi',
       'Assorted fresh fish slices (12 pieces)',
       18.00, 'https://images.unsplash.com/photo-1583623025817-d180a2221d0a?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-105');

-- BURGER HOUSE - Menu Items

-- Burgers
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-201', 'rest-003', 'cat-201', 'Classic Burger',
       'Angus beef, lettuce, tomato, pickles, onion and house sauce',
       12.00, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-201');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-202', 'rest-003', 'cat-201', 'Bacon Cheeseburger',
       'Angus beef, crispy bacon, cheddar cheese, BBQ sauce',
       14.50, 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-202');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-203', 'rest-003', 'cat-201', 'Veggie Burger',
       'Quinoa and black bean patty, avocado, sprouts',
       11.00, 'https://images.unsplash.com/photo-1520072959219-c595dc870360?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-203');

-- Sides
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-204', 'rest-003', 'cat-202', 'French Fries',
       'Crispy golden fries with sea salt',
       4.50, 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-204');

INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-205', 'rest-003', 'cat-202', 'Onion Rings',
       'Beer-battered onion rings',
       5.00, 'https://images.unsplash.com/photo-1639024471283-03518883512d?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-205');

-- Drinks
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, image_url, is_available, created_at, updated_at)
SELECT 'item-206', 'rest-003', 'cat-203', 'Craft Beer',
       'Local IPA on tap',
       5.50, 'https://images.unsplash.com/photo-1535958636474-b021ee887b13?w=400&h=300&fit=crop',
       1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_items WHERE id = 'item-206');

-- ========================================
 -- 5. CREATE MENU ITEM OPTIONS (if not exists)
-- ========================================

-- PIZZA OPTIONS (for Margherita, Quattro Formaggi, Diavola)

-- Extra toppings for all pizzas
INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pizza-001', 'item-003', 'Extra Mozzarella', 2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pizza-001');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pizza-002', 'item-003', 'Bordo Ripieno', 2.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pizza-002');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pizza-003', 'item-003', 'Prosciutto Crudo', 3.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pizza-003');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pizza-011', 'item-004', 'Extra Gorgonzola', 2.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pizza-011');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pizza-012', 'item-004', 'Bordo Ripieno', 2.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pizza-012');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pizza-021', 'item-005', 'Extra Piccante', 1.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pizza-021');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pizza-022', 'item-005', 'Bordo Ripieno', 2.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pizza-022');

-- PASTA OPTIONS (for Carbonara, Amatriciana)

-- Portion sizes for pasta
INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pasta-001', 'item-006', 'Porzione Piccola', -2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pasta-001');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pasta-002', 'item-006', 'Porzione Grande', 3.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pasta-002');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pasta-003', 'item-006', 'Extra Pecorino', 1.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pasta-003');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pasta-011', 'item-007', 'Porzione Piccola', -2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pasta-011');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-pasta-012', 'item-007', 'Porzione Grande', 3.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-pasta-012');

-- BURGER OPTIONS (for Classic, Bacon Cheeseburger, Veggie)

-- Cooking level for meat burgers (price_delta = 0 because it's just preference)
INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-001', 'item-201', 'Cottura: Al sangue', 0.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-001');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-002', 'item-201', 'Cottura: Media', 0.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-002');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-003', 'item-201', 'Cottura: Ben cotta', 0.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-003');

-- Extra toppings for all burgers
INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-004', 'item-201', 'Extra Bacon', 2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-004');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-005', 'item-201', 'Extra Cheese', 1.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-005');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-006', 'item-201', 'Avocado', 2.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-006');

-- Bacon Cheeseburger options
INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-011', 'item-202', 'Cottura: Al sangue', 0.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-011');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-012', 'item-202', 'Cottura: Media', 0.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-012');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-013', 'item-202', 'Cottura: Ben cotta', 0.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-013');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-014', 'item-202', 'Extra Bacon', 2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-014');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-015', 'item-202', 'Extra Cheese', 1.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-015');

-- Veggie Burger options (no cooking level needed)
INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-021', 'item-203', 'Extra Avocado', 2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-021');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-burger-022', 'item-203', 'Vegan Cheese', 1.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-burger-022');

-- SIDES OPTIONS (for French Fries)

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-sides-001', 'item-204', 'Porzione Grande', 2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-sides-001');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-sides-002', 'item-204', 'Con Cheddar e Bacon', 3.50, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-sides-002');

-- DRINKS OPTIONS (for Craft Beer)

-- Size options for craft beer
INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-drink-001', 'item-206', 'Piccola (33cl)', -1.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-drink-001');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-drink-002', 'item-206', 'Media (50cl)', 0.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-drink-002');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-drink-003', 'item-206', 'Grande (75cl)', 2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-drink-003');

-- Prosecco options (glass size)
INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-drink-011', 'item-011', 'Calice Piccolo (10cl)', -1.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-drink-011');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-drink-012', 'item-011', 'Calice Grande (15cl)', 2.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-drink-012');

INSERT INTO menu_item_options (id, menu_item_id, name, price_delta, is_multiple, created_at)
SELECT 'opt-drink-013', 'item-011', 'Bottiglia (75cl)', 20.00, 0, UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM menu_item_options WHERE id = 'opt-drink-013');

-- ========================================
-- 6. CREATE RESTAURANT TABLES (if not exists)
-- ========================================

-- La Bella Vita - Tables
INSERT INTO restaurant_tables (id, restaurant_id, code, qr_slug, is_active, created_at, updated_at)
SELECT 'table-001', 'rest-001', 'T1', 'qr-labellavita-t1', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM restaurant_tables WHERE id = 'table-001');

INSERT INTO restaurant_tables (id, restaurant_id, code, qr_slug, is_active, created_at, updated_at)
SELECT 'table-002', 'rest-001', 'T2', 'qr-labellavita-t2', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM restaurant_tables WHERE id = 'table-002');

INSERT INTO restaurant_tables (id, restaurant_id, code, qr_slug, is_active, created_at, updated_at)
SELECT 'table-003', 'rest-001', 'T3', 'qr-labellavita-t3', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM restaurant_tables WHERE id = 'table-003');

-- Sushi Tokyo - Tables
INSERT INTO restaurant_tables (id, restaurant_id, code, qr_slug, is_active, created_at, updated_at)
SELECT 'table-101', 'rest-002', 'S1', 'qr-sushitokyo-s1', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM restaurant_tables WHERE id = 'table-101');

INSERT INTO restaurant_tables (id, restaurant_id, code, qr_slug, is_active, created_at, updated_at)
SELECT 'table-102', 'rest-002', 'S2', 'qr-sushitokyo-s2', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM restaurant_tables WHERE id = 'table-102');

-- Burger House - Tables
INSERT INTO restaurant_tables (id, restaurant_id, code, qr_slug, is_active, created_at, updated_at)
SELECT 'table-201', 'rest-003', 'B1', 'qr-burgerhouse-b1', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM restaurant_tables WHERE id = 'table-201');

INSERT INTO restaurant_tables (id, restaurant_id, code, qr_slug, is_active, created_at, updated_at)
SELECT 'table-202', 'rest-003', 'B2', 'qr-burgerhouse-b2', 1, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM restaurant_tables WHERE id = 'table-202');

-- ========================================
-- VERIFICATION & SUMMARY
-- ========================================

SELECT '✅ Test data initialization completed successfully!' AS status;

SELECT
    'Users' AS entity,
    COUNT(*) AS count
FROM users
UNION ALL
SELECT
    'Restaurants',
    COUNT(*)
FROM restaurants
UNION ALL
SELECT
    'Menu Categories',
    COUNT(*)
FROM menu_categories
UNION ALL
SELECT
    'Menu Items',
    COUNT(*)
FROM menu_items
UNION ALL
SELECT
    'Menu Item Options',
    COUNT(*)
FROM menu_item_options
UNION ALL
SELECT
    'Tables',
    COUNT(*)
FROM restaurant_tables;

SELECT '=======================' AS info
UNION ALL SELECT 'Admin Credentials:'
UNION ALL SELECT 'Email: admin@gmail.com'
UNION ALL SELECT 'Password: admin2025';