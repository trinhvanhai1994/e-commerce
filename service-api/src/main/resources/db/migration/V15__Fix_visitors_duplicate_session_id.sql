-- Fix duplicate session_id in visitors table
-- Step 1: Remove duplicate records, keeping only the most recent one for each session_id
-- Using CTE to identify duplicates and keep the most recent record
WITH ranked_visitors AS (
    SELECT 
        id,
        session_id,
        ROW_NUMBER() OVER (PARTITION BY session_id ORDER BY last_visit_at DESC, id DESC) as rn
    FROM visitors
)
DELETE FROM visitors
WHERE id IN (
    SELECT id 
    FROM ranked_visitors 
    WHERE rn > 1
);

-- Step 2: Drop the existing index if it exists (will be replaced by unique constraint)
DROP INDEX IF EXISTS idx_visitors_session_id;

-- Step 3: Add unique constraint on session_id to prevent future duplicates
ALTER TABLE visitors
ADD CONSTRAINT uk_visitors_session_id UNIQUE (session_id);

