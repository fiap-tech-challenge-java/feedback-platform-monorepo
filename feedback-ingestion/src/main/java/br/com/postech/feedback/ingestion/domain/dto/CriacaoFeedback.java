package br.com.postech.feedback.ingestion.domain.dto;

public record CriacaoFeedback(
        String description,
        Integer rating
) {
}
