package br.com.postech.feedback.ingestion.domain.service;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.repository.FeedbackRepository;
import br.com.postech.feedback.ingestion.domain.dto.CriacaoFeedback;
import org.springframework.stereotype.Service;

@Service
public class FeedbackInjectionService {
    private final FeedbackRepository feedbackRepository;

    public FeedbackInjectionService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback processFeedback(CriacaoFeedback criacaoFeedback) {
        Feedback feedback = new Feedback(
                criacaoFeedback.description(),
                criacaoFeedback.rating(),
                null,
                null
        );

        return feedbackRepository.save(feedback);
    }
}
