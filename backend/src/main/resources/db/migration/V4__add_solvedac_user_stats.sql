-- V4: Add Solved.ac user statistics schema
-- This migration adds support for Solved.ac user statistics integration

-- 1. Extend users table with Solved.ac fields
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS solvedac_handle VARCHAR(50) UNIQUE AFTER email,
ADD COLUMN IF NOT EXISTS solvedac_tier INT AFTER solvedac_handle,
ADD COLUMN IF NOT EXISTS solvedac_rating INT AFTER solvedac_tier,
ADD COLUMN IF NOT EXISTS solvedac_class INT AFTER solvedac_rating,
ADD COLUMN IF NOT EXISTS stats_last_synced_at TIMESTAMP NULL AFTER solvedac_class;

CREATE INDEX IF NOT EXISTS idx_users_solvedac_handle ON users(solvedac_handle);

-- 2. Create user_class_stats table
CREATE TABLE IF NOT EXISTS user_class_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    class_number INT NOT NULL,
    total INT NOT NULL DEFAULT 0 COMMENT 'solved.ac에 등록된 해당 클래스의 문제 수',
    total_solved INT NOT NULL DEFAULT 0 COMMENT '사용자가 푼 클래스 문제 수',
    essentials INT NOT NULL DEFAULT 0 COMMENT 'solved.ac에 등록된 해당 클래스의 에센셜 문제 수',
    essential_solved INT NOT NULL DEFAULT 0 COMMENT '사용자가 푼 클래스 에센셜 문제 수',
    decoration VARCHAR(20) COMMENT '획득한 클래스 치장 (bronze/silver/gold 등)',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_class (user_id, class_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_user_class_stats_user ON user_class_stats(user_id);

-- 3. Create user_tag_stats table
CREATE TABLE IF NOT EXISTS user_tag_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tag_key VARCHAR(50) NOT NULL,
    total INT NOT NULL DEFAULT 0 COMMENT 'solved.ac에 등록된 해당 태그 문제 수',
    solved INT NOT NULL DEFAULT 0 COMMENT '사용자가 푼 문제 수',
    partial INT NOT NULL DEFAULT 0 COMMENT '사용자가 부분 성공한 문제 수',
    tried INT NOT NULL DEFAULT 0 COMMENT '사용자가 시도해 본 문제 수',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_key) REFERENCES tags(tag_key),
    UNIQUE KEY uk_user_tag (user_id, tag_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX IF NOT EXISTS idx_user_tag_stats_user ON user_tag_stats(user_id);
CREATE INDEX IF NOT EXISTS idx_user_tag_stats_tag ON user_tag_stats(tag_key);
