package br.com.postech.feedback.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetrics {

    private Long totalFeedbacks;
    private Double averageScore;
    private Map<String, Long> feedbacksByDay;
    private Map<String, Long> feedbacksByUrgency;
    private List<FeedbackDetail> feedbacks;
}
