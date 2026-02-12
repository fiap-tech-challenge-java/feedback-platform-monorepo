package br.com.postech.feedback.ingestion.controller;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.ingestion.domain.FeedbackResponse;
import br.com.postech.feedback.ingestion.domain.dto.CreateFeedback;
import br.com.postech.feedback.ingestion.domain.dto.FeedbackRequest;
import br.com.postech.feedback.ingestion.domain.service.FeedbackInjectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackInjectionController Tests")
class FeedbackInjectionControllerTest {

    @Mock
    private FeedbackInjectionService feedbackInjectionService;

    private FeedbackInjectionController controller;

    @BeforeEach
    void setUp() {
        controller = new FeedbackInjectionController(feedbackInjectionService);
    }

    @Nested
    @DisplayName("feedbackInjection() Tests")
    class FeedbackInjectionTests {

        @Test
        @DisplayName("Should return CREATED status on successful feedback submission")
        void shouldReturnCreatedStatusOnSuccessfulFeedbackSubmission() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Great product", 8);
            Feedback feedback = createMockFeedback(1L, "Great product", 8, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        @DisplayName("Should return FeedbackResponse in body")
        void shouldReturnFeedbackResponseInBody() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Good experience", 7);
            Feedback feedback = createMockFeedback(2L, "Good experience", 7, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertNotNull(response.getBody());
            assertEquals(2L, response.getBody().id());
            assertEquals("Good experience", response.getBody().description());
            assertEquals(7, response.getBody().rating());
        }

        @Test
        @DisplayName("Should call service with correct parameters")
        void shouldCallServiceWithCorrectParameters() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test feedback", 5);
            Feedback feedback = createMockFeedback(3L, "Test feedback", 5, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            controller.feedbackInjection(request);

            // Assert
            verify(feedbackInjectionService, times(1)).processFeedback(any(CreateFeedback.class));
        }

        @Test
        @DisplayName("Should propagate exception from service")
        void shouldPropagateExceptionFromService() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Bad request", 5);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class)))
                    .thenThrow(new RuntimeException("Service error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> controller.feedbackInjection(request));
        }

        @Test
        @DisplayName("Should return feedback with CRITICAL status")
        void shouldReturnFeedbackWithCriticalStatus() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Poor product", 2);
            Feedback feedback = createMockFeedback(4L, "Poor product", 2, StatusFeedback.CRITICAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertNotNull(response.getBody());
            assertEquals(StatusFeedback.CRITICAL, response.getBody().status());
        }

        @Test
        @DisplayName("Should return feedback with NORMAL status")
        void shouldReturnFeedbackWithNormalStatus() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Good product", 8);
            Feedback feedback = createMockFeedback(5L, "Good product", 8, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertNotNull(response.getBody());
            assertEquals(StatusFeedback.NORMAL, response.getBody().status());
        }

        @Test
        @DisplayName("Should include timestamps in response")
        void shouldIncludeTimestampsInResponse() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test feedback", 6);
            Feedback feedback = createMockFeedback(6L, "Test feedback", 6, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().createdAt());
            assertNotNull(response.getBody().updatedAt());
        }
    }

    @Nested
    @DisplayName("Rating Boundary Tests")
    class RatingBoundaryTests {

        @Test
        @DisplayName("Should handle rating 0")
        void shouldHandleRating0() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Terrible", 0);
            Feedback feedback = createMockFeedback(7L, "Terrible", 0, StatusFeedback.CRITICAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(0, response.getBody().rating());
        }

        @Test
        @DisplayName("Should handle rating 10")
        void shouldHandleRating10() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Perfect", 10);
            Feedback feedback = createMockFeedback(8L, "Perfect", 10, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(10, response.getBody().rating());
        }
    }

    @Nested
    @DisplayName("Description Edge Cases Tests")
    class DescriptionEdgeCasesTests {

        @Test
        @DisplayName("Should handle empty description")
        void shouldHandleEmptyDescription() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("", 5);
            Feedback feedback = createMockFeedback(9L, "", 5, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals("", response.getBody().description());
        }

