package br.com.postech.feedback.ingestion.domain.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateFeedback Tests")
class CreateFeedbackTest {

    @Nested
    @DisplayName("Record Creation Tests")
    class RecordCreationTests {

        @Test
        @DisplayName("Should create CreateFeedback with valid data")
        void shouldCreateCreateFeedbackWithValidData() {
            // Act
            CreateFeedback createFeedback = new CreateFeedback("Great product", 8);

            // Assert
            assertEquals("Great product", createFeedback.description());
            assertEquals(8, createFeedback.rating());
        }

        @Test
        @DisplayName("Should allow null values")
        void shouldAllowNullValues() {
            // Act
            CreateFeedback createFeedback = new CreateFeedback(null, null);

            // Assert
            assertNull(createFeedback.description());
            assertNull(createFeedback.rating());
        }

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            // Arrange
            CreateFeedback createFeedback1 = new CreateFeedback("Test", 5);
            CreateFeedback createFeedback2 = new CreateFeedback("Test", 5);

            // Assert
            assertEquals(createFeedback1, createFeedback2);
            assertEquals(createFeedback1.hashCode(), createFeedback2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            // Arrange
            CreateFeedback createFeedback1 = new CreateFeedback("Test1", 5);
            CreateFeedback createFeedback2 = new CreateFeedback("Test2", 5);

            // Assert
            assertNotEquals(createFeedback1, createFeedback2);
        }
    }

    @Nested
    @DisplayName("Rating Boundary Tests")
    class RatingBoundaryTests {

        @Test
        @DisplayName("Should accept rating 0")
        void shouldAcceptRating0() {
            // Act
            CreateFeedback createFeedback = new CreateFeedback("Terrible", 0);

            // Assert
            assertEquals(0, createFeedback.rating());
        }

        @Test
        @DisplayName("Should accept rating 10")
        void shouldAcceptRating10() {
            // Act
            CreateFeedback createFeedback = new CreateFeedback("Perfect", 10);

            // Assert
            assertEquals(10, createFeedback.rating());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Test description", 7);

            // Act
            String toString = createFeedback.toString();

            // Assert
            assertTrue(toString.contains("CreateFeedback"));
            assertTrue(toString.contains("Test description"));
            assertTrue(toString.contains("7"));
        }
    }
}
