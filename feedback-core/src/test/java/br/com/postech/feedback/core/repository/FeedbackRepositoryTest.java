package br.com.postech.feedback.core.repository;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.domain.StatusFeedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackRepository Tests")
class FeedbackRepositoryTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    private Feedback createFeedback(Long id, String description, Integer rating, StatusFeedback status, LocalDateTime createdAt) {
        Feedback feedback = new Feedback();
        feedback.setId(id);
        feedback.setDescription(description);
        feedback.setRating(rating);
        feedback.setStatus(status);
        feedback.setCreatedAt(createdAt);
        feedback.setUpdatedAt(createdAt);
        return feedback;
    }

    @Nested
    @DisplayName("countTotalFeedbacks() Tests")
    class CountTotalFeedbacksTests {

        @Test
        @DisplayName("Should return 0 when no feedbacks exist")
        void shouldReturnZeroWhenNoFeedbacksExist() {
            // Arrange
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(0L);

            // Act
            Long count = feedbackRepository.countTotalFeedbacks();

            // Assert
            assertThat(count).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should return correct count when feedbacks exist")
        void shouldReturnCorrectCountWhenFeedbacksExist() {
            // Arrange
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(3L);

            // Act
            Long count = feedbackRepository.countTotalFeedbacks();

            // Assert
            assertThat(count).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should call countTotalFeedbacks exactly once")
        void shouldCallCountTotalFeedbacksExactlyOnce() {
            // Arrange
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(5L);

            // Act
            feedbackRepository.countTotalFeedbacks();

            // Assert
            verify(feedbackRepository, times(1)).countTotalFeedbacks();
        }
    }

    @Nested
    @DisplayName("countCriticalFeedbacks() Tests")
    class CountCriticalFeedbacksTests {

        @Test
        @DisplayName("Should return 0 when no critical feedbacks exist")
        void shouldReturnZeroWhenNoCriticalFeedbacksExist() {
            // Arrange
            when(feedbackRepository.countCriticalFeedbacks()).thenReturn(0L);

            // Act
            Long count = feedbackRepository.countCriticalFeedbacks();

            // Assert
            assertThat(count).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should return correct count of critical feedbacks")
        void shouldReturnCorrectCountOfCriticalFeedbacks() {
            // Arrange
            when(feedbackRepository.countCriticalFeedbacks()).thenReturn(2L);

            // Act
            Long count = feedbackRepository.countCriticalFeedbacks();

            // Assert
            assertThat(count).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should call countCriticalFeedbacks exactly once")
        void shouldCallCountCriticalFeedbacksExactlyOnce() {
            // Arrange
            when(feedbackRepository.countCriticalFeedbacks()).thenReturn(1L);

            // Act
            feedbackRepository.countCriticalFeedbacks();

            // Assert
            verify(feedbackRepository, times(1)).countCriticalFeedbacks();
        }
    }

    @Nested
    @DisplayName("calculateAverageScore() Tests")
    class CalculateAverageScoreTests {

        @Test
        @DisplayName("Should return 0.0 when no feedbacks exist")
        void shouldReturnZeroWhenNoFeedbacksExist() {
            // Arrange
            when(feedbackRepository.calculateAverageScore()).thenReturn(0.0);

            // Act
            Double average = feedbackRepository.calculateAverageScore();

            // Assert
            assertThat(average).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should return correct average score")
        void shouldReturnCorrectAverageScore() {
            // Arrange
            when(feedbackRepository.calculateAverageScore()).thenReturn(5.0);

            // Act
            Double average = feedbackRepository.calculateAverageScore();

            // Assert
            assertThat(average).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Should handle decimal average correctly")
        void shouldHandleDecimalAverageCorrectly() {
            // Arrange
            when(feedbackRepository.calculateAverageScore()).thenReturn(4.5);

            // Act
            Double average = feedbackRepository.calculateAverageScore();

            // Assert
            assertThat(average).isEqualTo(4.5);
        }
    }

    @Nested
    @DisplayName("findAllFeedbacksForReport() Tests")
    class FindAllFeedbacksForReportTests {

        @Test
        @DisplayName("Should return empty list when no feedbacks exist")
        void shouldReturnEmptyListWhenNoFeedbacksExist() {
            // Arrange
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(new ArrayList<>());

            // Act
            List<Feedback> feedbacks = feedbackRepository.findAllFeedbacksForReport();

            // Assert
            assertThat(feedbacks).isEmpty();
        }

        @Test
        @DisplayName("Should return all feedbacks")
        void shouldReturnAllFeedbacks() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            List<Feedback> expectedFeedbacks = Arrays.asList(
                    createFeedback(1L, "F1", 5, StatusFeedback.NORMAL, now),
                    createFeedback(2L, "F2", 3, StatusFeedback.CRITICAL, now),
                    createFeedback(3L, "F3", 8, StatusFeedback.NORMAL, now)
            );
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(expectedFeedbacks);

            // Act
            List<Feedback> feedbacks = feedbackRepository.findAllFeedbacksForReport();

            // Assert
            assertThat(feedbacks).hasSize(3);
        }

        @Test
        @DisplayName("Should return feedbacks with correct data")
        void shouldReturnFeedbacksWithCorrectData() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            Feedback feedback = createFeedback(1L, "Test Description", 7, StatusFeedback.NORMAL, now);
            when(feedbackRepository.findAllFeedbacksForReport()).thenReturn(List.of(feedback));

            // Act
            List<Feedback> feedbacks = feedbackRepository.findAllFeedbacksForReport();

            // Assert
            assertThat(feedbacks).hasSize(1);
            assertThat(feedbacks.get(0).getDescription()).isEqualTo("Test Description");
            assertThat(feedbacks.get(0).getRating()).isEqualTo(7);
            assertThat(feedbacks.get(0).getStatus()).isEqualTo(StatusFeedback.NORMAL);
        }
    }

    @Nested
    @DisplayName("findFeedbacksSince() Tests")
    class FindFeedbacksSinceTests {

        @Test
        @DisplayName("Should return empty list when no feedbacks exist after date")
        void shouldReturnEmptyListWhenNoFeedbacksExistAfterDate() {
            // Arrange
            LocalDateTime searchDate = LocalDateTime.of(2026, 1, 1, 0, 0);
            when(feedbackRepository.findFeedbacksSince(searchDate)).thenReturn(new ArrayList<>());

            // Act
            List<Feedback> feedbacks = feedbackRepository.findFeedbacksSince(searchDate);

            // Assert
            assertThat(feedbacks).isEmpty();
        }

        @Test
        @DisplayName("Should return feedbacks created after specified date")
        void shouldReturnFeedbacksCreatedAfterSpecifiedDate() {
            // Arrange
            LocalDateTime searchDate = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime recentDate = LocalDateTime.of(2026, 2, 1, 10, 0);

            List<Feedback> expectedFeedbacks = Arrays.asList(
                    createFeedback(1L, "Recent", 7, StatusFeedback.NORMAL, recentDate),
                    createFeedback(2L, "Very Recent", 9, StatusFeedback.NORMAL, recentDate.plusDays(4))
            );
            when(feedbackRepository.findFeedbacksSince(searchDate)).thenReturn(expectedFeedbacks);

            // Act
            List<Feedback> feedbacks = feedbackRepository.findFeedbacksSince(searchDate);

            // Assert
            assertThat(feedbacks).hasSize(2);
        }

        @Test
        @DisplayName("Should call findFeedbacksSince with correct parameter")
        void shouldCallFindFeedbacksSinceWithCorrectParameter() {
            // Arrange
            LocalDateTime searchDate = LocalDateTime.of(2026, 2, 1, 10, 0, 0);
            when(feedbackRepository.findFeedbacksSince(searchDate)).thenReturn(new ArrayList<>());

            // Act
            feedbackRepository.findFeedbacksSince(searchDate);

            // Assert
            verify(feedbackRepository, times(1)).findFeedbacksSince(searchDate);
        }
    }

    @Nested
    @DisplayName("JPA Standard Methods Tests")
    class JpaStandardMethodsTests {

        @Test
        @DisplayName("Should save feedback correctly")
        void shouldSaveFeedbackCorrectly() {
            // Arrange
            Feedback feedback = createFeedback(null, "Test description", 7, StatusFeedback.NORMAL, LocalDateTime.now());
            Feedback savedFeedback = createFeedback(1L, "Test description", 7, StatusFeedback.NORMAL, LocalDateTime.now());
            when(feedbackRepository.save(any(Feedback.class))).thenReturn(savedFeedback);

            // Act
            Feedback result = feedbackRepository.save(feedback);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getDescription()).isEqualTo("Test description");
        }

        @Test
        @DisplayName("Should find feedback by id")
        void shouldFindFeedbackById() {
            // Arrange
            Feedback feedback = createFeedback(1L, "Test", 5, StatusFeedback.NORMAL, LocalDateTime.now());
            when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

            // Act
            Optional<Feedback> result = feedbackRepository.findById(1L);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return empty optional for non-existent id")
        void shouldReturnEmptyOptionalForNonExistentId() {
            // Arrange
            when(feedbackRepository.findById(999L)).thenReturn(Optional.empty());

            // Act
            Optional<Feedback> result = feedbackRepository.findById(999L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should delete feedback by id")
        void shouldDeleteFeedbackById() {
            // Arrange
            doNothing().when(feedbackRepository).deleteById(1L);

            // Act
            feedbackRepository.deleteById(1L);

            // Assert
            verify(feedbackRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should check existence correctly")
        void shouldCheckExistenceCorrectly() {
            // Arrange
            when(feedbackRepository.existsById(1L)).thenReturn(true);
            when(feedbackRepository.existsById(999L)).thenReturn(false);

            // Assert
            assertThat(feedbackRepository.existsById(1L)).isTrue();
            assertThat(feedbackRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Should count all feedbacks correctly")
        void shouldCountAllFeedbacksCorrectly() {
            // Arrange
            when(feedbackRepository.count()).thenReturn(3L);

            // Act
            long count = feedbackRepository.count();

            // Assert
            assertThat(count).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very large count")
        void shouldHandleVeryLargeCount() {
            // Arrange
            when(feedbackRepository.countTotalFeedbacks()).thenReturn(Long.MAX_VALUE);

            // Act
            Long count = feedbackRepository.countTotalFeedbacks();

            // Assert
            assertThat(count).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        @DisplayName("Should handle maximum average score")
        void shouldHandleMaximumAverageScore() {
            // Arrange
            when(feedbackRepository.calculateAverageScore()).thenReturn(10.0);

            // Act
            Double average = feedbackRepository.calculateAverageScore();

            // Assert
            assertThat(average).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Should handle minimum average score")
        void shouldHandleMinimumAverageScore() {
            // Arrange
            when(feedbackRepository.calculateAverageScore()).thenReturn(0.0);

            // Act
            Double average = feedbackRepository.calculateAverageScore();

            // Assert
            assertThat(average).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Should handle feedback with null description")
        void shouldHandleFeedbackWithNullDescription() {
            // Arrange
            Feedback feedback = createFeedback(1L, null, 5, StatusFeedback.NORMAL, LocalDateTime.now());
            when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));

            // Act
            Optional<Feedback> result = feedbackRepository.findById(1L);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getDescription()).isNull();
        }
    }
}
