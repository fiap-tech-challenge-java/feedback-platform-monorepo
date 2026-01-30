package br.com.postech.feedback.ingestion.controller;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.ingestion.domain.FeedbackResponse;
import br.com.postech.feedback.ingestion.domain.dto.FeedbackRequest;
import br.com.postech.feedback.ingestion.domain.mapper.FeedbackInjectionApiMapper;
import br.com.postech.feedback.ingestion.domain.service.FeedbackInjectionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/feedback")
public class FeedbackInjectionController {

    private static final Logger logger = LoggerFactory.getLogger(FeedbackInjectionController.class);

    private final FeedbackInjectionService feedbackInjectionService;
    private final FeedbackInjectionApiMapper MAPPER_FEEDBACK_INJECTION = FeedbackInjectionApiMapper.INSTANCE;

    public FeedbackInjectionController(FeedbackInjectionService feedbackInjectionService) {
        this.feedbackInjectionService = feedbackInjectionService;
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> feedbackInjection(@Valid @RequestBody FeedbackRequest feedbackRequest) {
        long startTime = System.currentTimeMillis();

        logger.info("🚀 [HTTP] POST /rest/feedback - Requisição recebida");
        logger.debug("🚀 [HTTP] Payload: description='{}', rating={}",
                feedbackRequest.description(), feedbackRequest.rating());

        try {
            Feedback feedback = this.feedbackInjectionService.processFeedback(
                    MAPPER_FEEDBACK_INJECTION.mapToCreateFeedback(feedbackRequest)
            );

            long duration = System.currentTimeMillis() - startTime;
            FeedbackResponse response = MAPPER_FEEDBACK_INJECTION.mapToFeedbackResponse(feedback);

            logger.info("✅ [HTTP] Resposta enviada com sucesso - ID: {}, Status: 201 Created, Duração: {}ms",
                    response.id(), duration);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("❌ [HTTP] Erro ao processar feedback - Status: 500 Internal Server Error, Duração: {}ms, Erro: {}",
                    duration, e.getMessage(), e);
            throw e;
        }
    }

}
