package com.lastcommit.piilot.domain.filescan.dto.response;

import java.util.List;

/**
 * AI 서버 파일 스캔 배치 응답 DTO
 * 설계 문서: FILE_SERVER_SCAN_DESIGN.md - AI 서버 API 계약
 */
public record FileScanAiResponseDTO(
        List<FileResult> results
) {
    public record FileResult(
            String filePath,
            Boolean piiDetected,
            List<PiiDetail> piiDetails
    ) {
    }

    public record PiiDetail(
            String piiType,
            Integer totalCount,
            Integer maskedCount
    ) {
    }
}
