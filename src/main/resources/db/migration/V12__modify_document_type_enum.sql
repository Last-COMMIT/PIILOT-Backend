-- V12: Modify document type enum
-- LAWS, INTERNAL_REGULATIONS → REGULATION
-- DB_INFO → DB_DICTIONARY

-- Update existing records
UPDATE document SET type = 'REGULATION' WHERE type IN ('LAWS', 'INTERNAL_REGULATIONS');
UPDATE document SET type = 'DB_DICTIONARY' WHERE type = 'DB_INFO';

-- Drop existing check constraint if any
ALTER TABLE document DROP CONSTRAINT IF EXISTS document_type_check;

-- Add new check constraint
ALTER TABLE document ADD CONSTRAINT document_type_check
    CHECK (type IN ('REGULATION', 'DB_DICTIONARY'));
