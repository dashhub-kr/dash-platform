-- Add missing columns to problems table
ALTER TABLE problems
ADD COLUMN IF NOT EXISTS level INT AFTER title,
ADD COLUMN IF NOT EXISTS class INT AFTER level,
ADD COLUMN IF NOT EXISTS essential BOOLEAN DEFAULT FALSE AFTER class,
ADD COLUMN IF NOT EXISTS sprout BOOLEAN DEFAULT FALSE AFTER essential;

-- If tier column exists from old schema, drop it
-- ALTER TABLE problems DROP COLUMN IF EXISTS tier;
