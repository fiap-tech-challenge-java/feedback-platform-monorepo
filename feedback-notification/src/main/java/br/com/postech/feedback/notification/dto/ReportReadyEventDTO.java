package br.com.postech.feedback.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * DTO para receber eventos de relatório pronto do SNS
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReportReadyEventDTO(
        String eventType,
        String message,
        String reportLink,
        String bucketName,
        String s3Key,
        Long totalFeedbacks,
        Double averageScore,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime generatedAt
) {
    /**
     * Verifica se este é um evento de relatório pronto
     */
    public boolean isReportReadyEvent() {
        return "ReportReady".equals(eventType);
    }
}
