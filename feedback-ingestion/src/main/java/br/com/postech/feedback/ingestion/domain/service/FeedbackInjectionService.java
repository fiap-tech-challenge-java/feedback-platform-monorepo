package br.com.postech.feedback.ingestion.domain.service;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import br.com.postech.feedback.core.repository.FeedbackRepository;
import br.com.postech.feedback.core.utils.FeedbackMapper;
import br.com.postech.feedback.ingestion.domain.dto.CriacaoFeedback;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.stereotype.Service;

@Service
public class FeedbackInjectionService {
    private final FeedbackRepository feedbackRepository;
    private final SqsTemplate sqsTemplate;

    public FeedbackInjectionService(FeedbackRepository feedbackRepository, SqsTemplate sqsTemplate) {
        this.feedbackRepository = feedbackRepository;
        this.sqsTemplate = sqsTemplate;
    }

    public Feedback processFeedback(CriacaoFeedback criacaoFeedback) {
        Feedback feedback = new Feedback(
                criacaoFeedback.description(),
                criacaoFeedback.rating()
        );

        feedbackRepository.save(feedback);

        try {
            FeedbackEventDTO feedbackEventDTO = FeedbackMapper.toEvent(feedback);
            sqsTemplate.send(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS, feedbackEventDTO);

            return feedback;
        } catch (Exception e) {
            throw e;
        }
    }

}
