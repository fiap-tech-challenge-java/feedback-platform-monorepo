package br.com.postech.feedback.notification.dto;

import br.com.postech.feedback.core.domain.StatusFeedback;
import java.time.LocalDateTime;

/**
 * DTO para os dados que serão enviados no e-mail de notificação
 */
public record NotificationEmailDTO(
        Long feedbackId,
        String description,
        Integer rating,
        StatusFeedback urgency,
        LocalDateTime sentDate,
        String subject,
        String reportLink,
        Long totalFeedbacks,
        Double averageScore
) {
    /**
     * Construtor de conveniência para criar um DTO de notificação crítica
     */
    public static NotificationEmailDTO fromCriticalFeedback(
            Long feedbackId,
            String description,
            Integer rating,
            StatusFeedback urgency,
            LocalDateTime sentDate
    ) {
        return new NotificationEmailDTO(
                feedbackId,
                description,
                rating,
                urgency,
                sentDate,
                "ALERTA: Novo Feedback Crítico Recebido",
                null,
                null,
                null
        );
    }

    /**
     * Construtor de conveniência para criar um DTO de relatório semanal
     */
    public static NotificationEmailDTO fromWeeklyReport(
            String reportLink,
            Long totalFeedbacks,
            Double averageScore,
            LocalDateTime generatedAt
    ) {
        return new NotificationEmailDTO(
                null,
                null,
                null,
                null,
                generatedAt,
                "Relatório Semanal de Feedbacks Disponível",
                reportLink,
                totalFeedbacks,
                averageScore
        );
    }

    /**
     * Verifica se é uma notificação de relatório
     */
    public boolean isReportNotification() {
        return reportLink != null;
    }
}
