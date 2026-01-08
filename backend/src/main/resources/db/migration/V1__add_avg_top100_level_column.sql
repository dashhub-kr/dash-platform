-- V1__add_avg_top100_level_column.sql
-- Adds avg_top100_level column to users table for bubble detection

ALTER TABLE users 
ADD COLUMN avg_top100_level INT DEFAULT NULL 
COMMENT 'Top 100 문제 평균 레벨 (거품 탐지용)';
