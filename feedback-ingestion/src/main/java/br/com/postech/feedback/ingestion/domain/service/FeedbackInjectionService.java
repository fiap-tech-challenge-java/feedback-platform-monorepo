package br.com.postech.feedback.ingestion.domain.service;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import br.com.postech.feedback.core.repository.FeedbackRepository;
import br.com.postech.feedback.core.utils.FeedbackMapper;
import br.com.postech.feedback.ingestion.domain.dto.CreateFeedback;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // <--- IMPORTANTE
import org.springframework.stereotype.Service;

@Service
public class FeedbackInjectionService {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackInjectionService.class);

    private final FeedbackRepository feedbackRepository;
    private final SqsTemplate sqsTemplate;

    // 1. Injetar a URL completa que está no application-prod.yaml
    @Value("${app.sqs.queue-url}")
    private String queueUrl;

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

        // Salvar no Banco
        logger.info("💾 [DATABASE] Iniciando salvamento no PostgreSQL...");
        feedbackRepository.save(feedback);
        logger.info("✅ [DATABASE] Feedback salvo! ID: {}", feedback.getId());

        // Enviar para SQS
        try {
            logger.info("📤 [SQS] Enviando para URL: '{}'", queueUrl); // Log para conferir
            FeedbackEventDTO feedbackEventDTO = FeedbackMapper.toEvent(feedback);

            // 2. USAR A VARIÁVEL queueUrl AQUI (Com .join() para garantir)
            var resultado = sqsTemplate.send(to -> to
                    .queue(queueUrl) // <--- O PULO DO GATO: Usa a URL completa, não o nome
                    .payload(feedbackEventDTO)
            );

            // O SqsTemplate do Spring Cloud AWS 3.x já é síncrono por padrão,
            // mas se ele retornar um CompletableFuture no futuro, o .join() garantiria.
            // Do jeito que está, apenas passar a URL correta deve resolver o erro "MessagingOperationFailed".

            logger.info("✅ [SQS] Enviado com sucesso! ID: {}", feedback.getId());
            return feedback;
        } catch (Exception e) {
            logger.error("❌ [SQS] FALHA FATAL: {}", e.getMessage(), e);
            // Não relançar erro para não travar o retorno HTTP, já que salvou no banco?
            // Depende da sua regra. Se SQS falhar, o cliente deve saber?
            // Para o desafio, pode deixar o throw e.
            throw e;
        }
    }
}