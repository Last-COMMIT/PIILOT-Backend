-- DB 서버 연결에 스캔 상태 컬럼 추가
ALTER TABLE db_server_connections ADD COLUMN is_scanning BOOLEAN NOT NULL DEFAULT FALSE;

-- 파일 서버 연결에 스캔 상태 컬럼 추가
ALTER TABLE file_server_connections ADD COLUMN is_scanning BOOLEAN NOT NULL DEFAULT FALSE;
