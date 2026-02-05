package br.com.postech.feedback.analysis.service;

import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackAnalysisService {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;

    @Value("${SNS_TOPIC_ARN:}")
    private String topicArn;

    private void validateTopicArn() {
        if (topicArn == null || topicArn.isBlank()) {
            log.error("❌ [CONFIG ERROR] SNS_TOPIC_ARN não está configurada!");
            log.error("Configure a variável de ambiente SNS_TOPIC_ARN com o ARN completo do tópico SNS");
            log.error("Exemplo: arn:aws:sns:us-east-2:123456789012:feedback-notifications");
            throw new IllegalStateException(
                "SNS_TOPIC_ARN environment variable is not configured. " +
                "Please set it to the full SNS topic ARN (e.g., arn:aws:sns:us-east-2:ACCOUNT_ID/TOPIC_NAME)"
            );
        }
    }

    /**
     * ✅ MODO 1: AWS LAMBDA (Produção)
     * Recebe o evento bruto do SQS (SQSEvent), itera sobre os registros (batch)
     * e processa cada um individualmente.
     */
    @Bean
    public Consumer<SQSEvent> analyzeFeedback() {
        return event -> {
            log.info("⚡ [LAMBDA] Recebido lote com {} mensagens do SQS", event.getRecords().size());

            event.getRecords().forEach(record -> {
                try {
                    log.info("📩 Processando mensagem ID: {}", record.getMessageId());

                    String body = record.getBody();
                    FeedbackEventDTO dto = objectMapper.readValue(body, FeedbackEventDTO.class);
                    processarFeedback(dto);

                } catch (JsonProcessingException e) {
                    log.error("❌ Erro ao converter JSON da mensagem: {}", e.getMessage(), e);
                } catch (Exception e) {
                    log.error("❌ Erro genérico ao processar mensagem: {}", e.getMessage(), e);
                    throw new RuntimeException("Erro no processamento da Lambda", e);
                }
            });
        };
    }

    /**
     * ✅ MODO 2: LOCALSTACK / DESENVOLVIMENTO
     * O @SqsListener do Spring Cloud AWS já entrega o objeto convertido.
     */
    @SqsListener("feedback-analysis-queue")
    public void listen(FeedbackEventDTO event) {
        log.info("💻 [LOCAL] Mensagem capturada via SqsListener");
        processarFeedback(event);
    }

    private void processarFeedback(FeedbackEventDTO event) {
        log.info("🔍 Analisando feedback ID: {} | Status: {}", event.id(), event.status());

        if (StatusFeedback.CRITICAL.equals(event.status())) {
            log.warn("🚨 Feedback CRÍTICO detectado (Nota: {}). Enviando alerta...", event.rating());
            sendToSns(event);
        } else {
            log.info("👍 Feedback analisado como NORMAL. Nenhuma ação necessária.");
        }
    }

    private void sendToSns(FeedbackEventDTO event) {
        validateTopicArn();
        
        try {
            String messageBody = objectMapper.writeValueAsString(event);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject("ALERTA: Novo Feedback Crítico")
                    .message(messageBody)
                    .build();

            snsClient.publish(request);
            log.info("✅ [SNS] Notificação enviada com sucesso! ARN: {}", topicArn);
        } catch (Exception e) {
            log.error("❌ [SNS] Falha ao publicar notificação", e);
            throw new RuntimeException("Erro na publicação SNS", e);
        }
    }
}