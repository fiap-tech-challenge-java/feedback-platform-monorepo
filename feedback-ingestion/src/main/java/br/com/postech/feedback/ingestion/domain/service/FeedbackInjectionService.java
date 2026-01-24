package br.com.postech.feedback.ingestion.domain.service;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import br.com.postech.feedback.core.repository.FeedbackRepository;
import br.com.postech.feedback.core.utils.FeedbackMapper;
import br.com.postech.feedback.ingestion.domain.dto.CreateFeedback;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FeedbackInjectionService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackInjectionService.class);

    private final FeedbackRepository feedbackRepository;
    private final SqsTemplate sqsTemplate;

    public FeedbackInjectionService(FeedbackRepository feedbackRepository, SqsTemplate sqsTemplate) {
        this.feedbackRepository = feedbackRepository;
        this.sqsTemplate = sqsTemplate;
    }

    public Feedback processFeedback(CreateFeedback createFeedback) {
        logger.info("📝 [INGESTION] Feedback recebido - description: '{}', rating: {}",
                createFeedback.description(), createFeedback.rating());

        Feedback feedback = new Feedback(
                createFeedback.description(),
                createFeedback.rating()
        );

        // 1️⃣ Salvar no PostgreSQL (RDS)
        logger.info("💾 [DATABASE] Iniciando salvamento no PostgreSQL...");
        try {
            feedbackRepository.save(feedback);
            logger.info("✅ [DATABASE] Feedback salvo com sucesso! ID: {}, Status: {}, CreatedAt: {}",
                    feedback.getId(), feedback.getStatus(), feedback.getCreatedAt());
        } catch (Exception e) {
            logger.error("❌ [DATABASE] Erro ao salvar feedback no PostgreSQL: {}", e.getMessage(), e);
            throw e;
        }

        // 2️⃣ Enviar para SQS
        try {
            logger.info("📤 [SQS] Iniciando envio para fila: '{}'", AwsConfigConstants.QUEUE_INGESTION_ANALYSIS);
            FeedbackEventDTO feedbackEventDTO = FeedbackMapper.toEvent(feedback);

            logger.debug("📤 [SQS] Payload a enviar - ID: {}, Description: '{}', Rating: {}, Status: {}",
                    feedbackEventDTO.id(), feedbackEventDTO.description(),
                    feedbackEventDTO.rating(), feedbackEventDTO.status());

            sqsTemplate.send(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS, feedbackEventDTO);
            logger.info("✅ [SQS] Mensagem enviada com sucesso para a fila '{}' - Feedback ID: {}",
                    AwsConfigConstants.QUEUE_INGESTION_ANALYSIS, feedback.getId());

            return feedback;
        } catch (Exception e) {
            logger.error("❌ [SQS] Erro ao enviar mensagem para SQS (fila: '{}'): {}",
                    AwsConfigConstants.QUEUE_INGESTION_ANALYSIS, e.getMessage(), e);
            logger.warn("⚠️  [SQS] Feedback foi salvo no banco (ID: {}) mas falhou no envio para SQS. " +
                    "Será processado manualmente ou por retry mechanism.", feedback.getId());
            throw e;
        }
    }

}
