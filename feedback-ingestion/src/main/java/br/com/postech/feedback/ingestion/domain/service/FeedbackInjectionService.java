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
    private final SqsClient sqsClient; // Cliente nativo configurado no AwsConfig
    private final ObjectMapper objectMapper; // Para converter objeto em JSON string

    // Injeção da URL da fila definida no application.yaml
    @Value("${app.sqs.queue-url}")
    private String queueUrl;

    public FeedbackInjectionService(FeedbackRepository feedbackRepository,
                                    SqsClient sqsClient,
                                    ObjectMapper objectMapper) {
        this.feedbackRepository = feedbackRepository;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public Feedback processFeedback(CreateFeedback createFeedback) {
        logger.info("📝 [INGESTION] Feedback recebido - description: '{}', rating: {}",
                createFeedback.description(), createFeedback.rating());

        // 1. Converter DTO para Entidade
        Feedback feedback = new Feedback(
                createFeedback.description(),
                createFeedback.rating()
        );

        // 2. Salvar no Banco (PostgreSQL)
        logger.info("💾 [DATABASE] Iniciando salvamento no PostgreSQL...");
        feedbackRepository.save(feedback);
        logger.info("✅ [DATABASE] Feedback salvo! ID: {}", feedback.getId());

        // 3. Enviar para SQS
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
            // Em arquitetura serverless orientada a eventos, se falhar o envio da mensagem,
            // geralmente queremos que a Lambda falhe para o DLQ capturar ou ocorrer retry.
            throw new RuntimeException("Erro ao enviar mensagem para o SQS", e);
        }
    }
}