-- Module 7: repository/query optimization indexes
-- Apply on MySQL 8.

SET @schema_name = DATABASE();

SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'table_booking'
      AND index_name = 'idx_tb_table_status_arrive'
);
SET @sql_stmt = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_tb_table_status_arrive ON table_booking (dining_table_id, booking_status, expected_arrive_time)',
    'SELECT ''idx_tb_table_status_arrive already exists'''
);
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'table_booking'
      AND index_name = 'idx_tb_active_status_arrive'
);
SET @sql_stmt = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_tb_active_status_arrive ON table_booking (is_active, booking_status, expected_arrive_time)',
    'SELECT ''idx_tb_active_status_arrive already exists'''
);
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = @schema_name
      AND table_name = 'table_booking'
      AND index_name = 'idx_tb_table_arrive_status'
);
SET @sql_stmt = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_tb_table_arrive_status ON table_booking (dining_table_id, expected_arrive_time, booking_status)',
    'SELECT ''idx_tb_table_arrive_status already exists'''
);
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
