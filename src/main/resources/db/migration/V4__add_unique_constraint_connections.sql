-- =====================================================
-- PIILOT Database Migration
-- V4: 연결 테이블들에 유니크 제약 추가
-- =====================================================

-- 파일 서버 연결
ALTER TABLE file_server_connections
    ADD CONSTRAINT uk_file_server_connections_user_connection_name
    UNIQUE (user_id, connection_name);

-- DB 서버 연결
ALTER TABLE db_server_connections
    ADD CONSTRAINT uk_db_server_connections_user_connection_name
    UNIQUE (user_id, connection_name);
