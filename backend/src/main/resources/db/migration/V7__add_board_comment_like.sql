-- V7: 게시판 기능 확장 (Board 확장 + Comment + Like 테이블)

-- 1. boards 테이블 확장
ALTER TABLE boards
    ADD COLUMN algorithm_record_id BIGINT NULL AFTER user_id,
    ADD COLUMN board_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL' AFTER algorithm_record_id,
    ADD COLUMN like_count INT NOT NULL DEFAULT 0 AFTER board_type,
    ADD CONSTRAINT fk_boards_algorithm_record 
        FOREIGN KEY (algorithm_record_id) REFERENCES algorithm_records(id) ON DELETE SET NULL;

-- 2. comments 테이블 생성
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT NULL COMMENT 'null이면 최상위 댓글, 있으면 대댓글 (1단계만 허용)',
    line_number INT NULL COMMENT 'null이면 일반 댓글, 숫자면 코드 라인 댓글',
    content TEXT NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,
    INDEX idx_comments_board (board_id),
    INDEX idx_comments_board_line (board_id, line_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. board_likes 테이블 생성
CREATE TABLE IF NOT EXISTS board_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_board_like (board_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. comment_likes 테이블 생성
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_comment_like (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
