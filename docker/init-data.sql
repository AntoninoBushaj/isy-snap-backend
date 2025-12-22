-- Script di inizializzazione per TableSnap
-- Popola il database con dati di test rispettando i vincoli JPA

USE tablesnap;

-- Crea le tabelle se non esistono ancora
CREATE TABLE IF NOT EXISTS restaurants (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image VARCHAR(512),
    logo VARCHAR(512),
    address VARCHAR(255),
    phone VARCHAR(64),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS menu_items (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    image VARCHAR(512),
    category VARCHAR(255) NOT NULL,
    available TINYINT(1) NOT NULL DEFAULT 1,
    vegetarian TINYINT(1) NOT NULL DEFAULT 0,
    spicy TINYINT(1) NOT NULL DEFAULT 0,
    preparation_time INT,
    restaurant_id VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_menu_items_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    id VARCHAR(64) PRIMARY KEY,
    restaurant_id VARCHAR(64) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    status VARCHAR(32) NOT NULL,
    special_instructions TEXT,
    estimated_delivery_time INT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_orders_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL,
    menu_item_id VARCHAR(64) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    id VARCHAR(64) PRIMARY KEY,
    order_id VARCHAR(64) NOT NULL UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    method VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    transaction_id VARCHAR(128),
    processed_at DATETIME(6),
    error_message TEXT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Svuota le tabelle mantenendo l'ordine delle FK
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM payments;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM menu_items;
DELETE FROM restaurants;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- RISTORANTI
-- ============================================
INSERT INTO restaurants (id, name, description, image, logo, address, phone, created_at, updated_at) VALUES
('rest-neo-001', 'TableSnap Bistro',
 'Bistrò contemporaneo con cucina stagionale e delivery veloce.',
 'https://images.unsplash.com/photo-1550966871-3ed3cdb5ed0c',
 'https://via.placeholder.com/96x96/e67e22/ffffff?text=TS',
 'Via Brera 12, Milano',
 '+39 02 1234568',
 NOW(), NOW()),
('rest-neo-002', 'Green Delight Vegan Kitchen',
 'Cucina plant-based creativa con ingredienti biologici certificati.',
 'https://images.unsplash.com/photo-1493770348161-369560ae357d',
 'https://via.placeholder.com/96x96/27ae60/ffffff?text=GD',
 'Corso Duca 55, Torino',
 '+39 011 4567890',
 NOW(), NOW()),
('rest-neo-003', 'Urban Brew Coffee House',
 'Specialty coffee shop con brunch artigianale e bakery interna.',
 'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085',
 'https://via.placeholder.com/96x96/2980b9/ffffff?text=UB',
 'Via Ostiense 101, Roma',
 '+39 06 7654321',
 NOW(), NOW());

-- ============================================
-- MENU ITEMS
-- ============================================

-- TableSnap Bistro
INSERT INTO menu_items (id, name, description, price, image, category, available, vegetarian, spicy, preparation_time, restaurant_id, created_at, updated_at) VALUES
('menu-ts-001', 'Rigatoni al Ragù',
 'Pasta tirata a mano con ragù di manzo a lenta cottura.',
 13.50, 'https://images.unsplash.com/photo-1540189549336-e6e99c3679fe',
 'Primi', true, false, false, 14, 'rest-neo-001', NOW(), NOW()),
('menu-ts-002', 'Gnocchi ai Funghi Porcini',
 'Gnocchi di patate fatti in casa con crema di porcini e timo.',
 12.00, 'https://images.unsplash.com/photo-1525755662778-989d0524087e',
 'Primi', true, true, false, 16, 'rest-neo-001', NOW(), NOW()),
('menu-ts-003', 'Pollo al Limone',
 'Petto di pollo marinato al limone e rosmarino con verdure croccanti.',
 15.00, 'https://images.unsplash.com/photo-1452195100486-9cc805987862',
 'Secondi', true, false, false, 18, 'rest-neo-001', NOW(), NOW()),
('menu-ts-004', 'Tiramisù Classico',
 'Ricetta tradizionale con mascarpone fresco e cacao amaro.',
 6.00, 'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17',
 'Dolci', true, true, false, 6, 'rest-neo-001', NOW(), NOW()),
('menu-ts-005', 'Spritz Veneziano',
 'Aperitivo con Aperol, prosecco e soda servito con fetta d\'arancia.',
 7.50, 'https://images.unsplash.com/photo-1504674900247-0877df9cc836',
 'Cocktail', true, true, false, 3, 'rest-neo-001', NOW(), NOW());

-- Green Delight Vegan Kitchen
INSERT INTO menu_items (id, name, description, price, image, category, available, vegetarian, spicy, preparation_time, restaurant_id, created_at, updated_at) VALUES
('menu-gd-001', 'Buddha Bowl Mediterranea',
 'Quinoa, hummus al basilico, olive taggiasche e verdure al forno.',
 11.00, 'https://images.unsplash.com/photo-1504674900247-0877df9cc836',
 'Bowls', true, true, false, 12, 'rest-neo-002', NOW(), NOW()),
('menu-gd-002', 'Burger di Ceci e Spinaci',
 'Pane multicereali con burger di ceci, salsa tahina e microgreens.',
 10.50, 'https://images.unsplash.com/photo-1608039829574-78524f79c4c7',
 'Burger', true, true, false, 15, 'rest-neo-002', NOW(), NOW()),
('menu-gd-003', 'Zuppa di Lenticchie Rosse',
 'Brodo vegetale, zenzero e latte di cocco per un comfort food speziato.',
 9.00, 'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17',
 'Zuppe', true, true, true, 20, 'rest-neo-002', NOW(), NOW()),
('menu-gd-004', 'Cheesecake Vegana al Mango',
 'Base raw di mandorle e datteri con crema al mango.',
 6.50, 'https://images.unsplash.com/photo-1499636136210-6f4ee915583e',
 'Dolci', true, true, false, 5, 'rest-neo-002', NOW(), NOW()),
('menu-gd-005', 'Kombucha allo Zenzero',
 'Fermentato artigianale con zenzero fresco e limone.',
 4.50, 'https://images.unsplash.com/photo-1470337458703-46ad1756a187',
 'Bevande', true, true, true, 2, 'rest-neo-002', NOW(), NOW());

-- Urban Brew Coffee House
INSERT INTO menu_items (id, name, description, price, image, category, available, vegetarian, spicy, preparation_time, restaurant_id, created_at, updated_at) VALUES
('menu-ub-001', 'Cold Brew Nitro',
 'Estratto a freddo con azoto servito on tap.',
 4.00, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93',
 'Bevande', true, true, false, 2, 'rest-neo-003', NOW(), NOW()),
('menu-ub-002', 'Cappuccino Oat Milk',
 'Miscela single origin con latte di avena micro-foam.',
 3.50, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93',
 'Bevande', true, true, false, 4, 'rest-neo-003', NOW(), NOW()),
('menu-ub-003', 'Croissant Integrale',
 'Sfoglia al burro francese con farina macinata a pietra.',
 2.20, 'https://images.unsplash.com/photo-1512058564366-18510be2db19',
 'Bakery', true, true, false, 8, 'rest-neo-003', NOW(), NOW()),
('menu-ub-004', 'Avocado Toast',
 'Pane lievito madre con avocado, lime e peperoncino in fiocchi.',
 7.80, 'https://images.unsplash.com/photo-1482049016688-2d3e1b311543',
 'Brunch', true, true, true, 10, 'rest-neo-003', NOW(), NOW()),
('menu-ub-005', 'Granola Bowl Yogurt',
 'Yogurt greco, granola alla nocciola e frutti di bosco.',
 6.50, 'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17',
 'Brunch', true, true, false, 7, 'rest-neo-003', NOW(), NOW());

-- ============================================
-- ORDINI DI TEST
-- ============================================
INSERT INTO orders (id, restaurant_id, subtotal, tax, total_price, status, special_instructions, estimated_delivery_time, created_at, updated_at) VALUES
('order-test-001', 'rest-neo-001', 25.50, 2.55, 28.05, 'CONFIRMED', 'Poco pepe sul ragù.', 25, NOW(), NOW()),
('order-test-002', 'rest-neo-001', 45.00, 4.50, 49.50, 'PREPARING', NULL, 30, NOW(), NOW()),
('order-test-003', 'rest-neo-002', 20.00, 2.00, 22.00, 'DELIVERED', 'Niente cipolla cruda.', 35, NOW(), NOW()),
('order-test-004', 'rest-neo-003', 19.30, 1.93, 21.23, 'CANCELLED', 'Cliente ha richiamato per annullare.', 15, NOW(), NOW());

-- ============================================
-- ITEMS ORDINI
-- ============================================
INSERT INTO order_items (menu_item_id, order_id, quantity, price, subtotal, created_at) VALUES
('menu-ts-001', 'order-test-001', 1, 13.50, 13.50, NOW()),
('menu-ts-004', 'order-test-001', 2, 6.00, 12.00, NOW()),
('menu-ts-003', 'order-test-002', 2, 15.00, 30.00, NOW()),
('menu-ts-005', 'order-test-002', 2, 7.50, 15.00, NOW()),
('menu-gd-001', 'order-test-003', 1, 11.00, 11.00, NOW()),
('menu-gd-005', 'order-test-003', 2, 4.50, 9.00, NOW()),
('menu-ub-001', 'order-test-004', 2, 4.00, 8.00, NOW()),
('menu-ub-002', 'order-test-004', 1, 3.50, 3.50, NOW()),
('menu-ub-004', 'order-test-004', 1, 7.80, 7.80, NOW());

-- ============================================
-- PAGAMENTI
-- ============================================
INSERT INTO payments (id, order_id, amount, method, status, transaction_id, processed_at, error_message, created_at, updated_at) VALUES
('pay-test-001', 'order-test-001', 28.05, 'CARD', 'SUCCESS', 'txn_ts_conf_01', NOW(), NULL, NOW(), NOW()),
('pay-test-002', 'order-test-002', 49.50, 'CARD', 'PROCESSING', 'txn_ts_proc_01', NULL, NULL, NOW(), NOW()),
('pay-test-003', 'order-test-003', 22.00, 'UPI', 'SUCCESS', 'txn_gd_succ_01', NOW(), NULL, NOW(), NOW()),
('pay-test-004', 'order-test-004', 21.23, 'WALLET', 'REFUNDED', 'txn_ub_ref_01', NOW(), 'Ordine annullato su richiesta cliente.', NOW(), NOW());

-- ============================================
-- REPORT RAPIDO
-- ============================================
SELECT 'Database di test popolato.' AS status;
SELECT COUNT(*) AS ristoranti FROM restaurants;
SELECT COUNT(*) AS menu_items FROM menu_items;
SELECT COUNT(*) AS ordini FROM orders;
SELECT COUNT(*) AS pagamenti FROM payments;
