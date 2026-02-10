package br.com.postech.feedback.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

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
    public boolean isReportReadyEvent() {
        return "ReportReady".equals(eventType);
    }
}
