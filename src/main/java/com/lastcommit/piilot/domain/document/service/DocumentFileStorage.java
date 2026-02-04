package com.lastcommit.piilot.domain.document.service;

import com.lastcommit.piilot.domain.document.exception.DocumentErrorStatus;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
public class DocumentFileStorage {

    private final Path uploadDir;

    public DocumentFileStorage(@Value("${file.upload.document-path}") String uploadPath) {
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        createDirectoryIfNotExists();
    }

    public String store(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetPath = uploadDir.resolve(storedFilename).normalize();

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 저장 완료: {}", targetPath);
            return storedFilename;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalFilename, e);
            throw new GeneralException(DocumentErrorStatus.FILE_STORAGE_FAILED);
        }
    }

    public void delete(String storedFilename) {
        try {
            Path filePath = uploadDir.resolve(storedFilename).normalize();
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", filePath);
        } catch (IOException e) {
            log.warn("파일 삭제 실패 (무시): {}", storedFilename, e);
        }
    }

    private void createDirectoryIfNotExists() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리 생성 실패: " + uploadDir, e);
        }
    }
}
