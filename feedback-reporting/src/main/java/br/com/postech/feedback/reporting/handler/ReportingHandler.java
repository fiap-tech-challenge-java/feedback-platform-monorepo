package br.com.postech.feedback.reporting.handler;

import br.com.postech.feedback.reporting.dto.ReportMetrics;
import br.com.postech.feedback.reporting.service.DatabaseQueryService;
import br.com.postech.feedback.reporting.service.ReportGeneratorService;
import br.com.postech.feedback.reporting.service.S3UploadService;
import br.com.postech.feedback.reporting.service.SnsPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ReportingHandler {

    private final DatabaseQueryService databaseQueryService;
    private final ReportGeneratorService reportGeneratorService;
    private final S3UploadService s3UploadService;
    private final SnsPublishService snsPublishService;

    @Bean
    public Function<Map<String, Object>, Map<String, Object>> generateReport() {
        return event -> {
            LocalDateTime generatedAt = LocalDateTime.now(ZoneOffset.UTC);
            log.info("Starting weekly report generation - trigger: {}", event);

            try {
                ReportMetrics metrics = databaseQueryService.fetchMetrics();

                String reportContent = reportGeneratorService.generateReport(metrics, generatedAt);
                String s3Key = reportGeneratorService.generateS3Key(generatedAt);
                String contentType = reportGeneratorService.getContentType();

                String reportUrl = s3UploadService.uploadReport(reportContent, s3Key, contentType);
                log.info("Report uploaded to S3: {}", reportUrl);

                snsPublishService.publishReportReadyEvent(reportUrl, generatedAt);
                log.info("Report notification sent to SNS");

                return Map.of(
                        "statusCode", 200,
                        "message", "Weekly report generated successfully",
                        "reportUrl", reportUrl,
                        "generatedAt", generatedAt.toString(),
                        "totalFeedbacks", metrics.getTotalFeedbacks(),
                        "averageScore", metrics.getAverageScore()
                );

            } catch (Exception e) {
                log.error("Report generation failed: {}", e.getMessage(), e);
                throw new RuntimeException("Report generation failed: " + e.getMessage(), e);
            }
        };
    }
}
