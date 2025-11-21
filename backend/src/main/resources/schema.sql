CREATE TABLE IF NOT EXISTS users (

    `id`               BIGINT          NOT NULL AUTO_INCREMENT COMMENT 'PK',
    `github_id`        BIGINT          NOT NULL                COMMENT '깃허브 ID (식별용)',
    `username`         VARCHAR(255)    NOT NULL                COMMENT '사용자 이름',
    `avatar_url`       VARCHAR(1024)   NULL                    COMMENT '프로필 이미지 URL',
    `solved_ac_handle` VARCHAR(255)    NULL                    COMMENT '백준 핸들 (Solved.ac 연동)',
    `access_token`     TEXT            NULL                    COMMENT '액세스 토큰',
    `created_at`       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    `updated_at`       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    
    PRIMARY KEY (`id`),
    
    UNIQUE KEY `uk_user_github_id` (`github_id`),
    UNIQUE KEY `uk_user_solved_ac_handle` (`solved_ac_handle`)
) COMMENT '사용자 정보' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- SHOW FULL COLUMNS FROM `users`;
-- SELECT * FROM `users`;
