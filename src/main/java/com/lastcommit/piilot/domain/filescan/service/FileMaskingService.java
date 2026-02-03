package com.lastcommit.piilot.domain.filescan.service;

import com.lastcommit.piilot.domain.filescan.dto.request.FileMaskingSaveRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.request.MaskingAiRequestDTO;
import com.lastcommit.piilot.domain.filescan.dto.response.*;
import com.lastcommit.piilot.domain.filescan.entity.*;
import com.lastcommit.piilot.domain.filescan.exception.FileMaskingErrorStatus;
import com.lastcommit.piilot.domain.filescan.repository.*;
import com.lastcommit.piilot.domain.shared.IssueStatus;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import com.lastcommit.piilot.global.util.AesEncryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileMaskingService {

    private static final long MAX_PREVIEW_SIZE = 20 * 1024 * 1024; // 20MB
    private static final long REDIS_TTL_MINUTES = 30;
    private static final String REDIS_KEY_PREFIX = "masking:";

    private final FileServerConnectionRepository connectionRepository;
    private final FileRepository fileRepository;
    private final FileTypeRepository fileTypeRepository;
    private final FilePiiRepository filePiiRepository;
    private final FilePiiIssueRepository filePiiIssueRepository;
    private final MaskingLogRepository maskingLogRepository;
    private final FileMaskingCustomRepository fileMaskingCustomRepository;
    private final FileDownloader fileDownloader;
    private final FileUploader fileUploader;
    private final MaskingAiServerClient maskingAiServerClient;
    private final AesEncryptor aesEncryptor;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            // PHOTO
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("heic", "image/heic"),
            // DOCUMENT
            Map.entry("pdf", "application/pdf"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("txt", "text/plain"),
            // AUDIO
            Map.entry("mp3", "audio/mpeg"),
            Map.entry("wav", "audio/wav"),
            // VIDEO
            Map.entry("mp4", "video/mp4"),
            Map.entry("avi", "video/x-msvideo"),
            Map.entry("mov", "video/quicktime")
    );

    public List<FileMaskingConnectionResponseDTO> getConnections(Long userId) {
        return connectionRepository.findByUserIdOrderByConnectionNameAsc(userId)
                .stream()
                .map(FileMaskingConnectionResponseDTO::from)
                .toList();
    }

    public List<FileMaskingFileResponseDTO> getIssueFiles(
            Long userId,
            Long connectionId,
            FileCategory fileCategory,
            com.lastcommit.piilot.domain.shared.RiskLevel riskLevel,
            String fileName
    ) {
        return fileMaskingCustomRepository.findIssueFilesWithFilters(
                userId, connectionId, fileCategory, riskLevel, fileName
        ).stream()
                .map(FileMaskingFileResponseDTO::from)
                .toList();
    }

    public FileMaskingPreviewResponseDTO getFilePreview(Long userId, Long fileId) {
        File file = getFileWithAccessCheck(userId, fileId);
        String mimeType = getMimeType(file.getFileType().getExtension());

        if (file.getFileSize() >= MAX_PREVIEW_SIZE) {
            return FileMaskingPreviewResponseDTO.unavailable(
                    file, mimeType,
                    "파일 크기가 20MB를 초과하여 미리보기를 지원하지 않습니다."
            );
        }

        FileServerConnection connection = file.getConnection();
        String decryptedPassword = aesEncryptor.decrypt(connection.getEncryptedPassword());

        byte[] fileContent = fileDownloader.download(connection, decryptedPassword, file.getFilePath());
        String base64Content = Base64.getEncoder().encodeToString(fileContent);

        return FileMaskingPreviewResponseDTO.available(file, mimeType, base64Content);
    }

    public FileMaskingMaskResponseDTO maskFile(Long userId, Long fileId) {
        File file = getFileWithAccessCheck(userId, fileId);
        validateMaskingTarget(file);

        String mimeType = getMimeType(file.getFileType().getExtension());
        String maskedFileName = generateMaskedFileName(file.getName());
        String redisKey = buildRedisKey(userId, fileId);

        // Redis 캐시 확인
        String cachedContent = (String) redisTemplate.opsForValue().get(redisKey);
        if (cachedContent != null) {
            log.info("Cache hit for masking result: userId={}, fileId={}", userId, fileId);
            return FileMaskingMaskResponseDTO.available(file, maskedFileName, mimeType, cachedContent);
        }

        // AI 서버 마스킹 요청
        log.info("Cache miss, calling AI server for masking: userId={}, fileId={}", userId, fileId);
        MaskingAiRequestDTO aiRequest = MaskingAiRequestDTO.of(
                file.getConnection().getId(),
                file.getFilePath(),
                file.getFileType().getType()
        );

        MaskingAiResponseDTO aiResponse = maskingAiServerClient.maskFile(aiRequest);

        if (!Boolean.TRUE.equals(aiResponse.success()) || aiResponse.maskedFileBase64() == null) {
            throw new GeneralException(FileMaskingErrorStatus.MASKING_PROCESS_FAILED);
        }

        // Redis에 캐시 저장
        redisTemplate.opsForValue().set(redisKey, aiResponse.maskedFileBase64(), REDIS_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Masking result cached: userId={}, fileId={}, TTL={}min", userId, fileId, REDIS_TTL_MINUTES);

        // 파일 크기 확인 (마스킹된 파일)
        byte[] maskedBytes = Base64.getDecoder().decode(aiResponse.maskedFileBase64());
        if (maskedBytes.length >= MAX_PREVIEW_SIZE) {
            return FileMaskingMaskResponseDTO.unavailable(
                    file, maskedFileName, mimeType,
                    "마스킹된 파일 크기가 20MB를 초과하여 미리보기를 지원하지 않습니다."
            );
        }

        return FileMaskingMaskResponseDTO.available(file, maskedFileName, mimeType, aiResponse.maskedFileBase64());
    }

    @Transactional
    public FileMaskingSaveResponseDTO saveMaskedFile(Long userId, Long fileId, FileMaskingSaveRequestDTO request) {
        File file = getFileWithAccessCheck(userId, fileId);
        validateMaskingTarget(file);

        String redisKey = buildRedisKey(userId, fileId);

        // Redis에서 마스킹 결과 조회
        String maskedContentBase64 = (String) redisTemplate.opsForValue().get(redisKey);
        if (maskedContentBase64 == null) {
            throw new GeneralException(FileMaskingErrorStatus.MASKING_RESULT_EXPIRED);
        }

        FileServerConnection connection = file.getConnection();
        String decryptedPassword = aesEncryptor.decrypt(connection.getEncryptedPassword());

        // 1. 원본 파일 다운로드
        byte[] originalContent = fileDownloader.download(connection, decryptedPassword, file.getFilePath());

        // 2. 원본 파일을 ZIP으로 암호화
        String originalFilePath = file.getFilePath();
        String zipFilePath = changeExtensionToZip(originalFilePath);
        byte[] encryptedZip = createEncryptedZip(originalContent, file.getName(), request.encryptionPassword());

        // 3. ZIP 파일 업로드 (원본 삭제 전에 먼저 업로드하여 데이터 유실 방지)
        fileUploader.upload(connection, decryptedPassword, zipFilePath, encryptedZip);

        // 4. 마스킹 파일 업로드 (실패 시 ZIP 파일 보상 삭제)
        byte[] maskedContent = Base64.getDecoder().decode(maskedContentBase64);
        String maskedFilePath = generateMaskedFilePath(originalFilePath);
        try {
            fileUploader.upload(connection, decryptedPassword, maskedFilePath, maskedContent);
        } catch (Exception e) {
            // 보상 처리: 마스킹 파일 업로드 실패 시 이미 업로드된 ZIP 파일 삭제
            log.error("Masked file upload failed, rolling back ZIP upload: {}", e.getMessage());
            try {
                fileUploader.delete(connection, decryptedPassword, zipFilePath);
            } catch (Exception rollbackEx) {
                log.error("Failed to rollback ZIP file: {}", rollbackEx.getMessage());
            }
            throw e;
        }

        // 5. 원본 파일 삭제 (모든 업로드 성공 후에만 삭제)
        fileUploader.delete(connection, decryptedPassword, originalFilePath);

        // 6. DB 업데이트 - 파일 경로 및 타입을 ZIP으로 변경
        FileType zipFileType = fileTypeRepository.findByExtension("zip")
                .orElseThrow(() -> new GeneralException(FileMaskingErrorStatus.FILE_UPLOAD_FAILED));
        file.updateToEncryptedZip(zipFilePath, zipFileType);

        // FilePii 업데이트 - 모든 PII를 마스킹됨으로 처리
        List<FilePii> filePiis = filePiiRepository.findByFileId(fileId);
        for (FilePii filePii : filePiis) {
            filePii.markAllAsMasked();
        }

        // FilePiiIssue 해결 처리
        filePiiIssueRepository.findByFileIdAndIssueStatus(fileId, IssueStatus.ACTIVE)
                .ifPresent(issue -> issue.resolve(LocalDateTime.now()));

        // MaskingLog 생성
        MaskingLog maskingLog = MaskingLog.builder()
                .file(file)
                .connection(connection)
                .originalFilePath(zipFilePath)
                .maskedFilePath(maskedFilePath)
                .performedAt(LocalDateTime.now())
                .build();
        maskingLogRepository.save(maskingLog);

        // 7. Redis 키 삭제
        redisTemplate.delete(redisKey);
        log.info("Masking saved and cache deleted: userId={}, fileId={}", userId, fileId);

        return FileMaskingSaveResponseDTO.of(fileId, zipFilePath, maskedFilePath);
    }

    private File getFileWithAccessCheck(Long userId, Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new GeneralException(FileMaskingErrorStatus.FILE_NOT_FOUND));

        if (!file.getConnection().getUser().getId().equals(userId)) {
            throw new GeneralException(FileMaskingErrorStatus.FILE_ACCESS_DENIED);
        }

        return file;
    }

    private void validateMaskingTarget(File file) {
        if (!Boolean.TRUE.equals(file.getHasPersonalInfo()) ||
                Boolean.TRUE.equals(file.getIsEncrypted()) ||
                !Boolean.TRUE.equals(file.getIsIssueOpen())) {
            throw new GeneralException(FileMaskingErrorStatus.NOT_MASKING_TARGET);
        }
    }

    private String getMimeType(String extension) {
        return MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }

    private String generateMaskedFileName(String originalName) {
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            return originalName.substring(0, dotIndex) + "_masked" + originalName.substring(dotIndex);
        }
        return originalName + "_masked";
    }

    private String generateMaskedFilePath(String originalPath) {
        int dotIndex = originalPath.lastIndexOf('.');
        if (dotIndex > 0) {
            return originalPath.substring(0, dotIndex) + "_masked" + originalPath.substring(dotIndex);
        }
        return originalPath + "_masked";
    }

    private String changeExtensionToZip(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex > 0) {
            return filePath.substring(0, dotIndex) + ".zip";
        }
        return filePath + ".zip";
    }

    private String buildRedisKey(Long userId, Long fileId) {
        return REDIS_KEY_PREFIX + userId + ":" + fileId;
    }

    private byte[] createEncryptedZip(byte[] content, String fileName, String password) {
        Path tempDir = null;
        Path tempFile = null;
        Path tempZip = null;

        try {
            // 임시 디렉토리 생성
            tempDir = Files.createTempDirectory("masking");
            tempFile = tempDir.resolve(fileName);
            tempZip = tempDir.resolve("encrypted.zip");

            // 원본 파일 임시 저장
            Files.write(tempFile, content);

            // ZIP 파일 생성 (비밀번호 암호화)
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionLevel(CompressionLevel.NORMAL);
            zipParameters.setEncryptFiles(true);
            zipParameters.setEncryptionMethod(EncryptionMethod.AES);

            try (ZipFile zipFile = new ZipFile(tempZip.toFile(), password.toCharArray())) {
                zipFile.addFile(tempFile.toFile(), zipParameters);
            }

            // ZIP 파일 내용 반환
            return Files.readAllBytes(tempZip);

        } catch (Exception e) {
            log.error("Failed to create encrypted ZIP: {}", e.getMessage());
            throw new GeneralException(FileMaskingErrorStatus.MASKING_PROCESS_FAILED);
        } finally {
            // 임시 파일 정리
            try {
                if (tempFile != null) Files.deleteIfExists(tempFile);
                if (tempZip != null) Files.deleteIfExists(tempZip);
                if (tempDir != null) Files.deleteIfExists(tempDir);
            } catch (Exception e) {
                log.warn("Failed to cleanup temp files: {}", e.getMessage());
            }
        }
    }
}
