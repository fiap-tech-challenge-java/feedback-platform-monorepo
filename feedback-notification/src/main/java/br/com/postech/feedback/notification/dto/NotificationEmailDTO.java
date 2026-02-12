package br.com.postech.feedback.notification.dto;

import br.com.postech.feedback.core.domain.StatusFeedback;
import java.time.LocalDateTime;

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

    public boolean isReportNotification() {
        return reportLink != null;
    }
}
