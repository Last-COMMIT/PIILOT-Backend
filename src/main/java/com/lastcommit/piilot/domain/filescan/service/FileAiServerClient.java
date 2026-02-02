package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.FileScanAiRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanAiResponseDTO;

public interface FileAiServerClient {

    /**
     * 배치로 파일 스캔 요청
     * @param request 스캔할 파일 목록을 포함한 배치 요청
     * @return 모든 파일의 스캔 결과
     */
    FileScanAiResponseDTO scanFiles(FileScanAiRequestDTO request);
}
