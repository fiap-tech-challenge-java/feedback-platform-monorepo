package br.com.postech.feedback.ingestion.domain.service;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.repository.FeedbackRepository;
import br.com.postech.feedback.ingestion.domain.dto.CreateFeedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackInjectionService Tests")
class FeedbackInjectionServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private SqsClient sqsClient;

    private ObjectMapper objectMapper;
    private FeedbackInjectionService service;

    private static final String VALID_QUEUE_URL = "https://sqs.us-east-2.amazonaws.com/123456789012/feedback-analysis-queue";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        service = new FeedbackInjectionService(feedbackRepository, sqsClient, objectMapper);
        ReflectionTestUtils.setField(service, "queueUrl", VALID_QUEUE_URL);
    }

    @Nested
    @DisplayName("processFeedback() Tests")
    class ProcessFeedbackTests {

        @Test
        @DisplayName("Should process feedback and save to database")
        void shouldProcessFeedbackAndSaveToDatabase() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Great product", 8);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 1L);
                return feedback;
            });

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-123").build());

            // Act
            Feedback result = service.processFeedback(createFeedback);

            // Assert
            assertNotNull(result);
            assertEquals("Great product", result.getDescription());
            assertEquals(8, result.getRating());
            verify(feedbackRepository, times(1)).save(any(Feedback.class));
        }

        @Test
        @DisplayName("Should send message to SQS after saving")
        void shouldSendMessageToSqsAfterSaving() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Good experience", 7);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 2L);
                return feedback;
            });

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-456").build());

            // Act
            service.processFeedback(createFeedback);

            // Assert
            verify(sqsClient, times(1)).sendMessage(any(SendMessageRequest.class));
        }

        @Test
        @DisplayName("Should set CRITICAL status for rating below 5")
        void shouldSetCriticalStatusForRatingBelow5() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Bad product", 2);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 3L);
                return feedback;
            });

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-789").build());

            // Act
            Feedback result = service.processFeedback(createFeedback);

            // Assert
            assertEquals(StatusFeedback.CRITICAL, result.getStatus());
        }

        @Test
        @DisplayName("Should set NORMAL status for rating 5 or above")
        void shouldSetNormalStatusForRating5OrAbove() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Average product", 5);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 4L);
                return feedback;
            });

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-101").build());

            // Act
            Feedback result = service.processFeedback(createFeedback);

            // Assert
            assertEquals(StatusFeedback.NORMAL, result.getStatus());
        }

        @Test
        @DisplayName("Should include correct queue URL in SQS request")
        void shouldIncludeCorrectQueueUrlInSqsRequest() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Test feedback", 6);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 5L);
                return feedback;
            });

            ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-202").build());

            // Act
            service.processFeedback(createFeedback);

            // Assert
            verify(sqsClient).sendMessage(requestCaptor.capture());
            assertEquals(VALID_QUEUE_URL, requestCaptor.getValue().queueUrl());
        }

        @Test
        @DisplayName("Should include feedback data in SQS message body")
        void shouldIncludeFeedbackDataInSqsMessageBody() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Excellent service", 9);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 6L);
                return feedback;
            });

            ArgumentCaptor<SendMessageRequest> requestCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-303").build());

            // Act
            service.processFeedback(createFeedback);

            // Assert
            verify(sqsClient).sendMessage(requestCaptor.capture());
            String messageBody = requestCaptor.getValue().messageBody();
            assertTrue(messageBody.contains("Excellent service"));
            assertTrue(messageBody.contains("9"));
        }
    }

    @Nested
    @DisplayName("Queue URL Validation Tests")
    class QueueUrlValidationTests {

        @Test
        @DisplayName("Should throw exception when queue URL is null")
        void shouldThrowExceptionWhenQueueUrlIsNull() {
            // Arrange
            ReflectionTestUtils.setField(service, "queueUrl", null);
            CreateFeedback createFeedback = new CreateFeedback("Test", 5);

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> service.processFeedback(createFeedback));
        }

        @Test
        @DisplayName("Should throw exception when queue URL is blank")
        void shouldThrowExceptionWhenQueueUrlIsBlank() {
            // Arrange
            ReflectionTestUtils.setField(service, "queueUrl", "   ");
            CreateFeedback createFeedback = new CreateFeedback("Test", 5);

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> service.processFeedback(createFeedback));
        }

        @Test
        @DisplayName("Should throw exception when queue URL is empty")
        void shouldThrowExceptionWhenQueueUrlIsEmpty() {
            // Arrange
            ReflectionTestUtils.setField(service, "queueUrl", "");
            CreateFeedback createFeedback = new CreateFeedback("Test", 5);

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> service.processFeedback(createFeedback));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw RuntimeException when SQS fails")
        void shouldThrowRuntimeExceptionWhenSqsFails() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Test feedback", 5);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 7L);
                return feedback;
            });

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenThrow(new RuntimeException("SQS connection failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> service.processFeedback(createFeedback));
        }

        @Test
        @DisplayName("Should still save to database even if SQS fails")
        void shouldStillSaveToDatabaseEvenIfSqsFails() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Test feedback", 5);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 8L);
                return feedback;
            });

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenThrow(new RuntimeException("SQS connection failed"));

            // Act
            try {
                service.processFeedback(createFeedback);
            } catch (RuntimeException ignored) {
            }

            // Assert
            verify(feedbackRepository, times(1)).save(any(Feedback.class));
        }
    }

    @Nested
    @DisplayName("Rating Boundary Tests")
    class RatingBoundaryTests {

        @Test
        @DisplayName("Should process feedback with rating 0")
        void shouldProcessFeedbackWithRating0() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Terrible experience", 0);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 9L);
                return feedback;
            });

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-404").build());

            // Act
            Feedback result = service.processFeedback(createFeedback);

            // Assert
            assertEquals(0, result.getRating());
            assertEquals(StatusFeedback.CRITICAL, result.getStatus());
        }

        @Test
        @DisplayName("Should process feedback with rating 10")
        void shouldProcessFeedbackWithRating10() {
            // Arrange
            CreateFeedback createFeedback = new CreateFeedback("Perfect experience", 10);

            when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
                Feedback feedback = invocation.getArgument(0);
                ReflectionTestUtils.setField(feedback, "id", 10L);
                return feedback;
            });

            when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                    .thenReturn(SendMessageResponse.builder().messageId("msg-505").build());

            // Act
            Feedback result = service.processFeedback(createFeedback);

            // Assert
            assertEquals(10, result.getRating());
            assertEquals(StatusFeedback.NORMAL, result.getStatus());
        }
    }
}
