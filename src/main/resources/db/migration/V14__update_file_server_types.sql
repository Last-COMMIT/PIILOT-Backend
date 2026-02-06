-- =====================================================
-- PIILOT Database Migration
-- V14: 파일 서버 유형 정리 (FTP, SFTP, WEBDAV만 유지)
-- =====================================================

-- 1. file_server_connections의 기존 type_id를 임시 값으로 변경 (충돌 방지)
-- 기존: FTP(4), SFTP(5), WEBDAV(6) → 임시: 104, 105, 106
UPDATE file_server_connections SET server_type_id = 104 WHERE server_type_id = 4;
UPDATE file_server_connections SET server_type_id = 105 WHERE server_type_id = 5;
UPDATE file_server_connections SET server_type_id = 106 WHERE server_type_id = 6;

-- 2. Local, NAS, S3 참조하는 연결 삭제 (사용하지 않는 유형)
DELETE FROM file_server_connections WHERE server_type_id IN (1, 2, 3);

-- 3. 외래키 제약 조건 임시 비활성화
ALTER TABLE file_server_connections DROP CONSTRAINT IF EXISTS fk_file_conn_server_type;

-- 4. 기존 file_server_types 데이터 모두 삭제
DELETE FROM file_server_types;

-- 5. 시퀀스 리셋
ALTER SEQUENCE file_server_types_id_seq RESTART WITH 1;

-- 6. FTP, SFTP, WEBDAV 순서로 재삽입 (ID: 1, 2, 3)
INSERT INTO file_server_types (id, name) VALUES
                                             (1, 'FTP'),
                                             (2, 'SFTP'),
                                             (3, 'WEBDAV');

-- 7. 시퀀스를 4부터 시작하도록 설정
ALTER SEQUENCE file_server_types_id_seq RESTART WITH 4;

-- 8. file_server_connections의 type_id를 새 ID로 업데이트
-- 임시: 104→1(FTP), 105→2(SFTP), 106→3(WEBDAV)
UPDATE file_server_connections SET server_type_id = 1 WHERE server_type_id = 104;
UPDATE file_server_connections SET server_type_id = 2 WHERE server_type_id = 105;
UPDATE file_server_connections SET server_type_id = 3 WHERE server_type_id = 106;

-- 9. 외래키 제약 조건 다시 활성화
ALTER TABLE file_server_connections
    ADD CONSTRAINT fk_file_conn_server_type
        FOREIGN KEY (server_type_id) REFERENCES file_server_types(id);
