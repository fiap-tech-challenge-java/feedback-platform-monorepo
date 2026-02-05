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
import java.util.Map;

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
        
        // ═══════════════════════════════════════════════════════════════════
        // CABEÇALHO DO RELATÓRIO
        // ═══════════════════════════════════════════════════════════════════
        csv.append("═══════════════════════════════════════════════════════════════════════════════\n");
        csv.append("                    RELATÓRIO SEMANAL DE FEEDBACKS\n");
        csv.append("                    Gerado em: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════\n");
        csv.append("\n");

        // ═══════════════════════════════════════════════════════════════════
        // RESUMO EXECUTIVO
        // ═══════════════════════════════════════════════════════════════════
        csv.append("┌─────────────────────────────────────────────────────────────────────────────┐\n");
        csv.append("│                           RESUMO EXECUTIVO                                  │\n");
        csv.append("├─────────────────────────────────────────────────────────────────────────────┤\n");
        csv.append(String.format("│  📊 Total de Feedbacks:     %-49d │\n", metrics.getTotalFeedbacks()));
        csv.append(String.format("│  ⭐ Nota Média:              %-49s │\n", String.format(java.util.Locale.US, "%.2f / 5.00", metrics.getAverageScore())));
        csv.append(String.format("│  📈 Satisfação:             %-49s │\n", calculateSatisfactionLevel(metrics.getAverageScore())));
        csv.append("└─────────────────────────────────────────────────────────────────────────────┘\n");
        csv.append("\n");

        // ═══════════════════════════════════════════════════════════════════
        // QUANTIDADE DE AVALIAÇÕES POR URGÊNCIA (Requisito Obrigatório)
        // ═══════════════════════════════════════════════════════════════════
        csv.append("┌─────────────────────────────────────────────────────────────────────────────┐\n");
        csv.append("│                 QUANTIDADE DE AVALIAÇÕES POR URGÊNCIA                       │\n");
        csv.append("├────────────────────┬──────────────┬──────────────────────────────────────────┤\n");
        csv.append("│      Urgência      │  Quantidade  │               Percentual                 │\n");
        csv.append("├────────────────────┼──────────────┼──────────────────────────────────────────┤\n");
        
        if (metrics.getFeedbacksByUrgency() != null && !metrics.getFeedbacksByUrgency().isEmpty()) {
            long total = metrics.getTotalFeedbacks();
            metrics.getFeedbacksByUrgency().forEach((urgency, count) -> {
                double percentage = total > 0 ? (count * 100.0 / total) : 0;
                String emoji = getUrgencyEmoji(urgency);
                String bar = generateProgressBar(percentage, 30);
                csv.append(String.format("│ %s %-15s │ %12d │ %s %5.1f%% │\n", 
                        emoji, urgency, count, bar, percentage));
            });
        } else {
            csv.append("│                    Nenhuma avaliação registrada                             │\n");
        }
        csv.append("└────────────────────┴──────────────┴──────────────────────────────────────────┘\n");
        csv.append("\n");

        // ═══════════════════════════════════════════════════════════════════
        // QUANTIDADE DE AVALIAÇÕES POR DIA (Requisito Obrigatório)
        // ═══════════════════════════════════════════════════════════════════
        csv.append("┌─────────────────────────────────────────────────────────────────────────────┐\n");
        csv.append("│                   QUANTIDADE DE AVALIAÇÕES POR DIA                          │\n");
        csv.append("├────────────────────┬──────────────┬──────────────────────────────────────────┤\n");
        csv.append("│   Data de Envio    │  Quantidade  │                 Gráfico                  │\n");
        csv.append("├────────────────────┼──────────────┼──────────────────────────────────────────┤\n");
        
        if (metrics.getFeedbacksByDay() != null && !metrics.getFeedbacksByDay().isEmpty()) {
            long maxCount = metrics.getFeedbacksByDay().values().stream().mapToLong(Long::longValue).max().orElse(1);
            metrics.getFeedbacksByDay().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        double percentage = maxCount > 0 ? (entry.getValue() * 100.0 / maxCount) : 0;
                        String bar = generateBarChart(percentage, 30);
                        csv.append(String.format("│ 📅 %-15s │ %12d │ %s │\n", 
                                entry.getKey(), entry.getValue(), bar));
                    });
        } else {
            csv.append("│                    Nenhuma avaliação registrada                             │\n");
        }
        csv.append("└────────────────────┴──────────────┴──────────────────────────────────────────┘\n");
        csv.append("\n");

        // ═══════════════════════════════════════════════════════════════════
        // DETALHES: DESCRIÇÃO, URGÊNCIA E DATA DE ENVIO (Requisitos Obrigatórios)
        // ═══════════════════════════════════════════════════════════════════
        csv.append("┌─────────────────────────────────────────────────────────────────────────────┐\n");
        csv.append("│            DETALHES DOS FEEDBACKS (Descrição, Urgência, Data)               │\n");
        csv.append("└─────────────────────────────────────────────────────────────────────────────┘\n");
        csv.append("\n");
        csv.append("DATA DE ENVIO,URGÊNCIA,DESCRIÇÃO\n");
        
        if (metrics.getFeedbacks() != null) {
            metrics.getFeedbacks().forEach(feedback -> {
                String description = feedback.getDescription() != null 
                        ? feedback.getDescription().replace(",", ";").replace("\n", " ") 
                        : "";
                csv.append(String.format("%s,%s,\"%s\"\n",
                        feedback.getCreatedAt() != null ? feedback.getCreatedAt() : "",
                        feedback.getUrgency() != null ? feedback.getUrgency() : "",
                        description));
            });
        }

        csv.append("\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════\n");
        csv.append("                           FIM DO RELATÓRIO\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════\n");

        return csv.toString();
    }

    private String calculateSatisfactionLevel(Double averageScore) {
        if (averageScore == null) return "N/A";
        if (averageScore >= 4.5) return "🟢 EXCELENTE";
        if (averageScore >= 4.0) return "🟢 MUITO BOM";
        if (averageScore >= 3.0) return "🟡 BOM";
        if (averageScore >= 2.0) return "🟠 REGULAR";
        return "🔴 CRÍTICO";
    }

    private String getUrgencyEmoji(String urgency) {
        if (urgency == null) return "⚪";
        return switch (urgency.toUpperCase()) {
            case "CRITICAL", "CRITICO", "CRÍTICO" -> "🔴";
            case "HIGH", "ALTA", "ALTO" -> "🟠";
            case "MEDIUM", "MEDIA", "MÉDIO", "MÉDIA" -> "🟡";
            case "LOW", "BAIXA", "BAIXO" -> "🟢";
            default -> "⚪";
        };
    }

    private String generateProgressBar(double percentage, int width) {
        int filled = (int) Math.round(percentage * width / 100);
        int empty = width - filled;
        return "█".repeat(Math.max(0, filled)) + "░".repeat(Math.max(0, empty));
    }

    private String generateBarChart(double percentage, int width) {
        int filled = (int) Math.round(percentage * width / 100);
        return "▓".repeat(Math.max(0, filled)) + "░".repeat(Math.max(0, width - filled));
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
