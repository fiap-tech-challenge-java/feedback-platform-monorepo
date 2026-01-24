package br.com.postech.feedback.ingestion.domain.dto;

public record CreateFeedback(
        String description,
        Integer rating
) {
}
