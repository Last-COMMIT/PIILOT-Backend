package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.FileScanAiRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.FileScanAiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Profile("stub")
public class StubFileAiServerClient implements FileAiServerClient {

    @Override
    public FileScanAiResponseDTO scanFiles(FileScanAiRequestDTO request) {
        log.debug("Stub AI scanning {} files for connectionId={}",
                request.piiFiles().size(), request.connectionId());

        List<FileScanAiResponseDTO.FileResult> results = new ArrayList<>();

        for (String filePath : request.piiFiles()) {
            FileScanAiResponseDTO.FileResult result = scanSingleFile(filePath);
            results.add(result);
        }

        return new FileScanAiResponseDTO(results);
    }

    private FileScanAiResponseDTO.FileResult scanSingleFile(String filePath) {
        log.debug("Stub AI scanning file: {}", filePath);

        List<FileScanAiResponseDTO.PiiDetail> piiDetails = new ArrayList<>();
        String fileName = extractFileName(filePath).toLowerCase();
        String extension = extractExtension(filePath).toLowerCase();

        // Simulate PII detection based on file name keywords
        if (fileName.contains("employee") || fileName.contains("staff") || fileName.contains("직원")) {
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("NM", 15, 10));
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("PH", 12, 8));
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("EM", 10, 7));
        }

        if (fileName.contains("customer") || fileName.contains("client") || fileName.contains("고객")) {
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("NM", 50, 30));
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("ADD", 45, 25));
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("PH", 48, 28));
        }

        if (fileName.contains("account") || fileName.contains("payment") || fileName.contains("계좌")) {
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("ACN", 20, 15));
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("NM", 18, 14));
        }

        if (fileName.contains("passport") || fileName.contains("여권")) {
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("PP", 5, 3));
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("NM", 5, 3));
        }

        if (fileName.contains("id") || fileName.contains("주민")) {
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("RRN", 8, 5));
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("NM", 8, 5));
        }

        // Image files - simulate face detection
        if (isImageExtension(extension)) {
            if (fileName.contains("profile") || fileName.contains("photo") ||
                fileName.contains("사진") || fileName.contains("증명")) {
                piiDetails.add(new FileScanAiResponseDTO.PiiDetail("FACE", 1, 0));
            }
        }

        // Default: add some random PII for demonstration if file name doesn't match
        if (piiDetails.isEmpty() && !fileName.contains("template") && !fileName.contains("sample")) {
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("NM", 5, 3));
            piiDetails.add(new FileScanAiResponseDTO.PiiDetail("PH", 3, 2));
        }

        boolean piiDetected = !piiDetails.isEmpty();

        return new FileScanAiResponseDTO.FileResult(filePath, piiDetected, piiDetails);
    }

    private String extractFileName(String filePath) {
        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    private String extractExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        return lastDot >= 0 ? filePath.substring(lastDot + 1) : "";
    }

    private boolean isImageExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") ||
               extension.equals("png") || extension.equals("heic");
    }
}
