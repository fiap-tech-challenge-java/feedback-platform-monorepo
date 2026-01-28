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

    @Value("${aws.sns.topic-arn:arn:aws:sns:us-east-2:990227772490:feedback-notifications}")
    private String topicArn;

    @Value("${aws.s3.bucket-name:feedback-reports-990227772490}")
    private String bucketName;

    public void publishReportReadyEvent(String reportUrl, String s3Key, LocalDateTime generatedAt,
                                         Long totalFeedbacks, Double averageScore) {
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
