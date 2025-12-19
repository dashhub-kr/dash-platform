CREATE TABLE IF NOT EXISTS studies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acorn_count INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



CREATE TABLE IF NOT EXISTS users (

	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(100) NOT NULL,
	email VARCHAR(255) NOT NULL,
	solvedac_handle VARCHAR(50) UNIQUE,
	solvedac_tier INT,
	solvedac_rating INT,
	solvedac_class INT,
	stats_last_synced_at TIMESTAMP NULL,
	created_at TIMESTAMP,
	
    provider VARCHAR(50),
	provider_id VARCHAR(255),
    avatar_url VARCHAR(512),
    study_id BIGINT,
    deleted_at TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (study_id) REFERENCES studies(id)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;



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


CREATE TABLE IF NOT EXISTS boards (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    FOREIGN KEY (user_id) REFERENCES users(id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_repositories (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    repository_name VARCHAR(255) NOT NULL,
    webhook_configured BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS tag_families (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    family_key VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    order_index INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag_key VARCHAR(50) NOT NULL UNIQUE,
    boj_tag_id INT NOT NULL UNIQUE,
    family_id BIGINT,
    parent_tag_key VARCHAR(50),
    importance_tier VARCHAR(10),
    weight DOUBLE DEFAULT 0.0,
    is_core BOOLEAN DEFAULT FALSE,
    display_names VARCHAR(100),
    FOREIGN KEY (family_id) REFERENCES tag_families(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS problems (
    problem_number VARCHAR(50) PRIMARY KEY,
    title VARCHAR(255),
    level INT,
    class INT,
    essential BOOLEAN DEFAULT FALSE,
    sprout BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS problem_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_number VARCHAR(50) NOT NULL,
    tag_key VARCHAR(50) NOT NULL,
    FOREIGN KEY (problem_number) REFERENCES problems(problem_number) ON DELETE CASCADE,
    FOREIGN KEY (tag_key) REFERENCES tags(tag_key),
    UNIQUE KEY uk_problem_tag (problem_number, tag_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Solved.ac User Statistics Tables
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

-- Phase 3: 성장 추세 분석용 테이블

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
    improvements JSON,
    refactor_provided BOOLEAN DEFAULT FALSE,
    refactor_code TEXT,
    refactor_explanation TEXT,
    score INT,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- index will be created by MySQL if it doesn't exist (handled by table def)
