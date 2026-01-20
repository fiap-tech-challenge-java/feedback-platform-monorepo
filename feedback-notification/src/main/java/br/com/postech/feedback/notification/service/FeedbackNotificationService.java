package br.com.postech.feedback.notification.service;

import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import br.com.postech.feedback.notification.dto.NotificationEmailDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.function.Consumer;

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

    @Value("${aws.ses.sender-email}")
    private String senderEmail;

    @Value("${aws.ses.recipient-email}")
    private String recipientEmail;

    @Value("${aws.ses.enabled:true}")
    private boolean sesEnabled;

    /**
     * Bean que define a função serverless para consumir mensagens do SNS
     * Esta função é acionada automaticamente quando uma mensagem chega ao tópico SNS
     */
    @Bean
    public Consumer<String> processNotification() {
        return snsMessage -> {
            log.info("Recebendo mensagem para processamento de notificação");
            log.debug("Payload recebido: {}", snsMessage);

            // Validação básica: ignorar payloads vazios ou não-JSON
            if (snsMessage == null || snsMessage.trim().isEmpty()) {
                log.warn("Payload vazio recebido. Ignorando...");
                return;
            }

            // Ignorar requisições GET ou payloads que claramente não são JSON
            if (!snsMessage.trim().startsWith("{") && !snsMessage.trim().startsWith("[")) {
                log.warn("Payload não-JSON recebido: '{}'. Ignorando...", snsMessage);
                return;
            }

            try {
                FeedbackEventDTO feedbackEvent = extractFeedbackEvent(snsMessage);

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
                sendEmail(emailData);

            } catch (Exception e) {
                log.error("Erro ao processar notificação. Payload: {}", snsMessage, e);
                throw new RuntimeException("Falha ao processar notificação", e);
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
     */
    private void sendEmail(NotificationEmailDTO emailData) {
        if (!sesEnabled) {
            log.warn("SES está desabilitado. E-mail não será enviado. Dados: {}", emailData);
            return;
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

        } catch (SesException e) {
            log.error("Erro ao enviar e-mail via SES. Código: {} | Mensagem: {}",
                    e.awsErrorDetails().errorCode(), e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Falha ao enviar e-mail", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao enviar e-mail", e);
            throw new RuntimeException("Falha ao enviar e-mail", e);
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
