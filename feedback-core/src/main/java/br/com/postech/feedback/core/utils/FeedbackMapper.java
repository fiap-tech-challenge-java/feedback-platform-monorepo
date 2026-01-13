package br.com.postech.feedback.core.utils;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FeedbackMapper {

    public static FeedbackEventDTO toEvent(Feedback feedback) {
        if (feedback == null) return null;

        return new FeedbackEventDTO(
                feedback.getId(),
                feedback.getDescription(),
                feedback.getRating(),
                feedback.getStatus(),
                feedback.getCreatedAt()
        );
    }
}