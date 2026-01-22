package br.com.postech.feedback.analysis.service;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // Injeta o valor do application.yaml. Se não existir, entra como vazio/null
    @Value("${aws.sns.topic.arn:}")
    private String topicArn;

    @Bean
    public Consumer<FeedbackEventDTO> analyzeFeedback() {
        return event -> {
            log.info("Iniciando análise do feedback ID: {}", event.id());

            // 1. REUTILIZAÇÃO DO DOMÍNIO (CORE):
            Feedback feedbackDomain = new Feedback(
                    event.description(),
                    event.rating()
            );

            StatusFeedback statusCalculado = feedbackDomain.getStatus();

            // 2. CRIAÇÃO DE NOVO DTO (Imutabilidade do Record):
            FeedbackEventDTO eventWithStatus = new FeedbackEventDTO(
                    event.id(),
                    event.description(),
                    event.rating(),
                    statusCalculado,
                    event.createdAt()
            );

            if (StatusFeedback.CRITICAL.equals(statusCalculado)) {
                log.warn("Feedback CRÍTICO detectado (Nota: {}). Enviando para SNS...", event.rating());
                sendToSns(eventWithStatus); // Envia o DTO já com o status preenchido
            } else {
                logToCloudWatch(eventWithStatus);
            }
        };
    }

    private void sendToSns(FeedbackEventDTO event) {

        // SEGURANÇA: Validação explícita antes de tentar enviar
        if (topicArn == null || topicArn.isBlank()) {
            throw new IllegalStateException("ERRO DE CONFIGURAÇÃO: O ARN do tópico SNS não foi definido. " +
                    "Verifique se a variável 'aws.sns.topic.arn' está configurada corretamente no ambiente.");
        }

        try {
            String messageBody = objectMapper.writeValueAsString(event);

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn) // Usa estritamente o que foi configurado
                    .subject("ALERTA: Novo Feedback Crítico Recebido")
                    .message(messageBody)
                    .build();

            snsClient.publish(request);
            log.info("Notificação enviada com sucesso para o ARN: {}", topicArn);

        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar JSON", e);
        } catch (Exception e) {
            log.error("Erro ao publicar mensagem no SNS", e);
        }
    }

    private void logToCloudWatch(FeedbackEventDTO event) {
        log.info("Feedback analisado como NORMAL. Nenhuma ação necessária. [Status: {}, Nota: {}]",
                event.status(), event.rating());
    }
}