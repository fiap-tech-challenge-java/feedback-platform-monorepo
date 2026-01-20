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
        String subject
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
                "ALERTA: Novo Feedback Crítico Recebido"
        );
    }
}
