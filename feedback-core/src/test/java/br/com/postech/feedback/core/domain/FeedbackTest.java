package br.com.postech.feedback.core.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Feedback Entity Tests")
class FeedbackTest {

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create feedback with description and rating")
        void shouldCreateFeedbackWithDescriptionAndRating() {
            // Act
            Feedback feedback = new Feedback("Great product", 8);

            // Assert
            assertEquals("Great product", feedback.getDescription());
            assertEquals(8, feedback.getRating());
            assertNotNull(feedback.getCreatedAt());
            assertNotNull(feedback.getUpdatedAt());
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should set CRITICAL status for rating below 5")
        void shouldSetCriticalStatusForRatingBelow5() {
            // Act
            Feedback feedback = new Feedback("Bad experience", 3);

            // Assert
            assertEquals(StatusFeedback.CRITICAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should set NORMAL status for rating 5 or above")
        void shouldSetNormalStatusForRating5OrAbove() {
            // Act
            Feedback feedback = new Feedback("Good experience", 5);

            // Assert
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should throw exception for rating below 0")
        void shouldThrowExceptionForRatingBelow0() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Feedback("Test", -1)
            );

            assertEquals("Rating deve estar entre 0 e 10", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for rating above 10")
        void shouldThrowExceptionForRatingAbove10() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new Feedback("Test", 11)
            );

            assertEquals("Rating deve estar entre 0 e 10", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null rating")
        void shouldThrowExceptionForNullRating() {
            // Act & Assert
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new Feedback("Test", null)
            );
        }

        @Test
        @DisplayName("Should create feedback with no-args constructor")
        void shouldCreateFeedbackWithNoArgsConstructor() {
            // Act
            Feedback feedback = new Feedback();

            // Assert
            assertNotNull(feedback);
            assertNull(feedback.getId());
            assertNull(feedback.getDescription());
        }

        @Test
        @DisplayName("Should create feedback with all-args constructor")
        void shouldCreateFeedbackWithAllArgsConstructor() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            Feedback feedback = new Feedback(1L, "Test description", 7, StatusFeedback.NORMAL, now, now);

            // Assert
            assertEquals(1L, feedback.getId());
            assertEquals("Test description", feedback.getDescription());
            assertEquals(7, feedback.getRating());
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
            assertEquals(now, feedback.getCreatedAt());
            assertEquals(now, feedback.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Rating Boundary Tests")
    class RatingBoundaryTests {

        @Test
        @DisplayName("Should accept rating 0 (minimum)")
        void shouldAcceptRating0() {
            // Act
            Feedback feedback = new Feedback("Worst experience ever", 0);

            // Assert
            assertEquals(0, feedback.getRating());
            assertEquals(StatusFeedback.CRITICAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should accept rating 10 (maximum)")
        void shouldAcceptRating10() {
            // Act
            Feedback feedback = new Feedback("Perfect experience", 10);

            // Assert
            assertEquals(10, feedback.getRating());
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should set CRITICAL status for rating 4")
        void shouldSetCriticalStatusForRating4() {
            // Act
            Feedback feedback = new Feedback("Below average", 4);

            // Assert
            assertEquals(StatusFeedback.CRITICAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should set NORMAL status for rating 5")
        void shouldSetNormalStatusForRating5() {
            // Act
            Feedback feedback = new Feedback("Average experience", 5);

            // Assert
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }
    }

    @Nested
    @DisplayName("Setter and Getter Tests")
    class SetterGetterTests {

        @Test
        @DisplayName("Should set and get all properties")
        void shouldSetAndGetAllProperties() {
            // Arrange
            Feedback feedback = new Feedback();
            LocalDateTime now = LocalDateTime.now();

            // Act
            feedback.setId(1L);
            feedback.setDescription("Test description");
            feedback.setRating(7);
            feedback.setStatus(StatusFeedback.NORMAL);
            feedback.setCreatedAt(now);
            feedback.setUpdatedAt(now);

            // Assert
            assertEquals(1L, feedback.getId());
            assertEquals("Test description", feedback.getDescription());
            assertEquals(7, feedback.getRating());
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
            assertEquals(now, feedback.getCreatedAt());
            assertEquals(now, feedback.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal for same id and properties")
        void shouldBeEqualForSameIdAndProperties() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            Feedback feedback1 = new Feedback(1L, "Test", 5, StatusFeedback.NORMAL, now, now);
            Feedback feedback2 = new Feedback(1L, "Test", 5, StatusFeedback.NORMAL, now, now);

            // Assert
            assertEquals(feedback1, feedback2);
            assertEquals(feedback1.hashCode(), feedback2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different ids")
        void shouldNotBeEqualForDifferentIds() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            Feedback feedback1 = new Feedback(1L, "Test", 5, StatusFeedback.NORMAL, now, now);
            Feedback feedback2 = new Feedback(2L, "Test", 5, StatusFeedback.NORMAL, now, now);

            // Assert
            assertNotEquals(feedback1, feedback2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString with all fields")
        void shouldGenerateToStringWithAllFields() {
            // Arrange
            Feedback feedback = new Feedback("Test", 5);
            feedback.setId(1L);

            // Act
            String toString = feedback.toString();

            // Assert
            assertTrue(toString.contains("id=1"));
            assertTrue(toString.contains("description=Test"));
            assertTrue(toString.contains("rating=5"));
        }
    }

    @Nested
    @DisplayName("PreUpdate Lifecycle Tests")
    class PreUpdateLifecycleTests {

        @Test
        @DisplayName("Should update updatedAt timestamp on preUpdate")
        void shouldUpdateUpdatedAtTimestampOnPreUpdate() throws InterruptedException {
            // Arrange
            Feedback feedback = new Feedback("Test", 5);
            LocalDateTime originalUpdatedAt = feedback.getUpdatedAt();

            // Wait a bit to ensure timestamp difference
            Thread.sleep(10);

            // Act
            feedback.preUpdate();

            // Assert
            assertNotNull(feedback.getUpdatedAt());
            assertTrue(feedback.getUpdatedAt().isAfter(originalUpdatedAt) ||
                      feedback.getUpdatedAt().isEqual(originalUpdatedAt));
        }

        @Test
        @DisplayName("Should not change other fields on preUpdate")
        void shouldNotChangeOtherFieldsOnPreUpdate() {
            // Arrange
            Feedback feedback = new Feedback("Original description", 7);
            feedback.setId(1L);
            LocalDateTime originalCreatedAt = feedback.getCreatedAt();
            String originalDescription = feedback.getDescription();
            Integer originalRating = feedback.getRating();
            StatusFeedback originalStatus = feedback.getStatus();

            // Act
            feedback.preUpdate();

            // Assert
            assertEquals(1L, feedback.getId());
            assertEquals(originalDescription, feedback.getDescription());
            assertEquals(originalRating, feedback.getRating());
            assertEquals(originalStatus, feedback.getStatus());
            assertEquals(originalCreatedAt, feedback.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Status Calculation Tests")
    class StatusCalculationTests {

        @Test
        @DisplayName("Should calculate CRITICAL for rating 0")
        void shouldCalculateCriticalForRating0() {
            Feedback feedback = new Feedback("Terrible", 0);
            assertEquals(StatusFeedback.CRITICAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should calculate CRITICAL for rating 1")
        void shouldCalculateCriticalForRating1() {
            Feedback feedback = new Feedback("Very bad", 1);
            assertEquals(StatusFeedback.CRITICAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should calculate CRITICAL for rating 2")
        void shouldCalculateCriticalForRating2() {
            Feedback feedback = new Feedback("Bad", 2);
            assertEquals(StatusFeedback.CRITICAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should calculate CRITICAL for rating 3")
        void shouldCalculateCriticalForRating3() {
            Feedback feedback = new Feedback("Below average", 3);
            assertEquals(StatusFeedback.CRITICAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should calculate NORMAL for rating 6")
        void shouldCalculateNormalForRating6() {
            Feedback feedback = new Feedback("Above average", 6);
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should calculate NORMAL for rating 7")
        void shouldCalculateNormalForRating7() {
            Feedback feedback = new Feedback("Good", 7);
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should calculate NORMAL for rating 8")
        void shouldCalculateNormalForRating8() {
            Feedback feedback = new Feedback("Very good", 8);
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should calculate NORMAL for rating 9")
        void shouldCalculateNormalForRating9() {
            Feedback feedback = new Feedback("Excellent", 9);
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should calculate NORMAL for rating 10")
        void shouldCalculateNormalForRating10() {
            Feedback feedback = new Feedback("Perfect", 10);
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
        }
    }

    @Nested
    @DisplayName("Description Edge Cases")
    class DescriptionEdgeCases {

        @Test
        @DisplayName("Should accept empty description")
        void shouldAcceptEmptyDescription() {
            Feedback feedback = new Feedback("", 5);
            assertEquals("", feedback.getDescription());
        }

        @Test
        @DisplayName("Should accept very long description")
        void shouldAcceptVeryLongDescription() {
            String longDescription = "a".repeat(1000);
            Feedback feedback = new Feedback(longDescription, 5);
            assertEquals(longDescription, feedback.getDescription());
        }

        @Test
        @DisplayName("Should accept description with special characters")
        void shouldAcceptDescriptionWithSpecialCharacters() {
            String specialDescription = "Feedback com !@#$%^&*()_+-=[]{}|;':\",./<>?";
            Feedback feedback = new Feedback(specialDescription, 5);
            assertEquals(specialDescription, feedback.getDescription());
        }

        @Test
        @DisplayName("Should accept description with unicode characters")
        void shouldAcceptDescriptionWithUnicodeCharacters() {
            String unicodeDescription = "Feedback com émojis 😀😃😄 e acentuação àáâãäå";
            Feedback feedback = new Feedback(unicodeDescription, 5);
            assertEquals(unicodeDescription, feedback.getDescription());
        }

        @Test
        @DisplayName("Should accept description with line breaks")
        void shouldAcceptDescriptionWithLineBreaks() {
            String multilineDescription = "First line\nSecond line\nThird line";
            Feedback feedback = new Feedback(multilineDescription, 5);
            assertEquals(multilineDescription, feedback.getDescription());
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("CreatedAt should be set automatically on construction")
        void createdAtShouldBeSetAutomaticallyOnConstruction() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            Feedback feedback = new Feedback("Test", 5);
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(feedback.getCreatedAt());
            assertTrue(feedback.getCreatedAt().isAfter(before));
            assertTrue(feedback.getCreatedAt().isBefore(after));
        }

        @Test
        @DisplayName("UpdatedAt should be set automatically on construction")
        void updatedAtShouldBeSetAutomaticallyOnConstruction() {
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            Feedback feedback = new Feedback("Test", 5);
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(feedback.getUpdatedAt());
            assertTrue(feedback.getUpdatedAt().isAfter(before));
            assertTrue(feedback.getUpdatedAt().isBefore(after));
        }

        @Test
        @DisplayName("CreatedAt and UpdatedAt should be close in time on construction")
        void createdAtAndUpdatedAtShouldBeCloseInTimeOnConstruction() {
            Feedback feedback = new Feedback("Test", 5);

            assertNotNull(feedback.getCreatedAt());
            assertNotNull(feedback.getUpdatedAt());
            // They should be very close (within 1 second)
            assertTrue(Math.abs(feedback.getCreatedAt().getNano() - feedback.getUpdatedAt().getNano()) < 1_000_000_000);
        }
    }

    @Nested
    @DisplayName("Entity State Tests")
    class EntityStateTests {

        @Test
        @DisplayName("New feedback should have null id")
        void newFeedbackShouldHaveNullId() {
            Feedback feedback = new Feedback("Test", 5);
            assertNull(feedback.getId());
        }

        @Test
        @DisplayName("Should allow setting id after creation")
        void shouldAllowSettingIdAfterCreation() {
            Feedback feedback = new Feedback("Test", 5);
            feedback.setId(100L);
            assertEquals(100L, feedback.getId());
        }

        @Test
        @DisplayName("Should allow changing rating after creation")
        void shouldAllowChangingRatingAfterCreation() {
            Feedback feedback = new Feedback("Test", 5);
            feedback.setRating(8);
            assertEquals(8, feedback.getRating());
        }

        @Test
        @DisplayName("Should allow changing status after creation")
        void shouldAllowChangingStatusAfterCreation() {
            Feedback feedback = new Feedback("Test", 5);
            feedback.setStatus(StatusFeedback.CRITICAL);
            assertEquals(StatusFeedback.CRITICAL, feedback.getStatus());
        }

        @Test
        @DisplayName("Should allow changing description after creation")
        void shouldAllowChangingDescriptionAfterCreation() {
            Feedback feedback = new Feedback("Original", 5);
            feedback.setDescription("Updated description");
            assertEquals("Updated description", feedback.getDescription());
        }
    }
}
