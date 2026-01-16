package br.com.postech.feedback.reporting.service;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.repository.FeedbackRepository;
import br.com.postech.feedback.reporting.dto.FeedbackDetail;
import br.com.postech.feedback.reporting.dto.ReportMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseQueryService {

    private final FeedbackRepository feedbackRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Transactional(readOnly = true)
    public ReportMetrics fetchMetrics() {
        log.info("Fetching metrics from database");

        try {
            Long totalFeedbacks = feedbackRepository.countTotalFeedbacks();
            Double averageScore = feedbackRepository.calculateAverageScore();
            List<Feedback> allFeedbacks = feedbackRepository.findAllFeedbacksForReport();

            Map<String, Long> feedbacksByDay = groupFeedbacksByDay(allFeedbacks);
            Map<String, Long> feedbacksByUrgency = groupFeedbacksByUrgency(allFeedbacks);
            List<FeedbackDetail> feedbackDetails = buildFeedbackDetails(allFeedbacks);

            ReportMetrics metrics = ReportMetrics.builder()
                    .totalFeedbacks(totalFeedbacks)
                    .averageScore(Math.round(averageScore * 100.0) / 100.0)
                    .feedbacksByDay(feedbacksByDay)
                    .feedbacksByUrgency(feedbacksByUrgency)
                    .feedbacks(feedbackDetails)
                    .build();

            log.info("Metrics fetched - Total: {}, Average: {}", metrics.getTotalFeedbacks(), metrics.getAverageScore());
            return metrics;

        } catch (Exception e) {
            log.error("Failed to fetch metrics: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch metrics from database", e);
        }
    }

    private Map<String, Long> groupFeedbacksByDay(List<Feedback> feedbacks) {
        return feedbacks.stream()
                .filter(f -> f.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        f -> f.getCreatedAt().format(DATE_FORMATTER),
                        Collectors.counting()
                ));
    }

    private Map<String, Long> groupFeedbacksByUrgency(List<Feedback> feedbacks) {
        Map<String, Long> result = new HashMap<>();
        result.put("LOW", 0L);
        result.put("MEDIUM", 0L);
        result.put("HIGH", 0L);

        for (Feedback feedback : feedbacks) {
            String urgency = mapStatusToUrgency(feedback.getStatus(), feedback.getRating());
            result.merge(urgency, 1L, Long::sum);
        }
        return result;
    }

    private String mapStatusToUrgency(StatusFeedback status, Integer rating) {
        if (status == StatusFeedback.CRITICAL || (rating != null && rating <= 2)) {
            return "HIGH";
        } else if (rating != null && rating <= 4) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<FeedbackDetail> buildFeedbackDetails(List<Feedback> feedbacks) {
        List<FeedbackDetail> details = new ArrayList<>();

        for (Feedback feedback : feedbacks) {
            String urgency = mapStatusToUrgency(feedback.getStatus(), feedback.getRating());
            String createdAt = feedback.getCreatedAt() != null
                    ? feedback.getCreatedAt().format(ISO_FORMATTER)
                    : null;

            details.add(FeedbackDetail.builder()
                    .description(feedback.getDescription())
                    .urgency(urgency)
                    .createdAt(createdAt)
                    .build());
        }
        return details;
    }
}
