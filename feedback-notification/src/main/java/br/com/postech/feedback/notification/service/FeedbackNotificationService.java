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

/**
 * Serviço responsável por processar notificações SNS e enviar e-mails via SES
 * Responsabilidade Única: Envio de notificações por e-mail para feedbacks críticos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackNotificationService {

    private final SesClient sesClient;
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    private final NotificationMetrics metrics;
    private final Validator validator;

    @Value("${aws.ses.sender-email:matheusfreiredev@gmail.com}")
    private String senderEmail;

    @Value("${aws.ses.recipient-email:matheusfreiredev@gmail.com}")
    private String recipientEmail;

    @Value("${aws.ses.enabled:true}")
    private boolean sesEnabled;

    /**
     * Bean que define a função serverless para consumir mensagens do SNS
     * Esta função é acionada automaticamente quando uma mensagem chega ao tópico SNS
     * Retorna uma resposta HTTP com status e detalhes do processamento
     */
    @Bean
    public Function<String, NotificationResponseDTO> processNotification() {
        return snsMessage -> {
            log.info("Recebendo mensagem para processamento de notificação");
            log.debug("Payload recebido: {}", snsMessage);

            // Validação básica: ignorar payloads vazios ou não-JSON
            if (snsMessage == null || snsMessage.trim().isEmpty()) {
                log.warn("Payload vazio recebido. Ignorando...");
                return NotificationResponseDTO.rejected("Payload vazio ou nulo");
            }

            // Ignorar requisições GET ou payloads que claramente não são JSON
            if (!snsMessage.trim().startsWith("{") && !snsMessage.trim().startsWith("[")) {
                log.warn("Payload não-JSON recebido: '{}'. Ignorando...", snsMessage);
                return NotificationResponseDTO.rejected("Payload não é JSON válido");
            }

            try {
                // Primeiro, extrai o corpo da mensagem
                String messageBody = extractMessageBody(snsMessage);

                // Verifica se é um evento de relatório pronto
                if (isReportReadyEvent(messageBody)) {
                    return processReportReadyEvent(messageBody);
                }

                // Caso contrário, processa como feedback crítico
                FeedbackEventDTO feedbackEvent = objectMapper.readValue(messageBody, FeedbackEventDTO.class);

                // Validar o FeedbackEventDTO
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

                // Cria DTO de notificação
                NotificationEmailDTO emailData = NotificationEmailDTO.fromCriticalFeedback(
                        feedbackEvent.id(),
                        feedbackEvent.description(),
                        feedbackEvent.rating(),
                        feedbackEvent.status(),
                        LocalDateTime.now()
                );

                // Envia e-mail
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

    /**
     * Extrai o corpo da mensagem do formato SNS
     */
    private String extractMessageBody(String message) throws Exception {
        JsonNode rootNode = objectMapper.readTree(message);

        // Verifica se é formato SNS via Lambda (tem campo "Records")
        if (rootNode.has("Records")) {
            JsonNode records = rootNode.path("Records");
            if (records.isArray() && !records.isEmpty()) {
                JsonNode snsNode = records.get(0).path("Sns");
                return snsNode.path("Message").asText();
            }
        }

        // Verifica se é uma mensagem SNS direta (tem campo "Message")
        if (rootNode.has("Message")) {
            return rootNode.path("Message").asText();
        }

        // Assume que é o conteúdo direto
        return message;
    }

    /**
     * Verifica se a mensagem é um evento de relatório pronto
     */
    private boolean isReportReadyEvent(String messageBody) {
        try {
            JsonNode node = objectMapper.readTree(messageBody);
            return node.has("eventType") && "ReportReady".equals(node.get("eventType").asText());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Processa evento de relatório pronto
     */
    private NotificationResponseDTO processReportReadyEvent(String messageBody) {
        try {
            ReportReadyEventDTO reportEvent = objectMapper.readValue(messageBody, ReportReadyEventDTO.class);

            log.info("Processando evento de relatório pronto - Link: {}", reportEvent.reportLink());

            metrics.incrementMessagesReceived();

            // Cria DTO de notificação para o relatório
            NotificationEmailDTO emailData = NotificationEmailDTO.fromWeeklyReport(
                    reportEvent.reportLink(),
                    reportEvent.totalFeedbacks(),
                    reportEvent.averageScore(),
                    reportEvent.generatedAt()
            );

            // Envia e-mail
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

    /**

    /**
     * Envia e-mail usando AWS SES com template HTML
     * @return true se o email foi enviado, false se SES está desabilitado
     */
    private boolean sendEmail(NotificationEmailDTO emailData) {
        if (!sesEnabled) {
            log.warn("SES está desabilitado. E-mail não será enviado. Dados: {}", emailData);
            return false;
        }

        try {
            // Gera o HTML do e-mail usando Thymeleaf
            String htmlBody = generateEmailHtml(emailData);

            // Cria a mensagem de e-mail
            Message message = Message.builder()
                    .subject(Content.builder().data(emailData.subject()).build())
                    .body(Body.builder()
                            .html(Content.builder().data(htmlBody).build())
                            .build())
                    .build();

            // Cria a requisição de envio
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(Destination.builder()
                            .toAddresses(recipientEmail)
                            .build())
                    .message(message)
                    .build();

            // Envia o e-mail
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

    /**
     * Gera o HTML do e-mail usando template Thymeleaf
     */
    private String generateEmailHtml(NotificationEmailDTO emailData) {
        Context context = new Context();

        if (emailData.isReportNotification()) {
            // Template para relatório semanal
            context.setVariable("reportLink", emailData.reportLink());
            context.setVariable("totalFeedbacks", emailData.totalFeedbacks());
            context.setVariable("averageScore", String.format("%.2f", emailData.averageScore()));
            context.setVariable("generatedAt", emailData.sentDate().format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            ));
            return templateEngine.process("weekly-report-email", context);
        } else {
            // Template para feedback crítico
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
