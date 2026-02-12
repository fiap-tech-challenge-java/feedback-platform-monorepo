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

    @Nested
    @DisplayName("JPA Annotations Validation Tests")
    class JpaAnnotationsValidationTests {

        @Test
        @DisplayName("Entity annotation should be present")
        void entityAnnotationShouldBePresent() {
            assertTrue(Feedback.class.isAnnotationPresent(jakarta.persistence.Entity.class));
        }

        @Test
        @DisplayName("Table annotation should have correct name")
        void tableAnnotationShouldHaveCorrectName() {
            jakarta.persistence.Table tableAnnotation = Feedback.class.getAnnotation(jakarta.persistence.Table.class);
            assertNotNull(tableAnnotation);
            assertEquals("feedbacks", tableAnnotation.name());
        }

        @Test
        @DisplayName("Id field should have Id annotation")
        void idFieldShouldHaveIdAnnotation() throws NoSuchFieldException {
            java.lang.reflect.Field idField = Feedback.class.getDeclaredField("id");
            assertTrue(idField.isAnnotationPresent(jakarta.persistence.Id.class));
        }

        @Test
        @DisplayName("Id field should have GeneratedValue annotation")
        void idFieldShouldHaveGeneratedValueAnnotation() throws NoSuchFieldException {
            java.lang.reflect.Field idField = Feedback.class.getDeclaredField("id");
            assertTrue(idField.isAnnotationPresent(jakarta.persistence.GeneratedValue.class));

            jakarta.persistence.GeneratedValue generatedValue = idField.getAnnotation(jakarta.persistence.GeneratedValue.class);
            assertEquals(jakarta.persistence.GenerationType.IDENTITY, generatedValue.strategy());
        }

        @Test
        @DisplayName("Rating field should have NotNull annotation")
        void ratingFieldShouldHaveNotNullAnnotation() throws NoSuchFieldException {
            java.lang.reflect.Field ratingField = Feedback.class.getDeclaredField("rating");
            assertTrue(ratingField.isAnnotationPresent(jakarta.validation.constraints.NotNull.class));
        }

        @Test
        @DisplayName("Rating field should have Min annotation with value 0")
        void ratingFieldShouldHaveMinAnnotationWithValue0() throws NoSuchFieldException {
            java.lang.reflect.Field ratingField = Feedback.class.getDeclaredField("rating");
            assertTrue(ratingField.isAnnotationPresent(jakarta.validation.constraints.Min.class));

            jakarta.validation.constraints.Min minAnnotation = ratingField.getAnnotation(jakarta.validation.constraints.Min.class);
            assertEquals(0, minAnnotation.value());
        }

        @Test
        @DisplayName("Rating field should have Max annotation with value 10")
        void ratingFieldShouldHaveMaxAnnotationWithValue10() throws NoSuchFieldException {
            java.lang.reflect.Field ratingField = Feedback.class.getDeclaredField("rating");
            assertTrue(ratingField.isAnnotationPresent(jakarta.validation.constraints.Max.class));

            jakarta.validation.constraints.Max maxAnnotation = ratingField.getAnnotation(jakarta.validation.constraints.Max.class);
            assertEquals(10, maxAnnotation.value());
        }

        @Test
        @DisplayName("Status field should have Enumerated annotation")
        void statusFieldShouldHaveEnumeratedAnnotation() throws NoSuchFieldException {
            java.lang.reflect.Field statusField = Feedback.class.getDeclaredField("status");
            assertTrue(statusField.isAnnotationPresent(jakarta.persistence.Enumerated.class));

            jakarta.persistence.Enumerated enumerated = statusField.getAnnotation(jakarta.persistence.Enumerated.class);
            assertEquals(jakarta.persistence.EnumType.STRING, enumerated.value());
        }

        @Test
        @DisplayName("CreatedAt field should have Column annotation with correct name")
        void createdAtFieldShouldHaveColumnAnnotationWithCorrectName() throws NoSuchFieldException {
            java.lang.reflect.Field createdAtField = Feedback.class.getDeclaredField("createdAt");
            assertTrue(createdAtField.isAnnotationPresent(jakarta.persistence.Column.class));

            jakarta.persistence.Column column = createdAtField.getAnnotation(jakarta.persistence.Column.class);
            assertEquals("created_at", column.name());
        }

        @Test
        @DisplayName("UpdatedAt field should have Column annotation with correct name")
        void updatedAtFieldShouldHaveColumnAnnotationWithCorrectName() throws NoSuchFieldException {
            java.lang.reflect.Field updatedAtField = Feedback.class.getDeclaredField("updatedAt");
            assertTrue(updatedAtField.isAnnotationPresent(jakarta.persistence.Column.class));

            jakarta.persistence.Column column = updatedAtField.getAnnotation(jakarta.persistence.Column.class);
            assertEquals("updated_at", column.name());
        }
    }

    @Nested
    @DisplayName("Null Safety Tests")
    class NullSafetyTests {

        @Test
        @DisplayName("Should handle null description in setter")
        void shouldHandleNullDescriptionInSetter() {
            Feedback feedback = new Feedback();
            feedback.setDescription(null);
            assertNull(feedback.getDescription());
        }

        @Test
        @DisplayName("Should handle null status in setter")
        void shouldHandleNullStatusInSetter() {
            Feedback feedback = new Feedback();
            feedback.setStatus(null);
            assertNull(feedback.getStatus());
        }

        @Test
        @DisplayName("Should handle null createdAt in setter")
        void shouldHandleNullCreatedAtInSetter() {
            Feedback feedback = new Feedback();
            feedback.setCreatedAt(null);
            assertNull(feedback.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle null updatedAt in setter")
        void shouldHandleNullUpdatedAtInSetter() {
            Feedback feedback = new Feedback();
            feedback.setUpdatedAt(null);
            assertNull(feedback.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle null rating in setter")
        void shouldHandleNullRatingInSetter() {
            Feedback feedback = new Feedback();
            feedback.setRating(null);
            assertNull(feedback.getRating());
        }
    }

    @Nested
    @DisplayName("Constructor Exception Message Tests")
    class ConstructorExceptionMessageTests {

        @Test
        @DisplayName("Should have correct exception message for rating -1")
        void shouldHaveCorrectExceptionMessageForRatingMinus1() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Feedback("Test", -1)
            );
            assertEquals("Rating deve estar entre 0 e 10", exception.getMessage());
        }

        @Test
        @DisplayName("Should have correct exception message for rating 11")
        void shouldHaveCorrectExceptionMessageForRating11() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Feedback("Test", 11)
            );
            assertEquals("Rating deve estar entre 0 e 10", exception.getMessage());
        }

        @Test
        @DisplayName("Should have correct exception message for rating 100")
        void shouldHaveCorrectExceptionMessageForRating100() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Feedback("Test", 100)
            );
            assertEquals("Rating deve estar entre 0 e 10", exception.getMessage());
        }

        @Test
        @DisplayName("Should have correct exception message for rating -100")
        void shouldHaveCorrectExceptionMessageForRatingMinus100() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Feedback("Test", -100)
            );
            assertEquals("Rating deve estar entre 0 e 10", exception.getMessage());
        }

        @Test
        @DisplayName("Should have correct exception message for null rating")
        void shouldHaveCorrectExceptionMessageForNullRating() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Feedback("Test", null)
            );
            assertEquals("Rating deve estar entre 0 e 10", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Data Annotation Tests")
    class DataAnnotationTests {

        @Test
        @DisplayName("Feedback should have getter methods from @Data")
        void feedbackShouldHaveGetterMethodsFromData() {
            Feedback feedback = new Feedback("Test", 5);
            feedback.setId(1L);

            // Test all getters work correctly
            assertNotNull(feedback.getId());
            assertNotNull(feedback.getDescription());
            assertNotNull(feedback.getRating());
            assertNotNull(feedback.getStatus());
            assertNotNull(feedback.getCreatedAt());
            assertNotNull(feedback.getUpdatedAt());
        }

        @Test
        @DisplayName("Feedback should have setter methods from @Data")
        void feedbackShouldHaveSetterMethodsFromData() {
            Feedback feedback = new Feedback();
            LocalDateTime now = LocalDateTime.now();

            // Test all setters work correctly
            feedback.setId(1L);
            feedback.setDescription("Test");
            feedback.setRating(5);
            feedback.setStatus(StatusFeedback.NORMAL);
            feedback.setCreatedAt(now);
            feedback.setUpdatedAt(now);

            assertEquals(1L, feedback.getId());
            assertEquals("Test", feedback.getDescription());
            assertEquals(5, feedback.getRating());
            assertEquals(StatusFeedback.NORMAL, feedback.getStatus());
            assertEquals(now, feedback.getCreatedAt());
            assertEquals(now, feedback.getUpdatedAt());
        }
    }
}
