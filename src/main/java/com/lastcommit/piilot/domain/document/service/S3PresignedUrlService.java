package com.lastcommit.piilot.domain.document.service;

import com.lastcommit.piilot.domain.document.dto.request.PresignedUrlRequestDTO;
import com.lastcommit.piilot.domain.document.dto.response.PresignedUrlResponseDTO;
import com.lastcommit.piilot.domain.document.entity.DocumentType;
import com.lastcommit.piilot.domain.document.exception.DocumentErrorStatus;
import com.lastcommit.piilot.global.config.S3Properties;
import com.lastcommit.piilot.global.error.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public PresignedUrlResponseDTO generatePresignedUrl(Long userId, PresignedUrlRequestDTO request) {
        try {
            String s3Key = generateS3Key(userId, request.fileName(), request.documentType());
            String s3Url = generateS3Url(s3Key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucketName())
                    .key(s3Key)
                    .contentType(request.contentType())
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(s3Properties.presignedUrlExpirationMinutes()))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

            log.info("Presigned URL 생성 완료 - userId={}, s3Key={}", userId, s3Key);

            return new PresignedUrlResponseDTO(
                    presignedRequest.url().toString(),
                    s3Key,
                    s3Url,
                    s3Properties.presignedUrlExpirationMinutes()
            );
        } catch (Exception e) {
            log.error("Presigned URL 생성 실패 - userId={}, fileName={}: {}",
                    userId, request.fileName(), e.getMessage(), e);
            throw new GeneralException(DocumentErrorStatus.S3_PRESIGNED_URL_ERROR);
        }
    }

    private String generateS3Key(Long userId, String fileName, DocumentType documentType) {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String typePrefix = documentType.name().toLowerCase();
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedFileName = sanitizeFileName(fileName);

        return String.format("documents/%s/%s/%d/%s_%s",
                typePrefix, datePrefix, userId, uniqueId, sanitizedFileName);
    }

    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Properties.bucketName(), s3Properties.region(), s3Key);
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9가-힣._-]", "_");
    }
}
