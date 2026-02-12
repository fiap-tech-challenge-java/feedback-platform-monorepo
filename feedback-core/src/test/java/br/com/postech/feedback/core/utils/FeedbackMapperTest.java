package br.com.postech.feedback.core.utils;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeedbackMapper Tests")
class FeedbackMapperTest {

    @Nested
    @DisplayName("toEvent() Method Tests")
    class ToEventMethodTests {

        @Test
        @DisplayName("Should map Feedback to FeedbackEventDTO correctly")
        void shouldMapFeedbackToFeedbackEventDTOCorrectly() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            Feedback feedback = new Feedback();
            feedback.setId(1L);
            feedback.setDescription("Great product");
            feedback.setRating(8);
            feedback.setStatus(StatusFeedback.NORMAL);
            feedback.setCreatedAt(now);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(1L, dto.id());
            assertEquals("Great product", dto.description());
            assertEquals(8, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
            assertEquals(now, dto.createdAt());
        }

        @Test
        @DisplayName("Should return null when feedback is null")
        void shouldReturnNullWhenFeedbackIsNull() {
            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(null);

            // Assert
            assertNull(dto);
        }

        @Test
        @DisplayName("Should map CRITICAL status correctly")
        void shouldMapCriticalStatusCorrectly() {
            // Arrange
            Feedback feedback = new Feedback();
            feedback.setId(2L);
            feedback.setDescription("Bad experience");
            feedback.setRating(2);
            feedback.setStatus(StatusFeedback.CRITICAL);
            feedback.setCreatedAt(LocalDateTime.now());

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(StatusFeedback.CRITICAL, dto.status());
        }

        @Test
        @DisplayName("Should handle feedback with null id")
        void shouldHandleFeedbackWithNullId() {
            // Arrange
            Feedback feedback = new Feedback();
            feedback.setDescription("Test");
            feedback.setRating(5);
            feedback.setStatus(StatusFeedback.NORMAL);
            feedback.setCreatedAt(LocalDateTime.now());

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertNull(dto.id());
        }

        @Test
        @DisplayName("Should handle feedback with null description")
        void shouldHandleFeedbackWithNullDescription() {
            // Arrange
            Feedback feedback = new Feedback();
            feedback.setId(1L);
            feedback.setDescription(null);
            feedback.setRating(5);
            feedback.setStatus(StatusFeedback.NORMAL);
            feedback.setCreatedAt(LocalDateTime.now());

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertNull(dto.description());
        }

        @Test
        @DisplayName("Should handle feedback with null createdAt")
        void shouldHandleFeedbackWithNullCreatedAt() {
            // Arrange
            Feedback feedback = new Feedback();
            feedback.setId(1L);
            feedback.setDescription("Test");
            feedback.setRating(5);
            feedback.setStatus(StatusFeedback.NORMAL);
            feedback.setCreatedAt(null);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertNull(dto.createdAt());
        }

