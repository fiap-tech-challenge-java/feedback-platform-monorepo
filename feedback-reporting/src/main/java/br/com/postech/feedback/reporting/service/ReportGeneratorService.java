package br.com.postech.feedback.reporting.service;

import br.com.postech.feedback.reporting.dto.Report;
import br.com.postech.feedback.reporting.dto.ReportFeedbackItem;
import br.com.postech.feedback.reporting.dto.ReportMetrics;
import br.com.postech.feedback.reporting.dto.ReportSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportGeneratorService {

    private static final String REPORT_TYPE = "WEEKLY_REPORT";
    private static final String REPORT_PERIOD = "weekly";

    @Value("${reporting.format:json}")
    private String reportFormat;

    private final ObjectMapper objectMapper;

    public ReportGeneratorService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String generateReport(ReportMetrics metrics, LocalDateTime generatedAt) {
        log.info("Generating report in {} format", reportFormat.toUpperCase());

        try {
            String content;
            if ("csv".equalsIgnoreCase(reportFormat)) {
                content = generateCsvReport(metrics);
            } else {
                content = generateJsonReport(metrics, generatedAt);
            }
            return content;
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
                .collect(Collectors.toList());

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
        csv.append("averageScore,").append(String.format("%.1f", metrics.getAverageScore())).append("\n");

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
