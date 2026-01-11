ALTER TABLE battle_participant ADD COLUMN solved_problem_ids VARCHAR(255) DEFAULT '[]' COMMENT '해결한 문제 ID 목록 (JSON)';
