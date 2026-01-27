package br.com.postech.feedback.notification.service;

import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import br.com.postech.feedback.notification.dto.NotificationEmailDTO;
import br.com.postech.feedback.notification.dto.NotificationResponseDTO;
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

    @Value("${aws.ses.recipient-email}")
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
                FeedbackEventDTO feedbackEvent = extractFeedbackEvent(snsMessage);

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
     * Extrai o FeedbackEventDTO da mensagem, suportando:
     * 1. Formato SNS (com campo "Message")
     * 2. FeedbackEventDTO direto (para testes)
     */
    private FeedbackEventDTO extractFeedbackEvent(String message) throws Exception {
        try {
            // Tenta parsear como JSON
            JsonNode rootNode = objectMapper.readTree(message);

            // Verifica se é uma mensagem SNS (tem campo "Message")
            if (rootNode.has("Message")) {
                log.debug("Detectado formato SNS");
                String messageBody = rootNode.path("Message").asText();

                // Parse do conteúdo real (FeedbackEventDTO)
                return objectMapper.readValue(messageBody, FeedbackEventDTO.class);
            } else {
                // Assume que é FeedbackEventDTO direto
                log.debug("Detectado formato FeedbackEventDTO direto");
                return objectMapper.readValue(message, FeedbackEventDTO.class);
            }
        } catch (Exception e) {
            log.error("Erro ao fazer parse da mensagem: {}", message, e);
            throw e;
        }
    }

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