        @Test
        @DisplayName("Should map feedback created with two-arg constructor")
        void shouldMapFeedbackCreatedWithTwoArgConstructor() {
            // Arrange
            Feedback feedback = new Feedback("Excellent service", 9);
            feedback.setId(100L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(100L, dto.id());
            assertEquals("Excellent service", dto.description());
            assertEquals(9, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
            assertNotNull(dto.createdAt());
        }

        @Test
        @DisplayName("Should map feedback with minimum rating")
        void shouldMapFeedbackWithMinimumRating() {
            // Arrange
            Feedback feedback = new Feedback("Terrible", 0);
            feedback.setId(1L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(0, dto.rating());
            assertEquals(StatusFeedback.CRITICAL, dto.status());
        }

        @Test
        @DisplayName("Should map feedback with maximum rating")
        void shouldMapFeedbackWithMaximumRating() {
            // Arrange
            Feedback feedback = new Feedback("Perfect", 10);
            feedback.setId(1L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(10, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should be able to call toEvent statically")
        void shouldBeAbleToCallToEventStatically() {
            // Arrange
            Feedback feedback = new Feedback("Test", 5);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Special Values Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should map feedback with empty description")
        void shouldMapFeedbackWithEmptyDescription() {
            // Arrange
            Feedback feedback = new Feedback("", 5);
            feedback.setId(1L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals("", dto.description());
        }

        @Test
        @DisplayName("Should map feedback with very long description")
        void shouldMapFeedbackWithVeryLongDescription() {
            // Arrange
            String longDescription = "a".repeat(5000);
            Feedback feedback = new Feedback(longDescription, 5);
            feedback.setId(1L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(longDescription, dto.description());
            assertEquals(5000, dto.description().length());
        }

        @Test
        @DisplayName("Should map feedback with special characters in description")
        void shouldMapFeedbackWithSpecialCharacters() {
            // Arrange
            String specialDescription = "Test @#$%^&*()_+-=[]{}|;':\",./<>?~`";
            Feedback feedback = new Feedback(specialDescription, 5);
            feedback.setId(1L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(specialDescription, dto.description());
        }

        @Test
        @DisplayName("Should map feedback with unicode characters")
        void shouldMapFeedbackWithUnicodeCharacters() {
            // Arrange
            String unicodeDescription = "Feedback com émojis 😀😃😄 e acentos àáâãäå";
            Feedback feedback = new Feedback(unicodeDescription, 5);
            feedback.setId(1L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(unicodeDescription, dto.description());
        }

        @Test
        @DisplayName("Should map feedback with line breaks")
        void shouldMapFeedbackWithLineBreaks() {
            // Arrange
            String multilineDescription = "Line 1\nLine 2\nLine 3";
            Feedback feedback = new Feedback(multilineDescription, 5);
            feedback.setId(1L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(multilineDescription, dto.description());
        }

        @Test
        @DisplayName("Should map feedback with very large ID")
        void shouldMapFeedbackWithVeryLargeId() {
            // Arrange
            Feedback feedback = new Feedback("Test", 5);
            feedback.setId(Long.MAX_VALUE);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(Long.MAX_VALUE, dto.id());
        }

        @Test
        @DisplayName("Should map feedback with ID zero")
        void shouldMapFeedbackWithIdZero() {
            // Arrange
            Feedback feedback = new Feedback("Test", 5);
            feedback.setId(0L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(0L, dto.id());
        }

        @Test
        @DisplayName("Should map feedback with negative ID")
        void shouldMapFeedbackWithNegativeId() {
            // Arrange
            Feedback feedback = new Feedback("Test", 5);
            feedback.setId(-1L);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(-1L, dto.id());
        }
    }

    @Nested
    @DisplayName("All Ratings Boundary Tests")
    class AllRatingsBoundaryTests {

        @Test
        @DisplayName("Should map rating 1 correctly")
        void shouldMapRating1Correctly() {
            Feedback feedback = new Feedback("Bad", 1);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(1, dto.rating());
            assertEquals(StatusFeedback.CRITICAL, dto.status());
        }

        @Test
        @DisplayName("Should map rating 2 correctly")
        void shouldMapRating2Correctly() {
            Feedback feedback = new Feedback("Poor", 2);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(2, dto.rating());
            assertEquals(StatusFeedback.CRITICAL, dto.status());
        }

        @Test
        @DisplayName("Should map rating 3 correctly")
        void shouldMapRating3Correctly() {
            Feedback feedback = new Feedback("Below Average", 3);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(3, dto.rating());
            assertEquals(StatusFeedback.CRITICAL, dto.status());
        }

        @Test
        @DisplayName("Should map rating 4 correctly")
        void shouldMapRating4Correctly() {
            Feedback feedback = new Feedback("Fair", 4);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(4, dto.rating());
            assertEquals(StatusFeedback.CRITICAL, dto.status());
        }

        @Test
        @DisplayName("Should map rating 5 correctly - boundary")
        void shouldMapRating5Correctly() {
            Feedback feedback = new Feedback("Average", 5);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(5, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
        }

        @Test
        @DisplayName("Should map rating 6 correctly")
        void shouldMapRating6Correctly() {
            Feedback feedback = new Feedback("Good", 6);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(6, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
        }

        @Test
        @DisplayName("Should map rating 7 correctly")
        void shouldMapRating7Correctly() {
            Feedback feedback = new Feedback("Very Good", 7);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(7, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
        }

        @Test
        @DisplayName("Should map rating 8 correctly")
        void shouldMapRating8Correctly() {
            Feedback feedback = new Feedback("Great", 8);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(8, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
        }

        @Test
        @DisplayName("Should map rating 9 correctly")
        void shouldMapRating9Correctly() {
            Feedback feedback = new Feedback("Excellent", 9);
            feedback.setId(1L);
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            assertEquals(9, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
        }
    }

    @Nested
    @DisplayName("Timestamp Mapping Tests")
    class TimestampMappingTests {

        @Test
        @DisplayName("Should map past timestamp correctly")
        void shouldMapPastTimestampCorrectly() {
            // Arrange
            LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0);
            Feedback feedback = new Feedback("Old feedback", 5);
            feedback.setId(1L);
            feedback.setCreatedAt(pastDate);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(pastDate, dto.createdAt());
        }

        @Test
        @DisplayName("Should map future timestamp correctly")
        void shouldMapFutureTimestampCorrectly() {
            // Arrange
            LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59);
            Feedback feedback = new Feedback("Future feedback", 5);
            feedback.setId(1L);
            feedback.setCreatedAt(futureDate);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(futureDate, dto.createdAt());
        }

        @Test
        @DisplayName("Should preserve timestamp precision")
        void shouldPreserveTimestampPrecision() {
            // Arrange
            LocalDateTime preciseTime = LocalDateTime.of(2026, 2, 10, 15, 30, 45, 123456789);
            Feedback feedback = new Feedback("Precise timestamp", 5);
            feedback.setId(1L);
            feedback.setCreatedAt(preciseTime);

            // Act
            FeedbackEventDTO dto = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto);
            assertEquals(preciseTime, dto.createdAt());
            assertEquals(123456789, dto.createdAt().getNano());
        }
    }

    @Nested
    @DisplayName("Multiple Conversions Tests")
    class MultipleConversionsTests {

        @Test
        @DisplayName("Should handle multiple consecutive conversions")
        void shouldHandleMultipleConsecutiveConversions() {
            // Arrange
            Feedback feedback1 = new Feedback("First", 1);
            feedback1.setId(1L);
            Feedback feedback2 = new Feedback("Second", 5);
            feedback2.setId(2L);
            Feedback feedback3 = new Feedback("Third", 10);
            feedback3.setId(3L);

            // Act
            FeedbackEventDTO dto1 = FeedbackMapper.toEvent(feedback1);
            FeedbackEventDTO dto2 = FeedbackMapper.toEvent(feedback2);
            FeedbackEventDTO dto3 = FeedbackMapper.toEvent(feedback3);

            // Assert
            assertEquals(1L, dto1.id());
            assertEquals(2L, dto2.id());
            assertEquals(3L, dto3.id());
            assertEquals(StatusFeedback.CRITICAL, dto1.status());
            assertEquals(StatusFeedback.NORMAL, dto2.status());
            assertEquals(StatusFeedback.NORMAL, dto3.status());
        }

        @Test
        @DisplayName("Should maintain thread safety for concurrent conversions")
        void shouldMaintainThreadSafetyForConcurrentConversions() {
            // Arrange
            Feedback feedback = new Feedback("Concurrent test", 5);
            feedback.setId(1L);

            // Act - Multiple conversions should work without issues
            FeedbackEventDTO dto1 = FeedbackMapper.toEvent(feedback);
            FeedbackEventDTO dto2 = FeedbackMapper.toEvent(feedback);

            // Assert
            assertNotNull(dto1);
            assertNotNull(dto2);
            assertEquals(dto1.id(), dto2.id());
            assertEquals(dto1.description(), dto2.description());
        }
    }
}
