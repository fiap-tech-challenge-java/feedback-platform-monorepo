package br.com.postech.feedback.reporting.service;

import br.com.postech.feedback.reporting.dto.FeedbackDetail;
import br.com.postech.feedback.reporting.dto.ReportMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
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
                .createdAt("2026-01-10T10:30:00")
                .build());
        feedbacks.add(FeedbackDetail.builder()
                .description("Precisa melhorar")
                .urgency("HIGH")
                .createdAt("2026-01-11T14:00:00")
                .build());

        return ReportMetrics.builder()
                .totalFeedbacks(19L)
                .averageScore(4.5)
                .feedbacksByDay(feedbacksByDay)
                .feedbacksByUrgency(feedbacksByUrgency)
                .feedbacks(feedbacks)
                .build();
    }

    @Test
    @DisplayName("Deve gerar relatório CSV com estrutura correta")
    void shouldGenerateCsvReportWithCorrectStructure() {
        // Arrange
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("RELATÓRIO SEMANAL DE FEEDBACKS");
        assertThat(csvContent).contains("RESUMO EXECUTIVO");
        assertThat(csvContent).contains("QUANTIDADE DE AVALIAÇÕES POR URGÊNCIA");
        assertThat(csvContent).contains("QUANTIDADE DE AVALIAÇÕES POR DIA");
        assertThat(csvContent).contains("DETALHES DOS FEEDBACKS");
        assertThat(csvContent).contains("FIM DO RELATÓRIO");
    }

    @Test
    @DisplayName("Deve incluir dados de métricas no CSV")
    void shouldIncludeMetricsDataInCsv() {
        // Arrange
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert - Verificar resumo
        assertThat(csvContent).contains("Total de Feedbacks");
        assertThat(csvContent).contains("19");
        assertThat(csvContent).contains("Nota Média");
        assertThat(csvContent).contains("4.50");
        assertThat(csvContent).contains("EXCELENTE");
    }

    @Test
    @DisplayName("Deve incluir avaliações por urgência no CSV")
    void shouldIncludeUrgencyDataInCsv() {
        // Arrange
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("LOW");
        assertThat(csvContent).contains("MEDIUM");
        assertThat(csvContent).contains("HIGH");
    }

    @Test
    @DisplayName("Deve incluir avaliações por dia no CSV")
    void shouldIncludeDailyDataInCsv() {
        // Arrange
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("10/01/2026");
        assertThat(csvContent).contains("11/01/2026");
    }

    @Test
    @DisplayName("Deve incluir detalhes dos feedbacks no CSV")
    void shouldIncludeFeedbackDetailsInCsv() {
        // Arrange
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("Ótima aula!");
        assertThat(csvContent).contains("Precisa melhorar");
    }

    @Test
    @DisplayName("Deve gerar chave S3 correta para CSV")
    void shouldGenerateCorrectS3Key() {
        // Arrange
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        String s3Key = reportGeneratorService.generateS3Key(generatedAt);

        // Assert
        assertThat(s3Key).isEqualTo("reports/2026/01/relatorio-semanal-2026-01-15.csv");
    }

    @Test
    @DisplayName("Deve retornar content-type correto para CSV")
    void shouldReturnCorrectContentType() {
        // Act
        String contentType = reportGeneratorService.getContentType();

        // Assert
        assertThat(contentType).isEqualTo("text/csv; charset=UTF-8");
    }

    @Test
    @DisplayName("Deve retornar extensão correta")
    void shouldReturnCorrectFileExtension() {
        // Act
        String extension = reportGeneratorService.getFileExtension();

        // Assert
        assertThat(extension).isEqualTo("csv");
    }

    @Test
    @DisplayName("Deve usar separador ponto-e-vírgula para Excel brasileiro")
    void shouldUseSemicolonSeparatorForBrazilianExcel() {
        // Arrange
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert - CSV usa ; como separador
        assertThat(csvContent).contains(";");
    }

    @Test
    @DisplayName("Deve incluir BOM UTF-8 para Excel reconhecer acentos")
    void shouldIncludeUtf8Bom() {
        // Arrange
        ReportMetrics metrics = createTestMetrics();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert - BOM UTF-8 está no início
        assertThat(csvContent).startsWith("\uFEFF");
    }

    @Test
    @DisplayName("Deve tratar métricas nulas graciosamente")
    void shouldHandleNullMetricsGracefully() {
        // Arrange
        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(null)
                .averageScore(null)
                .feedbacksByDay(null)
                .feedbacksByUrgency(null)
                .feedbacks(null)
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 1, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert - Deve gerar relatório sem erros
        assertThat(csvContent).contains("RELATÓRIO SEMANAL DE FEEDBACKS");
        assertThat(csvContent).contains("Nenhum dado");
    }
}
