-- =====================================================
-- PIILOT Database Initial Data
-- V2: 기본 데이터 삽입
-- =====================================================

-- =====================================================
-- 1. DBMS 유형 (dbms_types)
-- =====================================================
INSERT INTO dbms_types (name, jdbc_prefix, default_port, driver_class_name, test_query) VALUES
('MySQL', 'jdbc:mysql://', 3306, 'com.mysql.cj.jdbc.Driver', 'SELECT 1'),
('PostgreSQL', 'jdbc:postgresql://', 5432, 'org.postgresql.Driver', 'SELECT 1'),
('Oracle', 'jdbc:oracle:thin:@', 1521, 'oracle.jdbc.OracleDriver', 'SELECT 1 FROM DUAL');


-- =====================================================
-- 2. 파일 서버 유형 (file_server_types)
-- =====================================================
INSERT INTO file_server_types (name) VALUES
('Local'),
('NAS'),
('S3');


-- =====================================================
-- 3. 개인정보 유형 (pii_types)
-- =====================================================
INSERT INTO pii_types (type, risk_weight) VALUES
('NM', 0.3),      -- 이름: 낮은 위험도
('RRN', 1.0),    -- 주민등록번호: 최고 위험도
('ADD', 0.5),    -- 주소: 중간 위험도
('IP', 0.4),     -- IP 주소: 중간 위험도
('PH', 0.6),     -- 전화번호: 중간-높음 위험도
('ACN', 0.9),    -- 계좌번호: 높은 위험도
('PP', 0.8),     -- 여권번호: 높은 위험도
('EM', 0.4),     -- 이메일: 중간 위험도
('FACE', 0.7);   -- 얼굴: 높은 위험도


-- =====================================================
-- 4. 파일 유형 (file_type)
-- =====================================================

-- 문서 (DOCUMENT)
INSERT INTO file_type (type, extension) VALUES
('DOCUMENT', 'txt'),
('DOCUMENT', 'pdf'),
('DOCUMENT', 'docx');

-- 오디오 (AUDIO)
INSERT INTO file_type (type, extension) VALUES
('AUDIO', 'mp3'),
('AUDIO', 'wav');

-- 사진 (PHOTO)
INSERT INTO file_type (type, extension) VALUES
('PHOTO', 'jpg'),
('PHOTO', 'png'),
('PHOTO', 'jpeg'),
('PHOTO', 'heic');

-- 비디오 (VIDEO)
INSERT INTO file_type (type, extension) VALUES
('VIDEO', 'mp4'),
('VIDEO', 'avi'),
('VIDEO', 'mov');
