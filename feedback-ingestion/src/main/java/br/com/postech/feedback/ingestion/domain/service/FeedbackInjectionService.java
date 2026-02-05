package br.com.postech.feedback.ingestion.domain.service;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import br.com.postech.feedback.core.repository.FeedbackRepository;
import br.com.postech.feedback.core.utils.FeedbackMapper;
import br.com.postech.feedback.ingestion.domain.dto.CreateFeedback;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class FeedbackInjectionService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackInjectionService.class);

    private final FeedbackRepository feedbackRepository;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${SQS_QUEUE_URL:}")
    private String queueUrl;

    public FeedbackInjectionService(FeedbackRepository feedbackRepository,
                                    SqsClient sqsClient,
                                    ObjectMapper objectMapper) {
        this.feedbackRepository = feedbackRepository;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    private void validateConfiguration() {
        if (queueUrl == null || queueUrl.isBlank()) {
            logger.error("❌ [CONFIG ERROR] SQS_QUEUE_URL não está configurada!");
            logger.error("Configure a variável de ambiente SQS_QUEUE_URL com a URL completa da fila SQS");
            logger.error("Exemplo: https://sqs.us-east-2.amazonaws.com/123456789012/feedback-analysis-queue");
            throw new IllegalStateException(
                "SQS_QUEUE_URL environment variable is not configured. " +
                "Please set it to the full SQS queue URL (e.g., https://sqs.us-east-2.amazonaws.com/ACCOUNT_ID/QUEUE_NAME)"
            );
        }
    }

    public Feedback processFeedback(CreateFeedback createFeedback) {
        validateConfiguration();
        
        logger.info("📝 [INGESTION] Feedback recebido - description: '{}', rating: {}",
                createFeedback.description(), createFeedback.rating());

        Feedback feedback = new Feedback(
                createFeedback.description(),
                createFeedback.rating()
        );

        logger.info("💾 [DATABASE] Iniciando salvamento no PostgreSQL...");
        feedbackRepository.save(feedback);
        logger.info("✅ [DATABASE] Feedback salvo! ID: {}", feedback.getId());

        try {
            logger.info("📤 [SQS] Preparando envio para URL: '{}'", queueUrl);

            FeedbackEventDTO feedbackEventDTO = FeedbackMapper.toEvent(feedback);
            String messageBody = objectMapper.writeValueAsString(feedbackEventDTO);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            var response = sqsClient.sendMessage(sendMsgRequest);

            logger.info("✅ [SQS] Enviado com sucesso! MessageId: {}", response.messageId());

            return feedback;

        } catch (JsonProcessingException e) {
            logger.error("❌ [SQS] Erro ao converter objeto para JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Erro na serialização do feedback", e);

        } catch (Exception e) {
            logger.error("❌ [SQS] FALHA FATAL ao comunicar com AWS: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar mensagem para o SQS", e);
        }
    }
}