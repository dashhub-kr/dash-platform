CREATE TABLE IF NOT EXISTS users (

  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  
  provider VARCHAR(50),
  provider_id VARCHAR(255),
  avatar_url VARCHAR(512)

);

CREATE TABLE IF NOT EXISTS user_repositories (

  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  repository_name VARCHAR(255) NOT NULL,
  webhook_configured BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

);

CREATE TABLE IF NOT EXISTS user_oauth_tokens (

  user_id BIGINT PRIMARY KEY,
  access_token TEXT NOT NULL,
  token_type VARCHAR(50),
  expires_at TIMESTAMP,
  refresh_token TEXT,
  refresh_token_expires_at TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

);

CREATE TABLE IF NOT EXISTS github_push_event (

  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  delivery_id VARCHAR(100) NOT NULL,
  repository_name VARCHAR(255) NOT NULL,
  ref VARCHAR(255) NOT NULL,
  head_commit_sha VARCHAR(100) NOT NULL,
  author_login VARCHAR(100),
  author_email VARCHAR(255),
  commit_message CLOB,
  files_json CLOB,
  raw_payload CLOB,
  status VARCHAR(20) NOT NULL,
  failure_reason CLOB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (delivery_id)

);
