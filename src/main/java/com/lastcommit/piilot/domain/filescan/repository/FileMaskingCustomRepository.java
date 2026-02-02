package com.lastcommit.piilot.domain.filescan.repository;

import com.lastcommit.piilot.domain.filescan.entity.File;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.shared.RiskLevel;

import java.util.List;

public interface FileMaskingCustomRepository {

    List<File> findIssueFilesWithFilters(
            Long userId,
            Long connectionId,
            FileCategory fileCategory,
            RiskLevel riskLevel,
            String fileName
    );
}
