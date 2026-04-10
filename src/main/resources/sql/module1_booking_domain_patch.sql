-- Module 1: booking domain/status standardization
-- Apply this script on MySQL 8 before running runtime flows that persist new deposit fields.

ALTER TABLE table_booking
    ADD COLUMN deposit_paid TINYINT(1) NOT NULL DEFAULT 0 AFTER deposit_amount,
    ADD COLUMN deposit_paid_at DATETIME NULL AFTER deposit_paid,
    ADD COLUMN is_deposit_forfeited TINYINT(1) NOT NULL DEFAULT 0 AFTER deposit_paid_at,
    ADD COLUMN deposit_txn_ref VARCHAR(255) NULL AFTER is_deposit_forfeited;

-- Optional data normalization for legacy rows
UPDATE table_booking
SET deposit_paid = 0
WHERE deposit_paid IS NULL;

UPDATE table_booking
SET is_deposit_forfeited = 0
WHERE is_deposit_forfeited IS NULL;
