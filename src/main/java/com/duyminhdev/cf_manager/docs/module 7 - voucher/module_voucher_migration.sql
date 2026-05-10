-- ============================================================
-- Module: Voucher
-- Phiên bản an toàn (có thể chạy lại nhiều lần)
-- Yêu cầu: MySQL 8.0
-- ============================================================

DELIMITER //

-- 1. Tạo bảng voucher nếu chưa có
CREATE TABLE IF NOT EXISTS voucher (
    id             INT            NOT NULL AUTO_INCREMENT,
    code           VARCHAR(50)    NOT NULL UNIQUE COMMENT 'Mã voucher (không phân biệt hoa/thường khi validate)',
    description    VARCHAR(500),
    discount_type  ENUM('PERCENT','FIXED') NOT NULL COMMENT 'Loại giảm giá',
    discount_value DECIMAL(18,2)  NOT NULL COMMENT 'Giá trị giảm (% hoặc số tiền cố định)',
    min_order_amount DECIMAL(18,2) COMMENT 'Đơn tối thiểu để áp dụng. NULL = không giới hạn',
    max_discount   DECIMAL(18,2)  COMMENT 'Trần giảm tối đa (chỉ dùng với PERCENT). NULL = không giới hạn',
    usage_limit    INT            COMMENT 'Số lượt tối đa. NULL = không giới hạn',
    used_count     INT            NOT NULL DEFAULT 0 COMMENT 'Số lượt đã sử dụng (tăng bằng native UPDATE atomic)',
    start_date     DATETIME       COMMENT 'Ngày bắt đầu hiệu lực. NULL = hiệu lực ngay',
    end_date       DATETIME       COMMENT 'Ngày hết hạn. NULL = không hết hạn',
    is_active      TINYINT(1)     NOT NULL DEFAULT 1,
    created_by     INT            COMMENT 'Người tạo (account_id)',
    created_at     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version        BIGINT         NOT NULL DEFAULT 0 COMMENT 'Optimistic lock version',
    PRIMARY KEY (id),
    CONSTRAINT fk_voucher_created_by FOREIGN KEY (created_by) REFERENCES account(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tạo bảng voucher_usage nếu chưa có
CREATE TABLE IF NOT EXISTS voucher_usage (
    id             INT            NOT NULL AUTO_INCREMENT,
    voucher_id     INT            NOT NULL,
    dish_order_id  INT            NOT NULL,
    account_id     INT            NOT NULL COMMENT 'Nhân viên thực hiện thanh toán',
    discount_amount DECIMAL(18,2) NOT NULL COMMENT 'Số tiền thực tế được giảm',
    used_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_voucher_order (voucher_id, dish_order_id),
    KEY idx_vu_dish_order (dish_order_id),
    CONSTRAINT fk_vu_voucher_id    FOREIGN KEY (voucher_id)    REFERENCES voucher(id),
    CONSTRAINT fk_vu_dish_order_id FOREIGN KEY (dish_order_id) REFERENCES dish_order(id),
    CONSTRAINT fk_vu_account_id    FOREIGN KEY (account_id)    REFERENCES account(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Thêm cột vào dish_order (nếu chưa có)
-- Sử dụng INFORMATION_SCHEMA để kiểm tra
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish_order' AND COLUMN_NAME = 'voucher_id';
IF @col_exists = 0 THEN
    ALTER TABLE dish_order ADD COLUMN voucher_id INT NULL COMMENT 'Voucher đã áp dụng (FK)';
END IF;

SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish_order' AND COLUMN_NAME = 'discount_amount';
IF @col_exists = 0 THEN
    ALTER TABLE dish_order ADD COLUMN discount_amount DECIMAL(18,2) NULL COMMENT 'Số tiền được giảm';
END IF;

SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish_order' AND COLUMN_NAME = 'final_total';
IF @col_exists = 0 THEN
    ALTER TABLE dish_order ADD COLUMN final_total DECIMAL(18,2) NULL COMMENT 'Tiền khách thực trả = total_bill - discount_amount';
END IF;

-- Thêm chỉ mục cho voucher_id (nếu chưa có)
SELECT COUNT(*) INTO @idx_exists FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dish_order' AND INDEX_NAME = 'idx_dish_order_voucher_id';
IF @idx_exists = 0 THEN
    ALTER TABLE dish_order ADD INDEX idx_dish_order_voucher_id (voucher_id);
END IF;

-- Thêm foreign key (nếu chưa có)
SELECT COUNT(*) INTO @fk_exists FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'dish_order' AND CONSTRAINT_NAME = 'fk_dish_order_voucher_id';
IF @fk_exists = 0 THEN
    ALTER TABLE dish_order ADD CONSTRAINT fk_dish_order_voucher_id FOREIGN KEY (voucher_id) REFERENCES voucher(id) ON DELETE SET NULL;
END IF;

-- 4. Thêm cột vào invoice (tương tự)
SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'invoice' AND COLUMN_NAME = 'voucher_id';
IF @col_exists = 0 THEN
    ALTER TABLE invoice ADD COLUMN voucher_id INT NULL COMMENT 'FK tới voucher (để truy vết chính xác)';
END IF;

SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'invoice' AND COLUMN_NAME = 'voucher_code';
IF @col_exists = 0 THEN
    ALTER TABLE invoice ADD COLUMN voucher_code VARCHAR(50) NULL COMMENT 'Snapshot mã voucher để hiển thị nhanh';
END IF;

SELECT COUNT(*) INTO @col_exists FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'invoice' AND COLUMN_NAME = 'discount_amount';
IF @col_exists = 0 THEN
    ALTER TABLE invoice ADD COLUMN discount_amount DECIMAL(18,2) NULL COMMENT 'Số tiền được giảm';
END IF;

-- Chỉ mục
SELECT COUNT(*) INTO @idx_exists FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'invoice' AND INDEX_NAME = 'idx_invoice_voucher_id';
IF @idx_exists = 0 THEN
    ALTER TABLE invoice ADD INDEX idx_invoice_voucher_id (voucher_id);
END IF;

-- Foreign key
SELECT COUNT(*) INTO @fk_exists FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS
WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'invoice' AND CONSTRAINT_NAME = 'fk_invoice_voucher_id';
IF @fk_exists = 0 THEN
    ALTER TABLE invoice ADD CONSTRAINT fk_invoice_voucher_id FOREIGN KEY (voucher_id) REFERENCES voucher(id) ON DELETE SET NULL;
END IF;

DELIMITER ;


-- ============================================================
-- Dữ liệu mẫu cho module Voucher
-- Yêu cầu: đã chạy script tạo bảng trước đó
-- ============================================================

-- 1. Xóa dữ liệu voucher cũ (nếu có) để chạy lại sạch sẽ
DELETE FROM voucher_usage;
DELETE FROM voucher;

-- Reset AUTO_INCREMENT (tuỳ chọn)
ALTER TABLE voucher AUTO_INCREMENT = 1;
ALTER TABLE voucher_usage AUTO_INCREMENT = 1;

-- 2. Insert dữ liệu voucher
INSERT INTO voucher (code, description, discount_type, discount_value, min_order_amount, max_discount, usage_limit, used_count, start_date, end_date, is_active, created_by)
VALUES
('SUMMER21', 'Giảm 20% tối đa 50k cho đơn từ 100k', 'PERCENT', 20.00, 100000.00, 50000.00, 100, 2, '2026-05-01 00:00:00', '2026-06-30 23:59:59', 1, 1),
('WELCOME10', 'Giảm thẳng 10.000đ cho đơn từ 50k', 'FIXED', 10000.00, 50000.00, NULL, NULL, 1, '2026-05-01 00:00:00', NULL, 1, 1),
('FREESHIP', 'Giảm 15% không giới hạn (đã hết hạn)', 'PERCENT', 15.00, NULL, NULL, 200, 0, '2026-04-01 00:00:00', '2026-04-30 23:59:59', 0, 1),
('HETHANG', 'Giảm 50.000đ, giới hạn 1 lượt (đã dùng hết)', 'FIXED', 50000.00, NULL, NULL, 1, 1, '2026-05-01 00:00:00', NULL, 1, 1),
('TESTNULL', 'Giảm 10% không giới hạn, không ràng buộc', 'PERCENT', 10.00, NULL, NULL, NULL, 0, NULL, NULL, 1, 1);

-- 3. Insert lịch sử sử dụng voucher (voucher_usage)
-- Gắn voucher cho một số dish_order đã thanh toán (có trong DB hiện tại)
-- Lưu ý: Sử dụng dish_order_id có sẵn, đảm bảo tổng tiền phù hợp
INSERT INTO voucher_usage (voucher_id, dish_order_id, account_id, discount_amount, used_at)
VALUES
-- SUMMER21 (id=1) đã dùng 2 lần
(1, 108, 1, 40800, '2026-04-22 08:25:00'),   -- Order 108, total 204000, discount 20% = 40800
(1, 118, 1, 50000, '2026-04-22 15:51:00'),   -- Order 118, total 271000, discount 20% nhưng max 50000

-- WELCOME10 (id=2) dùng 1 lần
(2, 109, 73, 10000, '2026-04-22 09:54:00'),  -- Order 109, total 138000, fixed 10000

-- HETHANG (id=4) dùng 1 lần (hết lượt)
(4, 110, 73, 50000, '2026-04-22 09:54:00'),  -- Order 110, total 138000, fixed 50000

-- TESTNULL (id=5) dùng 1 lần cho order 120
(5, 120, 73, 23200, '2026-04-23 03:50:00');   -- Order 120, total 232000, discount 10% = 23200

-- 4. Cập nhật dish_order: gắn voucher_id, discount_amount, final_total
UPDATE dish_order
SET
    voucher_id = 1,
    discount_amount = 40800,
    final_total = total_bill - 40800
WHERE id = 108;

UPDATE dish_order
SET
    voucher_id = 1,
    discount_amount = 50000,
    final_total = total_bill - 50000
WHERE id = 118;

UPDATE dish_order
SET
    voucher_id = 2,
    discount_amount = 10000,
    final_total = total_bill - 10000
WHERE id = 109;

UPDATE dish_order
SET
    voucher_id = 4,
    discount_amount = 50000,
    final_total = total_bill - 50000
WHERE id = 110;

UPDATE dish_order
SET
    voucher_id = 5,
    discount_amount = 23200,
    final_total = total_bill - 23200
WHERE id = 120;

-- 5. Cập nhật invoice tương ứng (chỉ những hóa đơn đã tồn tại)
-- Order 118 → invoice 75, Order 120 → invoice 74
UPDATE invoice
SET
    voucher_id = 1,
    voucher_code = 'SUMMER21',
    discount_amount = 50000
WHERE id = 75;

UPDATE invoice
SET
    voucher_id = 5,
    voucher_code = 'TESTNULL',
    discount_amount = 23200
WHERE id = 74;

-- (Các order 108, 109, 110 không có invoice tương ứng nên không cần cập nhật)