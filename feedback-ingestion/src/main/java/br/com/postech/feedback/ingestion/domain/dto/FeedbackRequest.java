package br.com.postech.feedback.ingestion.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        @NotBlank(message = "Description não pode ser nula ou vazia")
        String description,

        @NotNull(message = "Rating não pode ser nulo")
        @Min(value = 0, message = "Rating deve ser maior ou igual a zero")
        @Max(value = 10, message = "Rating deve ser no máximo 10")
        Integer rating
) { }

