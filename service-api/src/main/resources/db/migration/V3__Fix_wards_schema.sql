-- Fix wards table schema if it has wrong column name
-- This migration handles the case where database might have district_code instead of parent_code

DO $$
BEGIN
    -- Check if both columns exist (shouldn't happen, but handle it)
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public'
        AND table_name = 'wards' 
        AND column_name = 'district_code'
    ) AND EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public'
        AND table_name = 'wards' 
        AND column_name = 'parent_code'
    ) THEN
        -- Both columns exist, drop district_code and its constraint
        ALTER TABLE wards DROP CONSTRAINT IF EXISTS wards_district_code_fkey;
        ALTER TABLE wards DROP COLUMN IF EXISTS district_code;
        RAISE NOTICE 'Dropped duplicate district_code column from wards table';
    ELSIF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'public'
        AND table_name = 'wards' 
        AND column_name = 'district_code'
    ) THEN
        -- Only district_code exists, rename it to parent_code
        ALTER TABLE wards DROP CONSTRAINT IF EXISTS wards_district_code_fkey;
        ALTER TABLE wards RENAME COLUMN district_code TO parent_code;
        
        -- Recreate foreign key
        ALTER TABLE wards 
        ADD CONSTRAINT wards_parent_code_fkey 
        FOREIGN KEY (parent_code) REFERENCES districts(code);
        
        RAISE NOTICE 'Renamed district_code to parent_code in wards table';
    END IF;
END $$;

