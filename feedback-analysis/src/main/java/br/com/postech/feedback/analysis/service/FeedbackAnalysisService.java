package br.com.postech.feedback.analysis.service;

import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener; // teste local
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

    @Value("${aws.sns.topic.arn:}")
    private String topicArn;

    // ✅ MODO 1: AWS LAMBDA (Produção)
    // O Spring Cloud Function usa isso quando deployado na AWS
    @Bean
    public Consumer<FeedbackEventDTO> analyzeFeedback() {
        return this::processarFeedback;
    }

    // ✅ MODO 2: LOCALSTACK (Desenvolvimento Local)
    // O @SqsListener fica "escutando" a fila ativamente enquanto roda o app no IntelliJ
    @SqsListener("feedback-analysis-queue")
    public void listen(FeedbackEventDTO event) {
        log.info("LOCAL: Mensagem capturada via SqsListener");
        processarFeedback(event);
    }

    // 🧠 Lógica Central (Compartilhada pelos dois modos)
    private void processarFeedback(FeedbackEventDTO event) {
        log.info("Iniciando análise do feedback ID: {}", event.id());

        if (StatusFeedback.CRITICAL.equals(event.status())) {
            log.warn("Feedback CRÍTICO detectado (Nota: {}). Enviando para SNS...", event.rating());
            sendToSns(event);
        } else {
            log.info("Feedback analisado como NORMAL. Nenhuma ação necessária.");
        }
    }

    private void sendToSns(FeedbackEventDTO event) {
        if (topicArn == null || topicArn.isBlank()) {
            log.warn("ARN do SNS não configurado. Notificação ignorada.");
            return;
        }
        try {
            String messageBody = objectMapper.writeValueAsString(event);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .subject("ALERTA: Novo Feedback Crítico")
                    .message(messageBody)
                    .build();

            snsClient.publish(request);
            log.info("Notificação enviada com sucesso para o ARN: {}", topicArn);
        } catch (Exception e) {
            log.error("Erro ao publicar no SNS", e);
        }
    }
}