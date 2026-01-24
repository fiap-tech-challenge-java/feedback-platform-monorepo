package br.com.postech.feedback.ingestion.domain.mapper;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.ingestion.domain.FeedbackResponse;
import br.com.postech.feedback.ingestion.domain.dto.CreateFeedback;
import br.com.postech.feedback.ingestion.domain.dto.FeedbackRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FeedbackInjectionApiMapper {
    FeedbackInjectionApiMapper INSTANCE = Mappers.getMapper(FeedbackInjectionApiMapper.class);

    CreateFeedback mapToCreateFeedback(FeedbackRequest feedbackRequest);
    FeedbackResponse mapToFeedbackResponse(Feedback feedback);
}
