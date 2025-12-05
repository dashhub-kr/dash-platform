-- 로컬 환경에서만 실행되는 스키마 초기화 스크립트
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS github_push_event;
DROP TABLE IF EXISTS user_oauth_tokens;
DROP TABLE IF EXISTS user_repositories;
DROP TABLE IF EXISTS algorithm_records;
DROP TABLE IF EXISTS boards;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;
