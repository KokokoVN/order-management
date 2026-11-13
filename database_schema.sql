-- ========================================================
-- CƠ SỞ DỮ LIỆU QUẢN LÝ ĐƠN HÀNG (order_management)
-- Hệ quản trị cơ sở dữ liệu: MySQL 8.x
-- ========================================================

CREATE DATABASE IF NOT EXISTS order_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE order_management;

-- 1. Bảng users – Tài khoản người dùng
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'user') NOT NULL DEFAULT 'user'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng code_values – Giá trị mã
CREATE TABLE IF NOT EXISTS code_values (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code_value VARCHAR(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng orders – Đơn hàng
CREATE TABLE IF NOT EXISTS orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    order_code VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at DATETIME NULL,
    status ENUM('da_tao', 'da_dat', 'da_nhan_hang') NOT NULL DEFAULT 'da_tao',
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng order_details – Chi tiết đơn hàng
CREATE TABLE IF NOT EXISTS order_details (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_name TEXT NOT NULL,
    variant VARCHAR(1000) NULL,
    quantity INT NOT NULL,
    code_value_id INT NOT NULL,
    CONSTRAINT chk_quantity_gt_zero CHECK (quantity > 0),
    CONSTRAINT fk_details_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_details_code_value FOREIGN KEY (code_value_id) REFERENCES code_values(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- DỮ LIỆU KHỞI TẠO MẪU (Sample Data)
-- ========================================================

-- Mật khẩu mặc định: admin123 và user123 (đã băm bằng BCrypt)
-- admin: $2a$10$wKx5K89eC8.61JzR87m.bO4yG3t8Fm1Wj/4o4.Y3t1E2Q3.g/S6e.
-- user:  $2a$10$f3F82q7s9yT5V1Q9B8jH6eG2p5R8Y1wK3m6L0/A1E4C7.i/T8h.
INSERT INTO users (id, name, password, role) VALUES
(1, 'admin', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'admin'),
(2, 'user', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'user')
ON DUPLICATE KEY UPDATE name=name;

-- Giá trị mã mẫu theo yêu cầu

