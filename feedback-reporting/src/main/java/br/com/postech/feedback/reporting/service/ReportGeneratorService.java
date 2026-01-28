package br.com.postech.feedback.reporting.service;

import br.com.postech.feedback.reporting.dto.Report;
import br.com.postech.feedback.reporting.dto.ReportFeedbackItem;
import br.com.postech.feedback.reporting.dto.ReportMetrics;
import br.com.postech.feedback.reporting.dto.ReportSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportGeneratorService {

    private static final String REPORT_TYPE = "WEEKLY_REPORT";
    private static final String REPORT_PERIOD = "weekly";

    private final ObjectMapper objectMapper;

    @Value("${reporting.format:csv}")
    private String reportFormat;

    public String generateReport(ReportMetrics metrics, LocalDateTime generatedAt) {
        log.info("Generating report in {} format", reportFormat.toUpperCase());

        try {
            if ("csv".equalsIgnoreCase(reportFormat)) {
                return generateCsvReport(metrics);
            }
            return generateJsonReport(metrics, generatedAt);
        } catch (Exception e) {
            log.error("Failed to generate report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    private String generateJsonReport(ReportMetrics metrics, LocalDateTime generatedAt) throws Exception {
        List<ReportFeedbackItem> feedbackItems = metrics.getFeedbacks().stream()
                .map(detail -> ReportFeedbackItem.builder()
                        .description(detail.getDescription())
                        .urgency(detail.getUrgency())
                        .createdAt(detail.getCreatedAt())
                        .build())
                .toList();

        Report report = Report.builder()
                .type(REPORT_TYPE)
                .generatedAt(generatedAt)
                .period(REPORT_PERIOD)
                .summary(ReportSummary.builder()
                        .totalFeedbacks(metrics.getTotalFeedbacks())
                        .averageScore(metrics.getAverageScore())
                        .build())
                .feedbacksByDay(metrics.getFeedbacksByDay())
                .feedbacksByUrgency(metrics.getFeedbacksByUrgency())
                .feedbacks(feedbackItems)
                .build();

        return objectMapper.writeValueAsString(report);
    }

    private String generateCsvReport(ReportMetrics metrics) {
        StringBuilder csv = new StringBuilder();
        csv.append("metric,value\n");
        csv.append("totalFeedbacks,").append(metrics.getTotalFeedbacks()).append("\n");
        csv.append("averageScore,").append(String.format(java.util.Locale.US, "%.1f", metrics.getAverageScore())).append("\n");

        if (metrics.getFeedbacksByUrgency() != null) {
            metrics.getFeedbacksByUrgency().forEach((urgency, count) ->
                    csv.append("urgency_").append(urgency).append(",").append(count).append("\n")
            );
        }
        return csv.toString();
    }

    public String getFileExtension() {
        return "csv".equalsIgnoreCase(reportFormat) ? "csv" : "json";
    }

    public String generateS3Key(LocalDateTime generatedAt) {
        String year = generatedAt.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = generatedAt.format(DateTimeFormatter.ofPattern("MM"));
        String date = generatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return String.format("reports/%s/%s/report-%s.%s", year, month, date, getFileExtension());
    }

    public String getContentType() {
        return "csv".equalsIgnoreCase(reportFormat) ? "text/csv" : "application/json";
    }
}
