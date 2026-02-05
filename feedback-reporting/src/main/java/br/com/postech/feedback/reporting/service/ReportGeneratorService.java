package br.com.postech.feedback.reporting.service;

import br.com.postech.feedback.reporting.dto.ReportMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Serviço responsável por gerar relatórios semanais em formato CSV.
 * O CSV é formatado para ser apresentável no Excel com separador ; (padrão brasileiro).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportGeneratorService {

    private static final String CSV_SEPARATOR = ";";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Gera o relatório CSV como bytes para upload no S3.
     */
    public byte[] generateReportAsBytes(ReportMetrics metrics, LocalDateTime generatedAt) {
        log.info("Generating CSV report...");
        try {
            String csvContent = generateCsvReport(metrics, generatedAt);
            return csvContent.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to generate report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate report", e);
        }
    }

    /**
     * Gera o relatório CSV formatado e apresentável.
     * 
     * Requisitos obrigatórios atendidos:
     * - Descrição
     * - Urgência  
     * - Data de envio
     * - Quantidade de avaliações por dia
     * - Quantidade de avaliações por urgência
     */
    private String generateCsvReport(ReportMetrics metrics, LocalDateTime generatedAt) {
        StringBuilder csv = new StringBuilder();
        
        // BOM para UTF-8 (Excel reconhece acentos corretamente)
        csv.append("\uFEFF");
        
        // ════════════════════════════════════════════════════════════════════════════
        // CABEÇALHO DO RELATÓRIO
        // ════════════════════════════════════════════════════════════════════════════
        csv.append("RELATÓRIO SEMANAL DE FEEDBACKS").append("\n");
        csv.append("Gerado em:").append(CSV_SEPARATOR)
           .append(generatedAt.format(DATE_TIME_FORMATTER)).append("\n");
        csv.append("Período:").append(CSV_SEPARATOR).append("Últimos 7 dias").append("\n");
        csv.append("\n");
        
        // ════════════════════════════════════════════════════════════════════════════
        // SEÇÃO 1: RESUMO EXECUTIVO
        // ════════════════════════════════════════════════════════════════════════════
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("RESUMO EXECUTIVO").append("\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("Indicador").append(CSV_SEPARATOR)
           .append("Valor").append(CSV_SEPARATOR)
           .append("Observação").append("\n");
        
        csv.append("Total de Feedbacks").append(CSV_SEPARATOR)
           .append(metrics.getTotalFeedbacks() != null ? metrics.getTotalFeedbacks() : 0).append(CSV_SEPARATOR)
           .append("Total de avaliações recebidas").append("\n");
        
        Double avgScore = metrics.getAverageScore();
        csv.append("Nota Média").append(CSV_SEPARATOR)
           .append(avgScore != null ? String.format("%.2f", avgScore) : "0.00").append(CSV_SEPARATOR)
           .append("Escala de 1 a 5").append("\n");
        
        csv.append("Nível de Satisfação").append(CSV_SEPARATOR)
           .append(calculateSatisfactionLevel(avgScore)).append(CSV_SEPARATOR)
           .append(getSatisfactionEmoji(avgScore)).append("\n");
        csv.append("\n");

        // ════════════════════════════════════════════════════════════════════════════
        // SEÇÃO 2: QUANTIDADE DE AVALIAÇÕES POR URGÊNCIA (Requisito Obrigatório)
        // ════════════════════════════════════════════════════════════════════════════
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("QUANTIDADE DE AVALIAÇÕES POR URGÊNCIA").append("\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("Urgência").append(CSV_SEPARATOR)
           .append("Quantidade").append(CSV_SEPARATOR)
           .append("Percentual").append(CSV_SEPARATOR)
           .append("Indicador").append("\n");
        
        if (metrics.getFeedbacksByUrgency() != null && !metrics.getFeedbacksByUrgency().isEmpty()) {
            long total = metrics.getTotalFeedbacks() != null ? metrics.getTotalFeedbacks() : 1;
            
            // Ordenar por prioridade: CRITICAL > HIGH > MEDIUM > LOW
            metrics.getFeedbacksByUrgency().entrySet().stream()
                    .sorted((e1, e2) -> getUrgencyPriority(e1.getKey()) - getUrgencyPriority(e2.getKey()))
                    .forEach(entry -> {
                        String urgency = entry.getKey();
                        Long count = entry.getValue();
                        double percentage = total > 0 ? (count * 100.0 / total) : 0;
                        
                        csv.append(urgency).append(CSV_SEPARATOR)
                           .append(count).append(CSV_SEPARATOR)
                           .append(String.format("%.1f%%", percentage)).append(CSV_SEPARATOR)
                           .append(getUrgencyIndicator(urgency)).append("\n");
                    });
        } else {
            csv.append("Nenhum dado").append(CSV_SEPARATOR)
               .append("0").append(CSV_SEPARATOR)
               .append("0%").append(CSV_SEPARATOR)
               .append("-").append("\n");
        }
        csv.append("\n");

        // ════════════════════════════════════════════════════════════════════════════
        // SEÇÃO 3: QUANTIDADE DE AVALIAÇÕES POR DIA (Requisito Obrigatório)
        // ════════════════════════════════════════════════════════════════════════════
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("QUANTIDADE DE AVALIAÇÕES POR DIA").append("\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("Data de Envio").append(CSV_SEPARATOR)
           .append("Dia da Semana").append(CSV_SEPARATOR)
           .append("Quantidade").append("\n");
        
        if (metrics.getFeedbacksByDay() != null && !metrics.getFeedbacksByDay().isEmpty()) {
            metrics.getFeedbacksByDay().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String dateStr = entry.getKey();
                        csv.append(formatDate(dateStr)).append(CSV_SEPARATOR)
                           .append(getDayOfWeek(dateStr)).append(CSV_SEPARATOR)
                           .append(entry.getValue()).append("\n");
                    });
        } else {
            csv.append("Nenhum dado").append(CSV_SEPARATOR)
               .append("-").append(CSV_SEPARATOR)
               .append("0").append("\n");
        }
        csv.append("\n");

        // ════════════════════════════════════════════════════════════════════════════
        // SEÇÃO 4: DETALHES DOS FEEDBACKS (Requisitos: Descrição, Urgência, Data de Envio)
        // ════════════════════════════════════════════════════════════════════════════
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("DETALHES DOS FEEDBACKS").append("\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("Data de Envio").append(CSV_SEPARATOR)
           .append("Urgência").append(CSV_SEPARATOR)
           .append("Indicador").append(CSV_SEPARATOR)
           .append("Descrição").append("\n");
        
        if (metrics.getFeedbacks() != null && !metrics.getFeedbacks().isEmpty()) {
            metrics.getFeedbacks().forEach(feedback -> {
                String description = sanitizeForCsv(feedback.getDescription());
                String urgency = feedback.getUrgency() != null ? feedback.getUrgency() : "";
                
                csv.append(formatDate(feedback.getCreatedAt())).append(CSV_SEPARATOR)
                   .append(urgency).append(CSV_SEPARATOR)
                   .append(getUrgencyIndicator(urgency)).append(CSV_SEPARATOR)
                   .append(description).append("\n");
            });
        } else {
            csv.append("-").append(CSV_SEPARATOR)
               .append("-").append(CSV_SEPARATOR)
               .append("-").append(CSV_SEPARATOR)
               .append("Nenhum feedback registrado no período").append("\n");
        }
        
        // ════════════════════════════════════════════════════════════════════════════
        // RODAPÉ
        // ════════════════════════════════════════════════════════════════════════════
        csv.append("\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");
        csv.append("FIM DO RELATÓRIO").append("\n");
        csv.append("═══════════════════════════════════════════════════════════════════════════════").append("\n");

        return csv.toString();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ════════════════════════════════════════════════════════════════════════════

    private String calculateSatisfactionLevel(Double averageScore) {
        if (averageScore == null) return "N/A";
        if (averageScore >= 4.5) return "EXCELENTE";
        if (averageScore >= 4.0) return "MUITO BOM";
        if (averageScore >= 3.0) return "BOM";
        if (averageScore >= 2.0) return "REGULAR";
        return "CRÍTICO";
    }

    private String getSatisfactionEmoji(Double averageScore) {
        if (averageScore == null) return "Sem dados";
        if (averageScore >= 4.5) return "Clientes muito satisfeitos!";
        if (averageScore >= 4.0) return "Boa satisfação geral";
        if (averageScore >= 3.0) return "Satisfação moderada";
        if (averageScore >= 2.0) return "Necessita atenção";
        return "Situação crítica!";
    }

    private String getUrgencyIndicator(String urgency) {
        if (urgency == null || urgency.isBlank()) return "-";
        return switch (urgency.toUpperCase()) {
            case "CRITICAL", "CRITICO", "CRÍTICO" -> "[!!!] CRÍTICO";
            case "HIGH", "ALTA", "ALTO" -> "[!!] ALTO";
            case "MEDIUM", "MEDIA", "MÉDIO", "MÉDIA" -> "[!] MÉDIO";
            case "LOW", "BAIXA", "BAIXO" -> "[ ] BAIXO";
            default -> "[-] " + urgency;
        };
    }

    private int getUrgencyPriority(String urgency) {
        if (urgency == null) return 99;
        return switch (urgency.toUpperCase()) {
            case "CRITICAL", "CRITICO", "CRÍTICO" -> 1;
            case "HIGH", "ALTA", "ALTO" -> 2;
            case "MEDIUM", "MEDIA", "MÉDIO", "MÉDIA" -> 3;
            case "LOW", "BAIXA", "BAIXO" -> 4;
            default -> 99;
        };
    }

    private String getDayOfWeek(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return "-";
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            return switch (date.getDayOfWeek()) {
                case MONDAY -> "Segunda-feira";
                case TUESDAY -> "Terça-feira";
                case WEDNESDAY -> "Quarta-feira";
                case THURSDAY -> "Quinta-feira";
                case FRIDAY -> "Sexta-feira";
                case SATURDAY -> "Sábado";
                case SUNDAY -> "Domingo";
            };
        } catch (Exception e) {
            return "-";
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return "-";
        try {
            // Tenta parsear como LocalDateTime primeiro
            if (dateStr.contains("T") || dateStr.contains(" ")) {
                java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(
                        dateStr.replace(" ", "T").substring(0, Math.min(19, dateStr.length())));
                return dateTime.format(DATE_FORMATTER);
            }
            // Se for só data (yyyy-MM-dd), formata para dd/MM/yyyy
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            return dateStr; // Retorna original se não conseguir formatar
        }
    }

    private String sanitizeForCsv(String text) {
        if (text == null) return "";
        // Remove caracteres problemáticos para CSV
        return text.replace(";", ",")
                   .replace("\n", " ")
                   .replace("\r", " ")
                   .replace("\"", "'")
                   .trim();
    }

    public String getFileExtension() {
        return "csv";
    }

    public String generateS3Key(LocalDateTime generatedAt) {
        String year = generatedAt.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = generatedAt.format(DateTimeFormatter.ofPattern("MM"));
        String date = generatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return String.format("reports/%s/%s/relatorio-semanal-%s.csv", year, month, date);
    }

    public String getContentType() {
        return "text/csv; charset=UTF-8";
    }
}
