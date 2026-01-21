package br.com.postech.feedback.ingestion.domain;

import br.com.postech.feedback.core.domain.StatusFeedback;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        String description,
        Integer rating,
        StatusFeedback status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String userId,
        String productId
) {
}
