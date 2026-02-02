package com.lastcommit.piilot.domain.filescan.dto.request;

import com.lastcommit.piilot.domain.filescan.entity.FileCategory;

import java.util.List;

/**
 * AI 서버 파일 스캔 배치 요청 DTO
 * 설계 문서: FILE_SERVER_SCAN_DESIGN.md - AI 서버 API 계약
 */
public record FileScanAiRequestDTO(
        String connectionId,
        List<PiiFile> piiFiles
) {
    public record PiiFile(
            String filePath,
            String fileName,
            FileCategory fileCategory,
            Boolean isEncrypted
    ) {
    }
}
