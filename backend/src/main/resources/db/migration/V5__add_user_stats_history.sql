-- V5: 사용자 통계 히스토리 테이블 (성장 추세 분석용)

-- 일별 스냅샷 테이블
CREATE TABLE IF NOT EXISTS user_stats_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_solved INT NOT NULL DEFAULT 0,
    solvedac_tier INT,
    solvedac_rating INT,
    solvedac_class INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_user_date (user_id, snapshot_date),
    INDEX idx_user_snapshots (user_id, snapshot_date DESC)
);

-- 태그별 히스토리 테이블
CREATE TABLE IF NOT EXISTS user_tag_stats_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    tag_key VARCHAR(50) NOT NULL,
    solved INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_snapshot_tag (snapshot_id, tag_key),
    INDEX idx_user_tag_history (user_id, tag_key, created_at DESC)
);
