package br.com.postech.feedback.core.dto;

import br.com.postech.feedback.core.domain.StatusFeedback;
import java.io.Serializable;
import java.time.LocalDateTime;

// Record do Java 17+ (Immutable and lightweight)
public record FeedbackEventDTO(
        Long id,
        String description,
        Integer rating,
        StatusFeedback status,
        LocalDateTime createdAt
) implements Serializable {
}