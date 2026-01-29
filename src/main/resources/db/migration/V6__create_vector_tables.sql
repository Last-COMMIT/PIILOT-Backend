-- V6: pgvector 확장 및 벡터 테이블 생성
-- AI 서버에서 사용하는 벡터 검색용 테이블

-- 1. pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. PII 표준단어 테이블 (PII 컬럼 탐지용)
CREATE TABLE IF NOT EXISTS pii_standard_words (
    id SERIAL PRIMARY KEY,
    chunk_text TEXT NOT NULL,
    embedding VECTOR(1024),
    abbr VARCHAR(30),
    korean VARCHAR(50),
    english VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pii_embedding ON pii_standard_words USING hnsw (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_pii_abbr ON pii_standard_words(abbr);

-- 3. 법령 데이터 테이블 (법령 검색용)
CREATE TABLE IF NOT EXISTS law_data (
    id SERIAL PRIMARY KEY,
    chunk_text TEXT NOT NULL,
    embedding VECTOR(1024),
    document_title VARCHAR(500),
    law_name VARCHAR(200),
    article VARCHAR(100),
    page INTEGER,
    effective_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_law_embedding ON law_data USING hnsw (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_law_name ON law_data(law_name);

COMMENT ON TABLE pii_standard_words IS 'PII 컬럼 탐지를 위한 표준단어 벡터 테이블';
COMMENT ON TABLE law_data IS '법령 검색을 위한 법령 데이터 벡터 테이블';
