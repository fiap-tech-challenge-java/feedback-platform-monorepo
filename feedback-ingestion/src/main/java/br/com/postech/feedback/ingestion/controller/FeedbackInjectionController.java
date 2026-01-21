package br.com.postech.feedback.ingestion.controller;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.ingestion.domain.FeedbackResponse;
import br.com.postech.feedback.ingestion.domain.dto.FeedbackRequest;
import br.com.postech.feedback.ingestion.domain.mapper.FeedbackInjectionApiMapper;
import br.com.postech.feedback.ingestion.domain.service.FeedbackInjectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("/rest/feedback")
public class FeedbackInjectionController {

    private final FeedbackInjectionService feedbackInjectionService;
    private final FeedbackInjectionApiMapper MAPPER_FEEDBACK_INJECTION = FeedbackInjectionApiMapper.INSTANCE;

    public FeedbackInjectionController(FeedbackInjectionService feedbackInjectionService) {
        this.feedbackInjectionService = feedbackInjectionService;
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> feedbackInjection(@RequestBody FeedbackRequest feedbackRequest) {
        Feedback feedback = this.feedbackInjectionService.processFeedback(
                MAPPER_FEEDBACK_INJECTION.mapToCriacaoFeedback(feedbackRequest)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                MAPPER_FEEDBACK_INJECTION.mapToFeedbackResponse(feedback)
        );
    }

}
