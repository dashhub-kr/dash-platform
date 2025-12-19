-- =====================================================
-- 튜터 대화 저장 테이블
-- 멀티턴 대화 지원 및 학습 분석용
-- =====================================================

CREATE TABLE tutor_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '대화 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    session_id VARCHAR(36) NOT NULL COMMENT '세션 ID (UUID)',
    role ENUM('user', 'assistant') NOT NULL COMMENT '역할',
    content TEXT NOT NULL COMMENT '메시지 내용',
    problem_number VARCHAR(20) NULL COMMENT '관련 문제 번호',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    
    INDEX idx_user_session_time (user_id, session_id, created_at DESC),
    INDEX idx_session (session_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='AI 튜터 대화 기록';

-- 90일 이상 된 데이터 정리용 쿼리 (월 1회 배치)
-- DELETE FROM tutor_conversation WHERE created_at < DATE_SUB(NOW(), INTERVAL 90 DAY);
