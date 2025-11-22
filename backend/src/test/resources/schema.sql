CREATE TABLE IF NOT EXISTS users (

  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  
  provider VARCHAR(50),
  provider_id VARCHAR(255),
  avatar_url VARCHAR(512)

);
