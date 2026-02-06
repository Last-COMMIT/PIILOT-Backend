-- db_pii_columns 테이블에 key_column 컬럼 추가
-- 암호화 체크에 사용된 PK 컬럼명을 저장하여 이슈 상세 조회 시 동일한 컬럼으로 데이터 조회

ALTER TABLE db_pii_columns ADD COLUMN key_column VARCHAR(100);
