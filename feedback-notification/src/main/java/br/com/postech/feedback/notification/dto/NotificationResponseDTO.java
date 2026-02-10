package br.com.postech.feedback.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponseDTO {

    private String status;
    private String message;
    private Long feedbackId;
    private String priority;
    private LocalDateTime processedAt;
    private Boolean emailSent;
    private String error;

    public static NotificationResponseDTO success(Long feedbackId, String priority, Boolean emailSent) {
        return NotificationResponseDTO.builder()
                .status("SUCCESS")
                .message("Notificação processada com sucesso")
                .feedbackId(feedbackId)
                .priority(priority)
                .emailSent(emailSent)
                .processedAt(LocalDateTime.now())
                .build();
    }

    public static NotificationResponseDTO error(String errorMessage) {
        return NotificationResponseDTO.builder()
                .status("ERROR")
                .message("Erro ao processar notificação")
                .error(errorMessage)
                .processedAt(LocalDateTime.now())
                .build();
    }

    public static NotificationResponseDTO rejected(String reason) {
        return NotificationResponseDTO.builder()
                .status("REJECTED")
                .message("Notificação rejeitada")
                .error(reason)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
