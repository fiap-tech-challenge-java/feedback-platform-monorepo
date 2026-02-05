package br.com.postech.feedback.reporting.service;

import br.com.postech.feedback.reporting.dto.ReportReadyEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class SnsPublishService {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sns.topic-arn:}")
    private String topicArn;

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;

    /**
     * Valida a configuração do SNS no momento do uso.
     * Lança exceção com mensagem clara se as variáveis não estiverem configuradas.
     */
    private void validateConfiguration() {
        if (topicArn == null || topicArn.isBlank()) {
            log.error("=== SNS CONFIGURATION ERROR ===");
            log.error("SNS Topic ARN not configured!");
            log.error("Set SNS_TOPIC_ARN environment variable in AWS Lambda.");
            log.error("================================");
            throw new IllegalStateException("SNS Topic ARN not configured. Set SNS_TOPIC_ARN environment variable.");
        }
        if (bucketName == null || bucketName.isBlank()) {
            log.error("=== S3 CONFIGURATION ERROR ===");
            log.error("S3 Bucket name not configured!");
            log.error("Set S3_BUCKET_NAME environment variable in AWS Lambda.");
            log.error("================================");
            throw new IllegalStateException("S3 Bucket name not configured. Set S3_BUCKET_NAME environment variable.");
        }
    }

    public void publishReportReadyEvent(String reportUrl, String s3Key, LocalDateTime generatedAt,
                                         Long totalFeedbacks, Double averageScore) {
        // Validar configuração antes de usar
        validateConfiguration();
        
        log.info("Publishing ReportReady event to SNS - Topic: {}", topicArn);

        try {
            ReportReadyEvent event = ReportReadyEvent.builder()
                    .eventType("ReportReady")
                    .message("Relatório semanal disponível")
                    .reportLink(reportUrl)
                    .bucketName(bucketName)
                    .s3Key(s3Key)
                    .totalFeedbacks(totalFeedbacks)
                    .averageScore(averageScore)
                    .generatedAt(generatedAt)
                    .build();

            String messageJson = objectMapper.writeValueAsString(event);

            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(messageJson)
                    .subject("Weekly Report Ready")
                    .build();

            PublishResponse response = snsClient.publish(publishRequest);
            log.info("Report notification sent - MessageId: {}", response.messageId());
        } catch (Exception e) {
            log.error("Failed to publish event to SNS: {}", e.getMessage());
            throw new RuntimeException("Failed to publish event to SNS", e);
        }
    }
}
