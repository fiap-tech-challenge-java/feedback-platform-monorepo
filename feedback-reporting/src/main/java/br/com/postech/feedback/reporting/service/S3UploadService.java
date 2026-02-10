package br.com.postech.feedback.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@Slf4j
public class S3UploadService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${S3_BUCKET_NAME:}")
    private String bucketName;

    @Value("${aws.region:us-east-2}")
    private String region;

    @Value("${aws.s3.presigned-url-expiration-days:7}")
    private int presignedUrlExpirationDays;

    public S3UploadService(S3Client s3Client) {
        this.s3Client = s3Client;
        this.s3Presigner = null;
    }

    /**
     * Upload de conteúdo em bytes (para Excel/binários)
     */
    public String uploadReport(byte[] content, String s3Key, String contentType) {
        validateBucketConfiguration();

        log.info("Uploading report to S3 - Bucket: {}, Key: {}", bucketName, s3Key);
        log.info("Report content size: {} bytes", content.length);
        log.info("S3 Client configured - Region: {}", region);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            log.info("Initiating S3 PutObject request...");
            PutObjectResponse response = s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(content));

            String presignedUrl = generatePresignedUrl(s3Key);

            log.info("=== S3 UPLOAD SUCCESS ===");
            log.info("ETag: {}", response.eTag());
            log.info("VersionId: {}", response.versionId());
            log.info("Presigned URL generated (valid for {} days)", presignedUrlExpirationDays);
            log.info("=========================");

            return presignedUrl;
        } catch (SdkClientException e) {
            log.error("=== S3 SDK CLIENT ERROR ===");
            log.error("This usually indicates network/timeout issues");
            log.error("Bucket: {}, Key: {}", bucketName, s3Key);
            log.error("Error: {}", e.getMessage(), e);
            log.error("===========================");
            throw new RuntimeException("Failed to upload report to S3 - SDK client error", e);
        } catch (Exception e) {
            log.error("=== S3 UPLOAD FAILED ===");
            log.error("Bucket: {}, Key: {}", bucketName, s3Key);
            log.error("Error: {}", e.getMessage(), e);
            log.error("========================");
            throw new RuntimeException("Failed to upload report to S3", e);
        }
    }

    /**
     * Upload de conteúdo em String (para CSV/JSON)
     */
    public String uploadReport(String content, String s3Key, String contentType) {
        return uploadReport(content.getBytes(StandardCharsets.UTF_8), s3Key, contentType);
    }

    private void validateBucketConfiguration() {
        if (bucketName == null || bucketName.isBlank()) {
            log.error("=== S3 CONFIGURATION ERROR ===");
            log.error("Bucket name not configured! Set S3_BUCKET_NAME environment variable.");
            log.error("==============================");
            throw new IllegalStateException("S3 bucket name not configured. Set S3_BUCKET_NAME environment variable.");
        }
    }

    /**
     * Gera uma URL pré-assinada (presigned URL) para download do relatório.
     * Esta URL permite acesso temporário ao arquivo sem expor o bucket publicamente.
     */
    private String generatePresignedUrl(String s3Key) {
        try (S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build()) {

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofDays(presignedUrlExpirationDays))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("Generated presigned URL for key: {}", s3Key);
            return presignedUrl;
        }
    }
}