        @Test
        @DisplayName("Should handle very long description")
        void shouldHandleVeryLongDescription() {
            // Arrange
            String longDescription = "a".repeat(1000);
            FeedbackRequest request = new FeedbackRequest(longDescription, 6);
            Feedback feedback = createMockFeedback(10L, longDescription, 6, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(longDescription, response.getBody().description());
        }

        @Test
        @DisplayName("Should handle description with special characters")
        void shouldHandleDescriptionWithSpecialCharacters() {
            // Arrange
            String specialDescription = "Feedback com caracteres especiais: @#$%^&*()[]{}|\\<>?/~`";
            FeedbackRequest request = new FeedbackRequest(specialDescription, 7);
            Feedback feedback = createMockFeedback(11L, specialDescription, 7, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(specialDescription, response.getBody().description());
        }

        @Test
        @DisplayName("Should handle description with unicode characters")
        void shouldHandleDescriptionWithUnicodeCharacters() {
            // Arrange
            String unicodeDescription = "Feedback com émojis 😀😃😄 e acentuação àáâãäå";
            FeedbackRequest request = new FeedbackRequest(unicodeDescription, 8);
            Feedback feedback = createMockFeedback(12L, unicodeDescription, 8, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(unicodeDescription, response.getBody().description());
        }

        @Test
        @DisplayName("Should handle description with line breaks")
        void shouldHandleDescriptionWithLineBreaks() {
            // Arrange
            String multilineDescription = "Primeira linha\nSegunda linha\nTerceira linha";
            FeedbackRequest request = new FeedbackRequest(multilineDescription, 5);
            Feedback feedback = createMockFeedback(13L, multilineDescription, 5, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(multilineDescription, response.getBody().description());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should propagate IllegalStateException from service")
        void shouldPropagateIllegalStateExceptionFromService() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test", 5);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class)))
                    .thenThrow(new IllegalStateException("SQS not configured"));

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> controller.feedbackInjection(request));
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException from service")
        void shouldPropagateIllegalArgumentExceptionFromService() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Test", 5);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class)))
                    .thenThrow(new IllegalArgumentException("Invalid rating"));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> controller.feedbackInjection(request));
        }
    }

    @Nested
    @DisplayName("All Rating Values Tests")
    class AllRatingValuesTests {

        @Test
        @DisplayName("Should set CRITICAL status for rating 1")
        void shouldSetCriticalStatusForRating1() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Very bad", 1);
            Feedback feedback = createMockFeedback(14L, "Very bad", 1, StatusFeedback.CRITICAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(StatusFeedback.CRITICAL, response.getBody().status());
        }

        @Test
        @DisplayName("Should set CRITICAL status for rating 2")
        void shouldSetCriticalStatusForRating2() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Bad", 2);
            Feedback feedback = createMockFeedback(15L, "Bad", 2, StatusFeedback.CRITICAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(StatusFeedback.CRITICAL, response.getBody().status());
        }

        @Test
        @DisplayName("Should set CRITICAL status for rating 3")
        void shouldSetCriticalStatusForRating3() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Poor", 3);
            Feedback feedback = createMockFeedback(16L, "Poor", 3, StatusFeedback.CRITICAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(StatusFeedback.CRITICAL, response.getBody().status());
        }

        @Test
        @DisplayName("Should set CRITICAL status for rating 4")
        void shouldSetCriticalStatusForRating4() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Below average", 4);
            Feedback feedback = createMockFeedback(17L, "Below average", 4, StatusFeedback.CRITICAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(StatusFeedback.CRITICAL, response.getBody().status());
        }

        @Test
        @DisplayName("Should set NORMAL status for rating 5")
        void shouldSetNormalStatusForRating5() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Average", 5);
            Feedback feedback = createMockFeedback(18L, "Average", 5, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(StatusFeedback.NORMAL, response.getBody().status());
        }

        @Test
        @DisplayName("Should set NORMAL status for rating 6")
        void shouldSetNormalStatusForRating6() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Above average", 6);
            Feedback feedback = createMockFeedback(19L, "Above average", 6, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(StatusFeedback.NORMAL, response.getBody().status());
        }

        @Test
        @DisplayName("Should set NORMAL status for rating 9")
        void shouldSetNormalStatusForRating9() {
            // Arrange
            FeedbackRequest request = new FeedbackRequest("Excellent", 9);
            Feedback feedback = createMockFeedback(20L, "Excellent", 9, StatusFeedback.NORMAL);

            when(feedbackInjectionService.processFeedback(any(CreateFeedback.class))).thenReturn(feedback);

            // Act
            ResponseEntity<FeedbackResponse> response = controller.feedbackInjection(request);

            // Assert
            assertEquals(StatusFeedback.NORMAL, response.getBody().status());
        }
    }

    // Helper method
    private Feedback createMockFeedback(Long id, String description, Integer rating, StatusFeedback status) {
        Feedback feedback = new Feedback();
        ReflectionTestUtils.setField(feedback, "id", id);
        feedback.setDescription(description);
        feedback.setRating(rating);
        feedback.setStatus(status);
        feedback.setCreatedAt(LocalDateTime.now());
        feedback.setUpdatedAt(LocalDateTime.now());
        return feedback;
    }
}
