package br.com.postech.feedback.ingestion.controller;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.ingestion.domain.FeedbackResponse;
import br.com.postech.feedback.ingestion.domain.dto.CreateFeedback;
import br.com.postech.feedback.ingestion.domain.dto.FeedbackRequest;
import br.com.postech.feedback.ingestion.domain.mapper.FeedbackInjectionApiMapper;
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
