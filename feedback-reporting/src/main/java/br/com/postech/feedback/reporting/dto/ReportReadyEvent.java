package br.com.postech.feedback.reporting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportReadyEvent {

    private String eventType;
    private String message;
    private String reportLink;
    private String bucketName;
    private String s3Key;
    private Long totalFeedbacks;
    private Double averageScore;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime generatedAt;
}
