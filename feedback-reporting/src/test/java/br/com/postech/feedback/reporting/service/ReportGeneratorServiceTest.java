package br.com.postech.feedback.reporting.service;

import br.com.postech.feedback.reporting.dto.FeedbackDetail;
import br.com.postech.feedback.reporting.dto.ReportMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportGeneratorServiceTest {

    private ReportGeneratorService reportGeneratorService;

    @BeforeEach
    void setUp() {
        reportGeneratorService = new ReportGeneratorService();
    }

    private ReportMetrics createTestMetrics() {
        Map<String, Long> feedbacksByDay = new HashMap<>();
        feedbacksByDay.put("2026-01-10", 12L);
        feedbacksByDay.put("2026-01-11", 7L);

        Map<String, Long> feedbacksByUrgency = new HashMap<>();
        feedbacksByUrgency.put("LOW", 15L);
        feedbacksByUrgency.put("MEDIUM", 3L);
        feedbacksByUrgency.put("HIGH", 1L);

        List<FeedbackDetail> feedbacks = new ArrayList<>();
        feedbacks.add(FeedbackDetail.builder()
                .description("Ótima aula!")
                .urgency("LOW")
                .createdAt("2026-01-10T10:30:00Z")
                .build());
        feedbacks.add(FeedbackDetail.builder()
                .description("Precisa melhorar")
                .urgency("HIGH")
                .createdAt("2026-01-11T14:00:00Z")
                .build());

        return ReportMetrics.builder()
                .totalFeedbacks(19L)
                .averageScore(7.5)
                .feedbacksByDay(feedbacksByDay)
                .feedbacksByUrgency(feedbacksByUrgency)
                .feedbacks(feedbacks)
                .build();
    }

    @Test
    @DisplayName("Deve gerar relatório em formato JSON com estrutura do Tech Challenge")
    void shouldGenerateJsonReportWithCorrectStructure() {
        // Arrange
        ReflectionTestUtils.setField(reportGeneratorService, "reportFormat", "json");
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        String result = reportGeneratorService.generateReport(metrics, generatedAt);

        // Assert
        assertThat(result).contains("\"type\" : \"WEEKLY_REPORT\"");
        assertThat(result).contains("\"period\" : \"weekly\"");
        assertThat(result).contains("\"summary\"");
        assertThat(result).contains("\"totalFeedbacks\" : 19");
        assertThat(result).contains("\"averageScore\" : 7.5");
        assertThat(result).contains("\"feedbacksByDay\"");
        assertThat(result).contains("\"feedbacksByUrgency\"");
        assertThat(result).contains("\"feedbacks\"");
        assertThat(result).contains("\"description\"");
        assertThat(result).contains("\"urgency\"");
    }

    @Test
    @DisplayName("Deve gerar relatório em formato CSV")
    void shouldGenerateCsvReport() {
        // Arrange
        ReflectionTestUtils.setField(reportGeneratorService, "reportFormat", "csv");
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        String result = reportGeneratorService.generateReport(metrics, generatedAt);

        // Assert
        assertThat(result).contains("metric,value");
        assertThat(result).contains("totalFeedbacks,19");
        assertThat(result).contains("averageScore,7.5");
    }

    @Test
    @DisplayName("Deve gerar chave S3 correta para JSON")
    void shouldGenerateCorrectS3KeyForJson() {
        // Arrange
        ReflectionTestUtils.setField(reportGeneratorService, "reportFormat", "json");
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        String s3Key = reportGeneratorService.generateS3Key(generatedAt);

        // Assert
        assertThat(s3Key).isEqualTo("reports/2026/01/report-2026-01-15.json");
    }

    @Test
    @DisplayName("Deve gerar chave S3 correta para CSV")
    void shouldGenerateCorrectS3KeyForCsv() {
        // Arrange
        ReflectionTestUtils.setField(reportGeneratorService, "reportFormat", "csv");
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        String s3Key = reportGeneratorService.generateS3Key(generatedAt);

        // Assert
        assertThat(s3Key).isEqualTo("reports/2026/01/report-2026-01-15.csv");
    }

    @Test
    @DisplayName("Deve retornar content-type correto para JSON")
    void shouldReturnCorrectContentTypeForJson() {
        // Arrange
        ReflectionTestUtils.setField(reportGeneratorService, "reportFormat", "json");

        // Act
        String contentType = reportGeneratorService.getContentType();

        // Assert
        assertThat(contentType).isEqualTo("application/json");
    }

    @Test
    @DisplayName("Deve retornar content-type correto para CSV")
    void shouldReturnCorrectContentTypeForCsv() {
        // Arrange
        ReflectionTestUtils.setField(reportGeneratorService, "reportFormat", "csv");

        // Act
        String contentType = reportGeneratorService.getContentType();

        // Assert
        assertThat(contentType).isEqualTo("text/csv");
    }
}
