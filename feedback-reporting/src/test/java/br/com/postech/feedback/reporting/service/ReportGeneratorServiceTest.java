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
        // Aceita tanto formato brasileiro (4,50) quanto internacional (4.50)
        assertThat(csvContent).containsAnyOf("4,50", "4.50");
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

    @Test
    @DisplayName("Deve gerar S3 key com data correta para diferentes meses")
    void shouldGenerateS3KeyWithCorrectDateForDifferentMonths() {
        // Arrange & Act & Assert
        assertThat(reportGeneratorService.generateS3Key(LocalDateTime.of(2026, 2, 15, 10, 30, 0)))
                .isEqualTo("reports/2026/02/relatorio-semanal-2026-02-15.csv");

        assertThat(reportGeneratorService.generateS3Key(LocalDateTime.of(2026, 12, 31, 23, 59, 59)))
                .isEqualTo("reports/2026/12/relatorio-semanal-2026-12-31.csv");

        assertThat(reportGeneratorService.generateS3Key(LocalDateTime.of(2025, 1, 1, 0, 0, 0)))
                .isEqualTo("reports/2025/01/relatorio-semanal-2025-01-01.csv");
    }

    @Test
    @DisplayName("Deve calcular nível de satisfação EXCELENTE corretamente")
    void shouldCalculateSatisfactionLevelExcellentCorrectly() {
        // Arrange
        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(100L)
                .averageScore(4.7)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 11, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("EXCELENTE");
    }

    @Test
    @DisplayName("Deve calcular nível de satisfação MUITO BOM corretamente")
    void shouldCalculateSatisfactionLevelVeryGoodCorrectly() {
        // Arrange
        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(100L)
                .averageScore(4.2)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 11, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("MUITO BOM");
    }

    @Test
    @DisplayName("Deve calcular nível de satisfação BOM corretamente")
    void shouldCalculateSatisfactionLevelGoodCorrectly() {
        // Arrange
        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(100L)
                .averageScore(3.5)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 11, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("BOM");
    }

    @Test
    @DisplayName("Deve calcular nível de satisfação REGULAR corretamente")
    void shouldCalculateSatisfactionLevelRegularCorrectly() {
        // Arrange
        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(100L)
                .averageScore(2.5)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 11, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("REGULAR");
    }

    @Test
    @DisplayName("Deve calcular nível de satisfação CRÍTICO corretamente")
    void shouldCalculateSatisfactionLevelCriticalCorrectly() {
        // Arrange
        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(100L)
                .averageScore(1.5)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 11, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("CRÍTICO");
    }

    @Test
    @DisplayName("Deve ordenar urgências corretamente no CSV")
    void shouldSortUrgenciesCorrectlyInCsv() {
        // Arrange
        Map<String, Long> feedbacksByUrgency = new HashMap<>();
        feedbacksByUrgency.put("LOW", 50L);
        feedbacksByUrgency.put("CRITICAL", 5L);
        feedbacksByUrgency.put("HIGH", 10L);
        feedbacksByUrgency.put("MEDIUM", 20L);

        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(85L)
                .averageScore(3.5)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(feedbacksByUrgency)
                .feedbacks(new ArrayList<>())
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 11, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert - CRITICAL should appear before HIGH, HIGH before MEDIUM, etc.
        int criticalIndex = csvContent.indexOf("CRITICAL");
        int highIndex = csvContent.indexOf("HIGH");
        int mediumIndex = csvContent.indexOf("MEDIUM");
        int lowIndex = csvContent.indexOf("LOW");

        assertThat(criticalIndex).isLessThan(highIndex);
        assertThat(highIndex).isLessThan(mediumIndex);
        assertThat(mediumIndex).isLessThan(lowIndex);
    }

    @Test
    @DisplayName("Deve sanitizar descrição com caracteres especiais")
    void shouldSanitizeDescriptionWithSpecialCharacters() {
        // Arrange
        List<FeedbackDetail> feedbacks = new ArrayList<>();
        feedbacks.add(FeedbackDetail.builder()
                .description("Feedback com ponto;vírgula e \"aspas\"")
                .urgency("LOW")
                .createdAt("2026-02-10T10:30:00")
                .build());

        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(1L)
                .averageScore(4.0)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(feedbacks)
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 11, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert - semicolons and quotes should be replaced
        assertThat(csvContent).doesNotContain("ponto;vírgula");
        assertThat(csvContent).contains("ponto,vírgula");
    }

    @Test
    @DisplayName("Deve incluir indicador de urgência correto para CRITICAL")
    void shouldIncludeCorrectUrgencyIndicatorForCritical() {
        // Arrange
        List<FeedbackDetail> feedbacks = new ArrayList<>();
        feedbacks.add(FeedbackDetail.builder()
                .description("Feedback crítico")
                .urgency("CRITICAL")
                .createdAt("2026-02-10T10:30:00")
                .build());

        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(1L)
                .averageScore(2.0)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(feedbacks)
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 11, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("[!!!] CRÍTICO");
    }

    @Test
    @DisplayName("Deve incluir dia da semana correto para cada data")
    void shouldIncludeCorrectDayOfWeekForEachDate() {
        // Arrange
        Map<String, Long> feedbacksByDay = new HashMap<>();
        feedbacksByDay.put("2026-02-09", 5L); // Segunda-feira
        feedbacksByDay.put("2026-02-14", 3L); // Sábado

        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(8L)
                .averageScore(4.0)
                .feedbacksByDay(feedbacksByDay)
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("Segunda-feira");
        assertThat(csvContent).contains("Sábado");
    }

    @Test
    @DisplayName("Deve formatar datas corretamente no padrão brasileiro")
    void shouldFormatDatesCorrectlyInBrazilianFormat() {
        // Arrange
        Map<String, Long> feedbacksByDay = new HashMap<>();
        feedbacksByDay.put("2026-02-09", 5L);

        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(5L)
                .averageScore(4.0)
                .feedbacksByDay(feedbacksByDay)
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();
        LocalDateTime generatedAt = LocalDateTime.of(2026, 2, 15, 10, 30, 0);

        // Act
        byte[] result = reportGeneratorService.generateReportAsBytes(metrics, generatedAt);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Assert
        assertThat(csvContent).contains("09/02/2026");
    }
}
