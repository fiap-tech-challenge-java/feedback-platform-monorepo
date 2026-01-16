package br.com.postech.feedback.reporting.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class S3UploadService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    public S3UploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadReport(String content, String s3Key, String contentType) {
        log.info("Uploading report to S3 - Bucket: {}, Key: {}", bucketName, s3Key);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(content.getBytes(StandardCharsets.UTF_8)));

            String reportUrl = generateS3Url(s3Key);
            log.info("Report uploaded successfully to: {}", reportUrl);
            return reportUrl;
        } catch (Exception e) {
            log.error("Failed to upload report to S3: {}", e.getMessage());
            throw new RuntimeException("Failed to upload report to S3", e);
        }
    }

    private String generateS3Url(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }
}
