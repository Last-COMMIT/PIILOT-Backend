package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.MaskingAiRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.MaskingAiResponseDTO;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@Component
@Profile("local")
public class StubMaskingAiServerClient implements MaskingAiServerClient {

    @Override
    public MaskingAiResponseDTO maskFile(MaskingAiRequestDTO request) {
        log.info("[Stub] Masking file: connectionId={}, filePath={}, category={}",
                request.connectionId(), request.filePath(), request.fileCategory());

        // Stub: 마스킹된 파일 시뮬레이션
        // 실제로는 AI 서버가 개인정보를 마스킹하여 반환함
        String stubMaskedContent = generateStubMaskedContent(request.fileCategory());

        return new MaskingAiResponseDTO(true, stubMaskedContent);
    }

    private String generateStubMaskedContent(FileCategory category) {
        // Stub용 마스킹된 파일 컨텐츠 생성
        // 실제 AI 서버는 원본 파일을 마스킹 처리하여 반환
        String stubContent = switch (category) {
            case PHOTO -> "[STUB] Masked photo - Face mosaic applied";
            case DOCUMENT -> "[STUB] Masked document - PII text replaced with ***";
            case AUDIO -> "[STUB] Masked audio - Beep sound applied to PII";
            case VIDEO -> "[STUB] Masked video - Face mosaic and beep applied";
        };

        return Base64.getEncoder().encodeToString(stubContent.getBytes());
    }
}
