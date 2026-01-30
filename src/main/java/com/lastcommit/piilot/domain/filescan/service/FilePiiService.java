package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.FilePiiSearchCondition;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiFilterOptionDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiListResponseDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FilePiiSummaryResponseDTO;
import com.lastcommit.piilot.domain.filescan.entity.FileCategory;
import com.lastcommit.piilot.domain.filescan.repository.FilePiiRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileRepository;
import com.lastcommit.piilot.domain.filescan.repository.FileServerConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FilePiiService {

    private final FileRepository fileRepository;
    private final FilePiiRepository filePiiRepository;
    private final FileServerConnectionRepository fileServerConnectionRepository;

    public FilePiiSummaryResponseDTO getSummary(Long userId) {
        long totalFiles = fileRepository.countFilesWithPersonalInfo(userId);
        long highRiskFiles = fileRepository.countHighRiskFiles(userId);
        long totalFileSize = fileRepository.sumFileSizeWithPersonalInfo(userId);

        // 마스킹율 계산
        long totalPiis = filePiiRepository.sumTotalPiisCountByUserId(userId);
        long maskedPiis = filePiiRepository.sumMaskedPiisCountByUserId(userId);
        double maskingRate = totalPiis > 0 ? (double) maskedPiis / totalPiis * 100 : 0.0;

        return FilePiiSummaryResponseDTO.of(totalFiles, highRiskFiles, maskingRate, totalFileSize);
    }

    public Slice<FilePiiListResponseDTO> getFilePiiList(Long userId, FilePiiSearchCondition condition, Pageable pageable) {
        return fileRepository.searchFilePiiList(userId, condition, pageable);
    }

    public List<FilePiiFilterOptionDTO> getConnectionOptions(Long userId) {
        return fileServerConnectionRepository.findByUserIdOrderByConnectionNameAsc(userId)
                .stream()
                .map(conn -> FilePiiFilterOptionDTO.of(
                        conn.getId(),
                        conn.getConnectionName() + " (" + conn.getServerType().getName() + ")"
                ))
                .toList();
    }

    public List<FilePiiFilterOptionDTO> getFileCategoryOptions() {
        return Arrays.stream(FileCategory.values())
                .map(category -> FilePiiFilterOptionDTO.of(category.name(), category.getDisplayName()))
                .toList();
    }
}
