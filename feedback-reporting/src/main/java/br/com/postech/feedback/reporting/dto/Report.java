package br.com.postech.feedback.reporting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime generatedAt;

    private String period;

    private ReportSummary summary;

    private Map<String, Long> feedbacksByDay;

    private Map<String, Long> feedbacksByUrgency;

    private List<ReportFeedbackItem> feedbacks;
}
