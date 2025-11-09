-- Fix districts table schema if it has wrong column name
-- This migration handles the case where database might have province_code instead of parent_code

DO $$
BEGIN
    -- Check if both columns exist (shouldn't happen, but handle it)
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public'
        AND table_name = 'districts' 
        AND column_name = 'province_code'
    ) AND EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public'
        AND table_name = 'districts' 
        AND column_name = 'parent_code'
    ) THEN
        -- Both columns exist, drop province_code and its constraint
        ALTER TABLE districts DROP CONSTRAINT IF EXISTS districts_province_code_fkey;
        ALTER TABLE districts DROP COLUMN IF EXISTS province_code;
        RAISE NOTICE 'Dropped duplicate province_code column from districts table';
    ELSIF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public'
        AND table_name = 'districts' 
        AND column_name = 'province_code'
    ) THEN
        -- Only province_code exists, rename it to parent_code
        ALTER TABLE districts DROP CONSTRAINT IF EXISTS districts_province_code_fkey;
        ALTER TABLE districts RENAME COLUMN province_code TO parent_code;
        
        -- Recreate foreign key
        ALTER TABLE districts 
        ADD CONSTRAINT districts_parent_code_fkey 
        FOREIGN KEY (parent_code) REFERENCES provinces(code);
        
        RAISE NOTICE 'Renamed province_code to parent_code in districts table';
    END IF;
END $$;

