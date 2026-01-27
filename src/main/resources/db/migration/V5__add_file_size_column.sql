-- files 테이블에 파일 크기 컬럼 추가
ALTER TABLE files ADD COLUMN file_size BIGINT NOT NULL DEFAULT 0;
