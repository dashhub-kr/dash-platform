CREATE TABLE IF NOT EXISTS studies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    creator_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acorn_count INT DEFAULT 0,
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    description TEXT,
    streak INT DEFAULT 0,
    active_mission_title VARCHAR(255),
    active_mission_progress INT DEFAULT 0,
    study_type VARCHAR(20) DEFAULT 'GROUP' COMMENT 'GROUP, PERSONAL'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS users (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(100) NOT NULL,
	email VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
	solvedac_handle VARCHAR(50) UNIQUE,
	solvedac_tier INT,
	solvedac_rating INT,
	solvedac_class INT,
	solved_count INT DEFAULT 0,
	stats_last_synced_at TIMESTAMP NULL,
	
    -- 랜덤 디펜스
    defense_type VARCHAR(20),
    defense_problem_id INT,
    defense_start_time TIMESTAMP NULL DEFAULT NULL,
    silver_streak INT DEFAULT 0,
    gold_streak INT DEFAULT 0,
    max_silver_streak INT DEFAULT 0,
    max_gold_streak INT DEFAULT 0,
	created_at TIMESTAMP,
    provider VARCHAR(50),
	provider_id VARCHAR(255),
    avatar_url VARCHAR(512),
    study_id BIGINT,
    equipped_decoration_id BIGINT,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (study_id) REFERENCES studies(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 모의고사 세션
CREATE TABLE IF NOT EXISTS mock_exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exam_type VARCHAR(20) NOT NULL COMMENT 'IM, A, B, SAMSUNG, KAKAO',
    problems TEXT COMMENT 'JSON array of problem IDs',
    start_time TIMESTAMP NOT NULL,
    solved_count INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS, COMPLETED, TIMEOUT, CANCELLED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_mock_exams_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 모임 가입 신청
CREATE TABLE IF NOT EXISTS study_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    message TEXT,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (study_id) REFERENCES studies(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 도토리 로그
CREATE TABLE IF NOT EXISTS acorn_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount INT NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (study_id) REFERENCES studies(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 알고리즘 기록
CREATE TABLE IF NOT EXISTS algorithm_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    study_id BIGINT,
    problem_number VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    code LONGTEXT,
    language VARCHAR(50),
    platform VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN',
    difficulty VARCHAR(50),
    runtime_ms INT,
    memory_kb INT,
    repository_name VARCHAR(255),
    file_path VARCHAR(512),
    commit_sha VARCHAR(100),
    commit_message TEXT,
    committed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    record_type VARCHAR(20) NOT NULL DEFAULT 'USER_SOLUTION' COMMENT 'USER_SOLUTION: 사용자 풀이, TOP100_PROBLEM: 탑100 문제',
    tag VARCHAR(20) DEFAULT 'GENERAL' COMMENT 'MISSION, MOCK_EXAM, DEFENSE, GENERAL',
    defense_streak INT,
    elapsed_time_seconds BIGINT NULL COMMENT '풀이 시간 (초), DEFENSE/MOCK_EXAM 태그일 때 사용',
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_algorithm_records_record_type (record_type),
    INDEX idx_algorithm_records_tag (tag),
    INDEX idx_algorithm_records_study_committed (study_id, committed_at),
    INDEX idx_algorithm_records_user_committed (user_id, committed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 게시글
CREATE TABLE IF NOT EXISTS boards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    algorithm_record_id BIGINT NULL,
    board_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL',
    like_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (algorithm_record_id) REFERENCES algorithm_records(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 사용자 저장소
CREATE TABLE IF NOT EXISTS user_repositories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    repository_name VARCHAR(255) NOT NULL,
    webhook_configured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 사용자 OAuth 토큰
CREATE TABLE IF NOT EXISTS user_oauth_tokens (
    user_id BIGINT PRIMARY KEY,
    access_token TEXT NOT NULL,
    token_type VARCHAR(50),
    expires_at TIMESTAMP NULL DEFAULT NULL,
    refresh_token TEXT,
    refresh_token_expires_at TIMESTAMP NULL DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- GitHub 푸시 이벤트
CREATE TABLE IF NOT EXISTS github_push_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    delivery_id VARCHAR(100) NOT NULL,
    repository_name VARCHAR(255) NOT NULL,
    ref VARCHAR(255) NOT NULL,
    head_commit_sha VARCHAR(100) NOT NULL,
    author_login VARCHAR(100),
    author_email VARCHAR(255),
    commit_message TEXT,
    files_json LONGTEXT,
    raw_payload LONGTEXT,
    status VARCHAR(20) NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_github_push_event_delivery (delivery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 태그 모음
CREATE TABLE IF NOT EXISTS tag_families (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    order_index INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 태그
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_key VARCHAR(50) NOT NULL UNIQUE,
    boj_tag_id INT NOT NULL UNIQUE,
    family_id BIGINT,
    parent_tag_key VARCHAR(50),
    importance_tier VARCHAR(10),
    weight DOUBLE DEFAULT 0.0,
    is_core BOOLEAN DEFAULT FALSE,
    is_basic BOOLEAN DEFAULT FALSE,  -- Whether this is a basic tag for beginners
    display_names VARCHAR(100),
    FOREIGN KEY (family_id) REFERENCES tag_families(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 태그 선수 관계 (그래프 스킬트리용)
CREATE TABLE IF NOT EXISTS tag_prerequisite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_key VARCHAR(50) NOT NULL,
    prerequisite_tag_key VARCHAR(50) NOT NULL,
    strength INT DEFAULT 1 COMMENT '1=권장, 2=필수',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tag_key) REFERENCES tags(tag_key),
    FOREIGN KEY (prerequisite_tag_key) REFERENCES tags(tag_key),
    UNIQUE KEY uk_tag_prerequisite (tag_key, prerequisite_tag_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 문제
CREATE TABLE IF NOT EXISTS problems (
    problem_number VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255),
    level INT,
    class INT,
    essential BOOLEAN DEFAULT FALSE,
    sprout BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 문제 태그
CREATE TABLE IF NOT EXISTS problem_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_number VARCHAR(50) NOT NULL,
    tag_key VARCHAR(50) NOT NULL,
    FOREIGN KEY (problem_number) REFERENCES problems(problem_number) ON DELETE CASCADE,
    FOREIGN KEY (tag_key) REFERENCES tags(tag_key),
    UNIQUE KEY uk_problem_tag (problem_number, tag_key),
    INDEX idx_problem_tags_tag (tag_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Solved.ac 사용자 통계
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
    UNIQUE    INDEX idx_user_class (user_id, class_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 성장 추세 분석용 테이블
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 태그 통계 히스토리
CREATE TABLE IF NOT EXISTS user_tag_stats_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    tag_key VARCHAR(50) NOT NULL,
    solved INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_snapshot_tag (snapshot_id, tag_key),
    INDEX idx_user_tag_history (user_id, tag_key, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 태그 통계
CREATE TABLE IF NOT EXISTS user_tag_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tag_key VARCHAR(50) NOT NULL,
    total INT NOT NULL DEFAULT 0 COMMENT 'solved.ac에 등록된 해당 태그 문제 수',
    solved INT NOT NULL DEFAULT 0 COMMENT '사용자가 푼 문제 수',
    partial INT NOT NULL DEFAULT 0 COMMENT '사용자가 부분 성공한 문제 수',
    tried INT NOT NULL DEFAULT 0 COMMENT '사용자가 시도해 본 문제 수',
    rating INT DEFAULT 0 COMMENT '태그 레이팅 점수',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_key) REFERENCES tags(tag_key),
    UNIQUE KEY uk_user_tag (user_id, tag_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- AI 코드 분석 결과 테이블
CREATE TABLE IF NOT EXISTS code_analysis_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    algorithm_record_id BIGINT NOT NULL UNIQUE,
    summary TEXT,
    time_complexity VARCHAR(50),
    space_complexity VARCHAR(50),
    complexity_explanation TEXT,
    patterns JSON,
    algorithm_intuition TEXT,
    pitfalls JSON,
    key_blocks JSON,
    improvements JSON,
    refactor_provided BOOLEAN DEFAULT FALSE,
    refactor_code TEXT,
    refactor_explanation TEXT,
    score INT,
    full_response LONGTEXT,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    counter_example_input TEXT,
    counter_example_expected TEXT,
    counter_example_predicted TEXT,
    counter_example_reason TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 게시판 댓글 테이블
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


-- 게시글 추천 테이블
CREATE TABLE IF NOT EXISTS board_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (board_id) REFERENCES boards(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_board_like (board_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 댓글 추천 테이블
CREATE TABLE IF NOT EXISTS comment_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_comment_like (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- AI 튜터 대화 테이블
CREATE TABLE IF NOT EXISTS tutor_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    session_id VARCHAR(100),
    role VARCHAR(20) NOT NULL,
    content TEXT,
    problem_number VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_tutor_session (user_id, session_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- AI 학습 경로 캐시 테이블 (일일 싱글톤)
CREATE TABLE IF NOT EXISTS learning_path_cache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    analysis_json LONGTEXT NOT NULL COMMENT 'Full LearningDashboardResponse as JSON',
    generated_at DATE NOT NULL COMMENT 'The date when this analysis was generated',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_learning_path_cache_user_date (user_id, generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 스터디 주차별 미션
CREATE TABLE IF NOT EXISTS study_missions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    week INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    problem_ids TEXT NOT NULL COMMENT 'JSON array of problem IDs',
    source_type VARCHAR(20) NOT NULL COMMENT 'AI_RECOMMENDED or MANUAL',
    deadline DATE,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (study_id) REFERENCES studies(id) ON DELETE CASCADE,
    INDEX idx_study_missions_study_week (study_id, week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 미션 제출 현황 (멤버별 문제 완료 여부)
CREATE TABLE IF NOT EXISTS study_mission_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mission_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    problem_id INT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    is_sos BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (mission_id) REFERENCES study_missions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_mission_user_problem (mission_id, user_id, problem_id),
    INDEX idx_mission_submissions_user (mission_id, user_id),
    INDEX idx_mission_submissions_mission (mission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 통합 알림 테이블
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receiver_id BIGINT NOT NULL,
    content VARCHAR(255) NOT NULL,
    url VARCHAR(512),
    related_id BIGINT,
    type VARCHAR(20) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notification_receiver (receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 친구 관계
CREATE TABLE IF NOT EXISTS friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_friendship (requester_id, receiver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 채팅
CREATE TABLE IF NOT EXISTS direct_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_dm_sender_receiver (sender_id, receiver_id),
    INDEX idx_dm_receiver_sender (receiver_id, sender_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 닉네임 치장 (이펙트)
CREATE TABLE IF NOT EXISTS decorations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    css_class VARCHAR(50) NOT NULL,
    type VARCHAR(20) DEFAULT 'PUBLIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자 보유 치장
CREATE TABLE IF NOT EXISTS user_decorations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    decoration_id BIGINT NOT NULL,
    acquired_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (decoration_id) REFERENCES decorations(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_decoration (user_id, decoration_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
