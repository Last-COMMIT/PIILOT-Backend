-- =====================================================
-- PIILOT Database Migration
-- V4: file_server_connections 테이블에 유니크 제약 추가
-- =====================================================

ALTER TABLE file_server_connections
    ADD CONSTRAINT uk_file_server_connections_user_connection_name
    UNIQUE (user_id, connection_name);
