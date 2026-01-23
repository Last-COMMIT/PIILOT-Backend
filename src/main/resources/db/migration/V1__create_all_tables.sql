-- =====================================================
-- PIILOT Database Schema
-- V1: 모든 테이블 생성
-- =====================================================

-- =====================================================
-- 1. 독립 테이블 (FK 없음)
-- =====================================================

-- 사용자
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- DBMS 유형
CREATE TABLE dbms_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    jdbc_prefix VARCHAR(255) NOT NULL,
    default_port INTEGER NOT NULL,
    driver_class_name VARCHAR(255) NOT NULL,
    test_query VARCHAR(255) NOT NULL
);

-- 파일 서버 유형
CREATE TABLE file_server_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- 개인정보 유형
CREATE TABLE pii_types (
    id SERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    risk_weight FLOAT NOT NULL
);

-- 파일 유형
CREATE TABLE file_type (
    id SERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    extension VARCHAR(30) NOT NULL
);


-- =====================================================
-- 3. 1단계 의존 테이블
-- =====================================================

-- 공지사항
CREATE TABLE notices (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notices_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notices_user_id ON notices(user_id);

-- 문서
CREATE TABLE document (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_document_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_document_user_id ON document(user_id);

-- DB 서버 연결
CREATE TABLE db_server_connections (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    dbms_type_id INTEGER NOT NULL,
    connection_name VARCHAR(100) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    db_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    encrypted_password VARCHAR(255) NOT NULL,
    manager_name VARCHAR(100) NOT NULL,
    manager_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DISCONNECTED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_db_conn_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_db_conn_dbms_type FOREIGN KEY (dbms_type_id) REFERENCES dbms_types(id)
);

CREATE INDEX idx_db_conn_user_id ON db_server_connections(user_id);

-- 파일 서버 연결
CREATE TABLE file_server_connections (
    id BIGSERIAL PRIMARY KEY,
    server_type_id INTEGER NOT NULL,
    user_id BIGINT NOT NULL,
    connection_name VARCHAR(100) NOT NULL,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    default_path VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL,
    encrypted_password VARCHAR(255) NOT NULL,
    manager_name VARCHAR(100) NOT NULL,
    manager_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DISCONNECTED',
    retention_period_months INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_conn_server_type FOREIGN KEY (server_type_id) REFERENCES file_server_types(id),
    CONSTRAINT fk_file_conn_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_file_conn_user_id ON file_server_connections(user_id);


-- =====================================================
-- 4. 2단계 의존 테이블
-- =====================================================

-- DB 스캔 이력
CREATE TABLE db_scan_history (
    id BIGSERIAL PRIMARY KEY,
    db_server_connection_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    scan_start_time TIMESTAMP NOT NULL,
    scan_end_time TIMESTAMP,
    total_tables_count BIGINT NOT NULL DEFAULT 0,
    total_columns_count BIGINT NOT NULL DEFAULT 0,
    scanned_columns_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_db_scan_conn FOREIGN KEY (db_server_connection_id) REFERENCES db_server_connections(id)
);

CREATE INDEX idx_db_scan_conn_id ON db_scan_history(db_server_connection_id);

-- DB 테이블
CREATE TABLE db_tables (
    id BIGSERIAL PRIMARY KEY,
    db_server_connection_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    total_columns BIGINT NOT NULL DEFAULT 0,
    last_change_signature VARCHAR(255),
    last_scanned_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_db_tables_conn FOREIGN KEY (db_server_connection_id) REFERENCES db_server_connections(id)
);

CREATE INDEX idx_db_tables_conn_id ON db_tables(db_server_connection_id);

-- 파일 스캔 이력
CREATE TABLE file_scan_history (
    id BIGSERIAL PRIMARY KEY,
    file_server_connection_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    scan_start_time TIMESTAMP NOT NULL,
    scan_end_time TIMESTAMP,
    total_files_count BIGINT NOT NULL DEFAULT 0,
    total_files_size BIGINT NOT NULL DEFAULT 0,
    scanned_files_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_scan_conn FOREIGN KEY (file_server_connection_id) REFERENCES file_server_connections(id)
);

CREATE INDEX idx_file_scan_conn_id ON file_scan_history(file_server_connection_id);

-- 파일
CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    connection_id BIGINT NOT NULL,
    file_type_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    has_personal_info BOOLEAN NOT NULL DEFAULT FALSE,
    risk_level VARCHAR(10),
    last_modified_time TIMESTAMP NOT NULL,
    last_scanned_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_files_conn FOREIGN KEY (connection_id) REFERENCES file_server_connections(id),
    CONSTRAINT fk_files_type FOREIGN KEY (file_type_id) REFERENCES file_type(id)
);

CREATE INDEX idx_files_conn_id ON files(connection_id);


-- =====================================================
-- 5. 3단계 의존 테이블
-- =====================================================

-- DB 개인정보 컬럼
CREATE TABLE db_pii_columns (
    id BIGSERIAL PRIMARY KEY,
    table_id BIGINT NOT NULL,
    pii_type_id INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    total_records_count BIGINT,
    enc_records_count BIGINT,
    unenc_records_key TEXT,
    risk_level VARCHAR(10),
    total_issues_count INTEGER,
    is_issue_open BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_db_pii_col_table FOREIGN KEY (table_id) REFERENCES db_tables(id),
    CONSTRAINT fk_db_pii_col_type FOREIGN KEY (pii_type_id) REFERENCES pii_types(id)
);

CREATE INDEX idx_db_pii_col_table_id ON db_pii_columns(table_id);

-- 파일 개인정보
CREATE TABLE file_pii (
    id BIGSERIAL PRIMARY KEY,
    type_id INTEGER NOT NULL,
    file_id BIGINT NOT NULL,
    total_piis_count INTEGER,
    masked_piis_count INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_pii_type FOREIGN KEY (type_id) REFERENCES pii_types(id),
    CONSTRAINT fk_file_pii_file FOREIGN KEY (file_id) REFERENCES files(id)
);

CREATE INDEX idx_file_pii_file_id ON file_pii(file_id);

-- 마스킹 로그
CREATE TABLE masking_logs (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT,
    connection_id BIGINT,
    original_file_path VARCHAR(1000) NOT NULL,
    masked_file_path VARCHAR(1000) NOT NULL,
    performed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_masking_file FOREIGN KEY (file_id) REFERENCES files(id),
    CONSTRAINT fk_masking_conn FOREIGN KEY (connection_id) REFERENCES file_server_connections(id)
);

CREATE INDEX idx_masking_file_id ON masking_logs(file_id);


-- =====================================================
-- 6. 4단계 의존 테이블
-- =====================================================

-- DB 개인정보 이슈
CREATE TABLE db_pii_issues (
    id BIGSERIAL PRIMARY KEY,
    column_id BIGINT NOT NULL,
    connection_id BIGINT,
    user_status VARCHAR(20) NOT NULL DEFAULT 'ISSUE',
    issue_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_db_issue_column FOREIGN KEY (column_id) REFERENCES db_pii_columns(id),
    CONSTRAINT fk_db_issue_conn FOREIGN KEY (connection_id) REFERENCES db_server_connections(id)
);

CREATE INDEX idx_db_issue_column_id ON db_pii_issues(column_id);

-- 파일 개인정보 이슈
CREATE TABLE file_pii_issues (
    id BIGSERIAL PRIMARY KEY,
    file_id BIGINT NOT NULL,
    connection_id BIGINT,
    user_status VARCHAR(20) NOT NULL DEFAULT 'ISSUE',
    issue_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    detected_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_file_issue_file FOREIGN KEY (file_id) REFERENCES files(id),
    CONSTRAINT fk_file_issue_conn FOREIGN KEY (connection_id) REFERENCES file_server_connections(id)
);

CREATE INDEX idx_file_issue_file_id ON file_pii_issues(file_id);


-- =====================================================
-- 7. 트리거 함수 (updated_at 자동 갱신)
-- =====================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 각 테이블에 트리거 적용
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_notices_updated_at BEFORE UPDATE ON notices FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_document_updated_at BEFORE UPDATE ON document FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_db_conn_updated_at BEFORE UPDATE ON db_server_connections FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_file_conn_updated_at BEFORE UPDATE ON file_server_connections FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_db_scan_updated_at BEFORE UPDATE ON db_scan_history FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_db_tables_updated_at BEFORE UPDATE ON db_tables FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_file_scan_updated_at BEFORE UPDATE ON file_scan_history FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_files_updated_at BEFORE UPDATE ON files FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_db_pii_col_updated_at BEFORE UPDATE ON db_pii_columns FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_file_pii_updated_at BEFORE UPDATE ON file_pii FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_masking_updated_at BEFORE UPDATE ON masking_logs FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_db_issue_updated_at BEFORE UPDATE ON db_pii_issues FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_file_issue_updated_at BEFORE UPDATE ON file_pii_issues FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
