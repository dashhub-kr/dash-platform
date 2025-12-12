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
);

-- Index handled by unique constraint
