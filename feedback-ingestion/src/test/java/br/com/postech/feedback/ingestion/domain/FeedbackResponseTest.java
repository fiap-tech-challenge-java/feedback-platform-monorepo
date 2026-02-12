package br.com.postech.feedback.ingestion.domain;

import br.com.postech.feedback.core.domain.StatusFeedback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeedbackResponse Tests")
class FeedbackResponseTest {

    @Nested
    @DisplayName("Record Creation Tests")
    class RecordCreationTests {

        @Test
        @DisplayName("Should create FeedbackResponse with all fields")
        void shouldCreateFeedbackResponseWithAllFields() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);

            // Act
            FeedbackResponse response = new FeedbackResponse(
                    1L,
                    "Great product",
                    8,
                    StatusFeedback.NORMAL,
                    now,
                    now
            );

            // Assert
            assertEquals(1L, response.id());
            assertEquals("Great product", response.description());
            assertEquals(8, response.rating());
            assertEquals(StatusFeedback.NORMAL, response.status());
            assertEquals(now, response.createdAt());
            assertEquals(now, response.updatedAt());
        }

        @Test
        @DisplayName("Should allow null values")
        void shouldAllowNullValues() {
            // Act
            FeedbackResponse response = new FeedbackResponse(null, null, null, null, null, null);

            // Assert
            assertNull(response.id());
            assertNull(response.description());
            assertNull(response.rating());
            assertNull(response.status());
            assertNull(response.createdAt());
            assertNull(response.updatedAt());
        }

        @Test
        @DisplayName("Should create response with CRITICAL status")
        void shouldCreateResponseWithCriticalStatus() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            FeedbackResponse response = new FeedbackResponse(
                    2L,
                    "Bad product",
                    2,
                    StatusFeedback.CRITICAL,
                    now,
                    now
            );

            // Assert
            assertEquals(StatusFeedback.CRITICAL, response.status());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            FeedbackResponse response1 = new FeedbackResponse(1L, "Test", 5, StatusFeedback.NORMAL, now, now);
            FeedbackResponse response2 = new FeedbackResponse(1L, "Test", 5, StatusFeedback.NORMAL, now, now);

            // Assert
            assertEquals(response1, response2);
            assertEquals(response1.hashCode(), response2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different ids")
        void shouldNotBeEqualForDifferentIds() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            FeedbackResponse response1 = new FeedbackResponse(1L, "Test", 5, StatusFeedback.NORMAL, now, now);
            FeedbackResponse response2 = new FeedbackResponse(2L, "Test", 5, StatusFeedback.NORMAL, now, now);

            // Assert
            assertNotEquals(response1, response2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            FeedbackResponse response = new FeedbackResponse(1L, "Test", 5, StatusFeedback.NORMAL, now, now);

            // Act
            String toString = response.toString();

            // Assert
            assertTrue(toString.contains("FeedbackResponse"));
            assertTrue(toString.contains("1"));
            assertTrue(toString.contains("Test"));
            assertTrue(toString.contains("5"));
            assertTrue(toString.contains("NORMAL"));
        }
    }
}
