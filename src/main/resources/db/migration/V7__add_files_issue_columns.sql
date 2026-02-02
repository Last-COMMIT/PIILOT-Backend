-- files 테이블에 이슈 관련 컬럼 추가
ALTER TABLE files ADD COLUMN is_issue_open BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE files ADD COLUMN total_issues_count INTEGER NOT NULL DEFAULT 0;
