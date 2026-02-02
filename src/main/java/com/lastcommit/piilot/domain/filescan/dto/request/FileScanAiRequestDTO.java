package com.lastcommit.piilot.domain.filescan.dto.request;

import java.util.List;

/**
 * AI 서버 파일 스캔 배치 요청 DTO
 * 설계 문서: FILE_SERVER_SCAN_DESIGN.md - AI 서버 API 계약
 *
 * 요청 형식:
 * {
 *   "connectionId": "1",
 *   "piiFiles": [
 *     "documents/user_list.xlsx",
 *     "documents/contract.pdf"
 *   ]
 * }
 */
public record FileScanAiRequestDTO(
        String connectionId,
        List<String> piiFiles
) {
}
