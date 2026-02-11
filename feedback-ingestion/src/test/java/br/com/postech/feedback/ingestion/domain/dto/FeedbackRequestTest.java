package br.com.postech.feedback.ingestion.domain.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeedbackRequest Tests")
class FeedbackRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Record Creation Tests")
    class RecordCreationTests {

        @Test
        @DisplayName("Should create FeedbackRequest with valid data")
        void shouldCreateFeedbackRequestWithValidData() {
            // Act
            FeedbackRequest request = new FeedbackRequest("Great product", 8);

            // Assert
            assertEquals("Great product", request.description());
            assertEquals(8, request.rating());
        }

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            // Arrange
            FeedbackRequest request1 = new FeedbackRequest("Test", 5);
            FeedbackRequest request2 = new FeedbackRequest("Test", 5);

            // Assert
            assertEquals(request1, request2);
            assertEquals(request1.hashCode(), request2.hashCode());
        }
    }

    @Nested
    @DisplayName("Validation Tests - Description")
    class DescriptionValidationTests {

        @Test
        @DisplayName("Should fail validation when description is null")
        void shouldFailValidationWhenDescriptionIsNull() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest(null, 5);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Description")));
        }

        @Test
        @DisplayName("Should fail validation when description is empty")
        void shouldFailValidationWhenDescriptionIsEmpty() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("", 5);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when description is blank")
        void shouldFailValidationWhenDescriptionIsBlank() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("   ", 5);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation with valid description")
        void shouldPassValidationWithValidDescription() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Valid description", 5);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }
    }

    @Nested
    @DisplayName("Validation Tests - Rating")
    class RatingValidationTests {

        @Test
        @DisplayName("Should fail validation when rating is null")
        void shouldFailValidationWhenRatingIsNull() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test", null);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Rating")));
        }

        @Test
        @DisplayName("Should fail validation when rating is below 0")
        void shouldFailValidationWhenRatingIsBelow0() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test", -1);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should fail validation when rating is above 10")
        void shouldFailValidationWhenRatingIsAbove10() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test", 11);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation with rating 0")
        void shouldPassValidationWithRating0() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test", 0);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation with rating 10")
        void shouldPassValidationWithRating10() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test", 10);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("Should pass validation with rating 5")
        void shouldPassValidationWithRating5() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test", 5);

            // Act
            Set<ConstraintViolation<FeedbackRequest>> violations = validator.validate(request);

            // Assert
            assertTrue(violations.isEmpty());
        }
    }
}
