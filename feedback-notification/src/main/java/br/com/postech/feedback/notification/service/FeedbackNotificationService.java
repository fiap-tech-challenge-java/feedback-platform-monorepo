package br.com.postech.feedback.notification.service;

import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import br.com.postech.feedback.notification.dto.NotificationEmailDTO;
import br.com.postech.feedback.notification.dto.NotificationResponseDTO;
import br.com.postech.feedback.notification.dto.ReportReadyEventDTO;
import br.com.postech.feedback.notification.metrics.NotificationMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackNotificationService {

    private final SesClient sesClient;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    private final NotificationMetrics metrics;
    private final Validator validator;

    @Value("${SES_FROM_EMAIL:}")
    private String senderEmail;

    @Value("${SES_RECIPIENT_EMAIL:}")
    private String recipientEmail;

    @Value("${aws.ses.enabled:true}")
    private boolean sesEnabled;

    private void validateSesConfiguration() {
        if (senderEmail == null || senderEmail.isBlank()) {
            log.error("=== SES CONFIGURATION ERROR ===");
            log.error("SES sender email not configured!");
            log.error("Set SES_FROM_EMAIL environment variable in AWS Lambda.");
            log.error("================================");
            throw new IllegalStateException("SES sender email not configured. Set SES_FROM_EMAIL environment variable.");
        }
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.error("=== SES CONFIGURATION ERROR ===");
            log.error("SES recipient email not configured!");
            log.error("Set SES_RECIPIENT_EMAIL environment variable in AWS Lambda.");
            log.error("================================");
            throw new IllegalStateException("SES recipient email not configured. Set SES_RECIPIENT_EMAIL environment variable.");
        }
    }

    @Bean
    public Function<String, NotificationResponseDTO> processNotification() {
        return snsMessage -> {
            log.info("Recebendo mensagem para processamento de notificação");
            log.debug("Payload recebido: {}", snsMessage);

            if (snsMessage == null || snsMessage.trim().isEmpty()) {
                log.warn("Payload vazio recebido. Ignorando...");
                return NotificationResponseDTO.rejected("Payload vazio ou nulo");
            }

            if (!snsMessage.trim().startsWith("{") && !snsMessage.trim().startsWith("[")) {
                log.warn("Payload não-JSON recebido: '{}'. Ignorando...", snsMessage);
                return NotificationResponseDTO.rejected("Payload não é JSON válido");
            }

            try {
                String messageBody = extractMessageBody(snsMessage);

                if (isReportReadyEvent(messageBody)) {
                    return processReportReadyEvent(messageBody);
                }

                FeedbackEventDTO feedbackEvent = objectMapper.readValue(messageBody, FeedbackEventDTO.class);

                var violations = validator.validate(feedbackEvent);
                if (!violations.isEmpty()) {
                    String errorMsg = violations.stream()
                            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                            .reduce((a, b) -> a + "; " + b)
                            .orElse("Erro de validação");
                    log.error("Validação do FeedbackEventDTO falhou: {}", errorMsg);
                    metrics.incrementMessagesRejected();
                    return NotificationResponseDTO.error("Validação falhou: " + errorMsg);
                }

                metrics.incrementMessagesReceived();

                log.info("Processando notificação para Feedback ID: {} com status: {}",
                        feedbackEvent.id(), feedbackEvent.status());

                NotificationEmailDTO emailData = NotificationEmailDTO.fromCriticalFeedback(
                        feedbackEvent.id(),
                        feedbackEvent.description(),
                        feedbackEvent.rating(),
                        feedbackEvent.status(),
                        LocalDateTime.now()
                );

                boolean emailSent = sendEmail(emailData);

                metrics.incrementMessagesProcessed();

                String priority = feedbackEvent.status() != null ? feedbackEvent.status().name() : "UNKNOWN";

                log.info("Notificação processada com sucesso - ID: {}, Prioridade: {}, Email enviado: {}",
                        feedbackEvent.id(), priority, emailSent);

                return NotificationResponseDTO.success(
                        feedbackEvent.id(),
                        priority,
                        emailSent
                );

            } catch (Exception e) {
                log.error("Erro ao processar notificação. Payload: {}", snsMessage, e);
                return NotificationResponseDTO.error(e.getMessage());
            }
        };
    }

    private String extractMessageBody(String message) throws Exception {
        JsonNode rootNode = objectMapper.readTree(message);

        if (rootNode.has("Records")) {
            JsonNode records = rootNode.path("Records");
            if (records.isArray() && !records.isEmpty()) {
                JsonNode snsNode = records.get(0).path("Sns");
                return snsNode.path("Message").asText();
            }
        }

        if (rootNode.has("Message")) {
            return rootNode.path("Message").asText();
        }

        return message;
    }

    private boolean isReportReadyEvent(String messageBody) {
        try {
            JsonNode node = objectMapper.readTree(messageBody);
            return node.has("eventType") && "ReportReady".equals(node.get("eventType").asText());
        } catch (Exception e) {
            return false;
        }
    }

    private NotificationResponseDTO processReportReadyEvent(String messageBody) {
        try {
            ReportReadyEventDTO reportEvent = objectMapper.readValue(messageBody, ReportReadyEventDTO.class);

            log.info("Processando evento de relatório pronto - Link: {}", reportEvent.reportLink());

            metrics.incrementMessagesReceived();

            NotificationEmailDTO emailData = NotificationEmailDTO.fromWeeklyReport(
                    reportEvent.reportLink(),
                    reportEvent.totalFeedbacks(),
                    reportEvent.averageScore(),
                    reportEvent.generatedAt()
            );

            boolean emailSent = sendEmail(emailData);

            metrics.incrementMessagesProcessed();

            log.info("Notificação de relatório processada com sucesso - Link: {}, Email enviado: {}",
                    reportEvent.reportLink(), emailSent);

            return NotificationResponseDTO.builder()
                    .status("SUCCESS")
                    .message("Relatório semanal notificado com sucesso")
                    .emailSent(emailSent)
                    .processedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao processar evento de relatório: {}", e.getMessage(), e);
            return NotificationResponseDTO.error("Erro ao processar relatório: " + e.getMessage());
        }
    }

    private boolean sendEmail(NotificationEmailDTO emailData) {
        if (!sesEnabled) {
            log.warn("SES está desabilitado. E-mail não será enviado. Dados: {}", emailData);
            return false;
        }

        validateSesConfiguration();

        try {
            String htmlBody = generateEmailHtml(emailData);

            Message message = Message.builder()
                    .subject(Content.builder().data(emailData.subject()).build())
                    .body(Body.builder()
                            .html(Content.builder().data(htmlBody).build())
                            .build())
                    .build();

            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(Destination.builder()
                            .toAddresses(recipientEmail)
                            .build())
                    .message(message)
                    .build();

            SendEmailResponse response = sesClient.sendEmail(emailRequest);

            log.info("E-mail enviado com sucesso! MessageId: {} | Feedback ID: {} | Para: {}",
                    response.messageId(), emailData.feedbackId(), recipientEmail);

            metrics.incrementEmailsSent();
            return true;

        } catch (SesException e) {
            metrics.incrementEmailsFailed();
            String errorCode = e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : "UNKNOWN";
            String errorMessage = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
            log.error("Erro ao enviar e-mail via SES. Código: {} | Mensagem: {}", errorCode, errorMessage, e);
            throw new RuntimeException("Falha ao enviar e-mail: " + e.getMessage(), e);
        } catch (Exception e) {
            metrics.incrementEmailsFailed();
            log.error("Erro inesperado ao enviar e-mail", e);
            throw new RuntimeException("Falha ao enviar e-mail: " + e.getMessage(), e);
        }
    }

    private String generateEmailHtml(NotificationEmailDTO emailData) {
        Context context = new Context();

        if (emailData.isReportNotification()) {
            context.setVariable("reportLink", emailData.reportLink());
            context.setVariable("totalFeedbacks", emailData.totalFeedbacks());
            context.setVariable("averageScore", String.format("%.2f", emailData.averageScore()));
            context.setVariable("generatedAt", emailData.sentDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            ));
            return templateEngine.process("weekly-report-email", context);
        } else {
            context.setVariable("feedbackId", emailData.feedbackId());
            context.setVariable("description", emailData.description());
            context.setVariable("rating", emailData.rating());
            context.setVariable("urgency", emailData.urgency().toString());
            context.setVariable("sentDate", emailData.sentDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            ));
            return templateEngine.process("critical-feedback-email", context);
        }
    }
}
