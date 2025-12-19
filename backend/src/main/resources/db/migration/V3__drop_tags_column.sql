-- Remove redundant tags column from problems table
ALTER TABLE problems DROP COLUMN IF EXISTS tags;
