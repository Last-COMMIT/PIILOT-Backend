-- =====================================================
-- PIILOT Database Migration
-- V3: FTP/SFTP 서버 유형 추가
-- =====================================================

INSERT INTO file_server_types (name) VALUES
('FTP'),
('SFTP');
