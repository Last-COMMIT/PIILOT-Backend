package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.dto.request.FilePiiSearchCondition;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiListResponseDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FileRepositoryCustom {

    Slice<FilePiiListResponseDTO> searchFilePiiList(Long userId, FilePiiSearchCondition condition, Pageable pageable);

    long countFilesWithPersonalInfo(Long userId);

    long countHighRiskFiles(Long userId);

    long sumFileSizeWithPersonalInfo(Long userId);
}
