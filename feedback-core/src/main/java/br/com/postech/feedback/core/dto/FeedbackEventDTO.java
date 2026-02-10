package br.com.postech.feedback.core.dto;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import br.com.postech.feedback.core.domain.StatusFeedback;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

public record FeedbackEventDTO(
        Long id,
        String description,
        Integer rating,
        StatusFeedback status,

        @JsonFormat(pattern = AwsConfigConstants.DATE_PATTERN)
        LocalDateTime createdAt
) implements Serializable {
}