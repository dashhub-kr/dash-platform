SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE IF NOT EXISTS battle (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type ENUM('MOCK_EXAM', 'DEFENSE') NOT NULL,
    creator_id BIGINT NOT NULL,
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    problem_ids JSON NOT NULL COMMENT '배틀에 사용되는 문제 ID 목록',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at DATETIME,
    completed_at DATETIME,
    
    KEY idx_battle_creator (creator_id),
    KEY idx_battle_status (status),
    FOREIGN KEY (creator_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS battle_participant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    battle_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status ENUM('INVITED', 'ACCEPTED', 'DECLINED', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'INVITED',
    score INT DEFAULT 0 COMMENT '획득 점수',
    problems_solved INT DEFAULT 0 COMMENT '푼 문제 수',
    total_time_seconds BIGINT DEFAULT 0 COMMENT '총 소요 시간',
    started_at DATETIME,
    completed_at DATETIME,
    
    KEY idx_battle_participant_user (user_id),
    KEY idx_battle_participant_status (status),
    FOREIGN KEY (battle_id) REFERENCES battle(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id),
    UNIQUE KEY uk_battle_user (battle_id, user_id)
);

SET FOREIGN_KEY_CHECKS=1;
