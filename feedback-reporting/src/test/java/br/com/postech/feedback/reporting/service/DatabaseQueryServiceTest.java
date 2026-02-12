package br.com.postech.feedback.reporting.service;
import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.repository.FeedbackRepository;
import br.com.postech.feedback.reporting.dto.ReportMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseQueryService Tests")
class DatabaseQueryServiceTest {
    @Mock
    private FeedbackRepository feedbackRepository;
    private DatabaseQueryService service;
    @BeforeEach
    void setUp() {
        service = new DatabaseQueryService(feedbackRepository);
    }
    private Feedback createFeedback(Long id, String desc, Integer rating, StatusFeedback status, LocalDateTime createdAt) {
        Feedback feedback = new Feedback();
        feedback.setId(id);
        feedback.setDescription(desc);
        feedback.setRating(rating);
        feedback.setStatus(status);
        feedback.setCreatedAt(createdAt);
        return feedback;
    }
    @Nested
    @DisplayName("fetchMetrics() Tests")
    class FetchMetricsTests {
        @Test
        @DisplayName("Should fetch metrics successfully")
        void shouldFetchMetricsSuccessfully() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Great", 5, StatusFeedback.NORMAL, LocalDateTime.of(2026, 2, 9, 10, 0)));
            feedbacks.add(createFeedback(2L, "Bad", 2, StatusFeedback.CRITICAL, LocalDateTime.of(2026, 2, 9, 11, 0)));
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(2L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(3.5);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);
            ReportMetrics result = service.fetchMetrics();
            assertNotNull(result);
            assertEquals(2L, result.getTotalFeedbacks());
            assertEquals(3.5, result.getAverageScore());
            assertNotNull(result.getFeedbacksByDay());
            assertNotNull(result.getFeedbacksByUrgency());
        }
        @Test
        @DisplayName("Should group feedbacks by day")
        void shouldGroupFeedbacksByDay() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "F1", 5, StatusFeedback.NORMAL, LocalDateTime.of(2026, 2, 9, 10, 0)));
            feedbacks.add(createFeedback(2L, "F2", 4, StatusFeedback.NORMAL, LocalDateTime.of(2026, 2, 9, 11, 0)));
            feedbacks.add(createFeedback(3L, "F3", 3, StatusFeedback.CRITICAL, LocalDateTime.of(2026, 2, 10, 10, 0)));
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(3L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(4.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);
            ReportMetrics result = service.fetchMetrics();
            assertEquals(2, result.getFeedbacksByDay().size());
            assertEquals(2L, result.getFeedbacksByDay().get("2026-02-09"));
            assertEquals(1L, result.getFeedbacksByDay().get("2026-02-10"));
        }
        @Test
        @DisplayName("Should group feedbacks by urgency")
        void shouldGroupFeedbacksByUrgency() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Great", 5, StatusFeedback.NORMAL, LocalDateTime.now()));
            feedbacks.add(createFeedback(2L, "Bad", 1, StatusFeedback.CRITICAL, LocalDateTime.now()));
            feedbacks.add(createFeedback(3L, "Medium", 3, StatusFeedback.NORMAL, LocalDateTime.now()));
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(3L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(3.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);
            ReportMetrics result = service.fetchMetrics();
            assertNotNull(result.getFeedbacksByUrgency());
            assertTrue(result.getFeedbacksByUrgency().containsKey("HIGH"));
            assertTrue(result.getFeedbacksByUrgency().containsKey("LOW"));
        }
        @Test
        @DisplayName("Should build feedback details")
        void shouldBuildFeedbackDetails() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Test feedback", 4, StatusFeedback.NORMAL, LocalDateTime.now()));
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(4.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);
            ReportMetrics result = service.fetchMetrics();
            assertNotNull(result.getFeedbacks());
            assertEquals(1, result.getFeedbacks().size());
            assertEquals("Test feedback", result.getFeedbacks().get(0).getDescription());
        }
        @Test
        @DisplayName("Should handle empty feedbacks")
        void shouldHandleEmptyFeedbacks() {
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(0L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(0.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(new ArrayList<>());
            ReportMetrics result = service.fetchMetrics();
            assertEquals(0L, result.getTotalFeedbacks());
            assertEquals(0.0, result.getAverageScore());
            assertTrue(result.getFeedbacks().isEmpty());
        }
    }
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        @Test
        @DisplayName("Should throw RuntimeException when database fails")
        void shouldThrowRuntimeExceptionWhenDatabaseFails() {
            when(feedbackRepository.countTotalFeedbacks())
                    .thenThrow(new RuntimeException("Database connection failed"));
            assertThrows(RuntimeException.class, () -> service.fetchMetrics());
        }

        @Test
        @DisplayName("Should throw RuntimeException when calculateAverageScore fails")
        void shouldThrowRuntimeExceptionWhenCalculateAverageScoreFails() {
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(5L);
            when(feedbackRepository.calculateAverageScore())
                    .thenThrow(new RuntimeException("Query execution failed"));

            assertThrows(RuntimeException.class, () -> service.fetchMetrics());
        }

        @Test
        @DisplayName("Should throw RuntimeException when findAllFeedbacksForReport fails")
        void shouldThrowRuntimeExceptionWhenFindAllFeedbacksForReportFails() {
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(5L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(4.0);
            when(feedbackRepository.findAllFeedbacksForReport())
                    .thenThrow(new RuntimeException("Query timeout"));

            assertThrows(RuntimeException.class, () -> service.fetchMetrics());
        }
    }

    @Nested
    @DisplayName("Urgency Mapping Tests")
    class UrgencyMappingTests {
        @Test
        @DisplayName("Should map rating 1 to HIGH urgency")
        void shouldMapRating1ToHighUrgency() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Very bad", 1, StatusFeedback.CRITICAL, LocalDateTime.now()));

            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(1.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);

            ReportMetrics result = service.fetchMetrics();

            assertEquals(1L, result.getFeedbacksByUrgency().get("HIGH"));
        }

        @Test
        @DisplayName("Should map rating 2 to HIGH urgency")
        void shouldMapRating2ToHighUrgency() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Bad", 2, StatusFeedback.CRITICAL, LocalDateTime.now()));

            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(2.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);

            ReportMetrics result = service.fetchMetrics();

            assertEquals(1L, result.getFeedbacksByUrgency().get("HIGH"));
        }

        @Test
        @DisplayName("Should map rating 3 to MEDIUM urgency")
        void shouldMapRating3ToMediumUrgency() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Below average", 3, StatusFeedback.NORMAL, LocalDateTime.now()));

            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(3.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);

            ReportMetrics result = service.fetchMetrics();

            assertEquals(1L, result.getFeedbacksByUrgency().get("MEDIUM"));
        }

        @Test
        @DisplayName("Should map rating 4 to MEDIUM urgency")
        void shouldMapRating4ToMediumUrgency() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Average", 4, StatusFeedback.NORMAL, LocalDateTime.now()));

            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(4.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);

            ReportMetrics result = service.fetchMetrics();

            assertEquals(1L, result.getFeedbacksByUrgency().get("MEDIUM"));
        }

        @Test
        @DisplayName("Should map rating 5 to LOW urgency")
        void shouldMapRating5ToLowUrgency() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Good", 5, StatusFeedback.NORMAL, LocalDateTime.now()));

            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(5.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);

            ReportMetrics result = service.fetchMetrics();

            assertEquals(1L, result.getFeedbacksByUrgency().get("LOW"));
        }

        @Test
        @DisplayName("Should map CRITICAL status to HIGH urgency regardless of rating")
        void shouldMapCriticalStatusToHighUrgencyRegardlessOfRating() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Critical", 5, StatusFeedback.CRITICAL, LocalDateTime.now()));

            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(5.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);

            ReportMetrics result = service.fetchMetrics();

            assertEquals(1L, result.getFeedbacksByUrgency().get("HIGH"));
        }
    }

    @Nested
    @DisplayName("Average Score Rounding Tests")
    class AverageScoreRoundingTests {
        @Test
        @DisplayName("Should round average score to two decimal places")
        void shouldRoundAverageScoreToTwoDecimalPlaces() {
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(4.567);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(new ArrayList<>());

            ReportMetrics result = service.fetchMetrics();

            assertEquals(4.57, result.getAverageScore());
        }

        @Test
        @DisplayName("Should handle whole numbers correctly")
        void shouldHandleWholeNumbersCorrectly() {
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(5.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(new ArrayList<>());

            ReportMetrics result = service.fetchMetrics();

            assertEquals(5.0, result.getAverageScore());
        }
    }

    @Nested
    @DisplayName("Feedback Details ISO Format Tests")
    class FeedbackDetailsIsoFormatTests {
        @Test
        @DisplayName("Should format createdAt in ISO format")
        void shouldFormatCreatedAtInIsoFormat() {
            LocalDateTime specificDate = LocalDateTime.of(2026, 2, 9, 14, 30, 45);
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Test", 5, StatusFeedback.NORMAL, specificDate));

            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(5.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);

            ReportMetrics result = service.fetchMetrics();

            assertEquals("2026-02-09T14:30:45Z", result.getFeedbacks().get(0).getCreatedAt());
        }

        @Test
        @DisplayName("Should handle null createdAt gracefully")
        void shouldHandleNullCreatedAtGracefully() {
            List<Feedback> feedbacks = new ArrayList<>();
            feedbacks.add(createFeedback(1L, "Test", 5, StatusFeedback.NORMAL, null));

            when(feedbackRepository.countTotalFeedbacks()).thenReturn(1L);
            when(feedbackRepository.calculateAverageScore()).thenReturn(5.0);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(feedbacks);

            ReportMetrics result = service.fetchMetrics();

            assertNull(result.getFeedbacks().get(0).getCreatedAt());
        }
    }
}
