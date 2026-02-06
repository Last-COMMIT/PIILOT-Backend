package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.dto.internal.FilePiiStatsDTO;
import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FileRepositoryCustom {

    Slice<File> findFilesWithFilters(
            Long userId,
            Long connectionId,
            FileCategory category,
            Boolean masked,
            RiskLevel riskLevel,
            String keyword,
            Pageable pageable
    );

    FilePiiStatsDTO calculateStats(
            Long userId,
            Long connectionId,
            FileCategory category,
            Boolean masked,
            RiskLevel riskLevel,
            String keyword
    );

    Long countMaskedFiles(
            Long userId,
            Long connectionId,
            FileCategory category,
            Boolean masked,
            RiskLevel riskLevel,
            String keyword
    );
}
