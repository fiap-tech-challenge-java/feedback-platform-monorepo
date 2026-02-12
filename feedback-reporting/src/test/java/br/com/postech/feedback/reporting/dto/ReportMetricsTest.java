package br.com.postech.feedback.reporting.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportMetricsTest {

    @Test
    @DisplayName("Deve criar ReportMetrics com todos os campos obrigatórios do Tech Challenge")
    void shouldCreateReportMetricsWithAllRequiredFields() {
        // Arrange
        Map<String, Long> feedbacksByDay = new HashMap<>();
        feedbacksByDay.put("2026-01-10", 12L);
        feedbacksByDay.put("2026-01-11", 7L);

        Map<String, Long> feedbacksByUrgency = new HashMap<>();
        feedbacksByUrgency.put("LOW", 15L);
        feedbacksByUrgency.put("MEDIUM", 3L);
        feedbacksByUrgency.put("HIGH", 1L);

        List<FeedbackDetail> feedbacks = new ArrayList<>();
        feedbacks.add(FeedbackDetail.builder()
                .description("Ótima aula!")
                .urgency("LOW")
                .createdAt("2026-01-10T10:30:00Z")
                .build());

        // Act
        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(19L)
                .averageScore(7.5)
                .feedbacksByDay(feedbacksByDay)
                .feedbacksByUrgency(feedbacksByUrgency)
                .feedbacks(feedbacks)
                .build();

        // Assert
        assertThat(metrics.getTotalFeedbacks()).isEqualTo(19L);
        assertThat(metrics.getAverageScore()).isEqualTo(7.5);
        assertThat(metrics.getFeedbacksByDay()).hasSize(2);
        assertThat(metrics.getFeedbacksByUrgency()).hasSize(3);
        assertThat(metrics.getFeedbacks()).hasSize(1);
    }

    @Test
    @DisplayName("Deve criar FeedbackDetail com descrição, urgência e data")
    void shouldCreateFeedbackDetailWithRequiredFields() {
        FeedbackDetail detail = FeedbackDetail.builder()
                .description("Precisa melhorar o conteúdo")
                .urgency("HIGH")
                .createdAt("2026-01-15T14:00:00Z")
                .build();

        assertThat(detail.getDescription()).isEqualTo("Precisa melhorar o conteúdo");
        assertThat(detail.getUrgency()).isEqualTo("HIGH");
        assertThat(detail.getCreatedAt()).isEqualTo("2026-01-15T14:00:00Z");
    }

    @Test
    @DisplayName("Deve permitir valores vazios nas coleções")
    void shouldAllowEmptyCollections() {
        ReportMetrics metrics = ReportMetrics.builder()
                .totalFeedbacks(0L)
                .averageScore(0.0)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();

        assertThat(metrics.getTotalFeedbacks()).isZero();
        assertThat(metrics.getAverageScore()).isZero();
        assertThat(metrics.getFeedbacksByDay()).isEmpty();
        assertThat(metrics.getFeedbacksByUrgency()).isEmpty();
        assertThat(metrics.getFeedbacks()).isEmpty();
    }
}
