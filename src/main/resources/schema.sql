-- 혹시 기존 테이블이 있다면 날리고 새로 시작 (개발 단계용)
DROP TABLE IF EXISTS user_entity;

-- 테이블 생성
CREATE TABLE user_entity (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);
