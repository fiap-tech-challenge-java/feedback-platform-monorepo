package br.com.postech.feedback.analysis.service;

import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackAnalysisService Tests")
class FeedbackAnalysisServiceTest {

    @Mock
    private SnsClient snsClient;

    private ObjectMapper objectMapper;
    private FeedbackAnalysisService service;

    private static final String VALID_TOPIC_ARN = "arn:aws:sns:us-east-2:123456789012:feedback-notifications";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        service = new FeedbackAnalysisService(snsClient, objectMapper);
        ReflectionTestUtils.setField(service, "topicArn", VALID_TOPIC_ARN);
    }

    @Nested
    @DisplayName("Lambda Mode - analyzeFeedback()")
    class LambdaModeTests {

        @Test
        @DisplayName("Should process critical feedback and send SNS notification")
        void shouldProcessCriticalFeedbackAndSendSnsNotification() throws Exception {
            // Arrange
            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    1L,
                    "Produto com defeito grave",
                    2,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            SQSEvent sqsEvent = createSqsEvent(objectMapper.writeValueAsString(feedbackEvent));

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            Consumer<SQSEvent> consumer = service.analyzeFeedback();
            consumer.accept(sqsEvent);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should not send SNS notification for normal feedback")
        void shouldNotSendSnsNotificationForNormalFeedback() throws Exception {
            // Arrange
            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    2L,
                    "Produto excelente, muito satisfeito",
                    5,
                    StatusFeedback.NORMAL,
                    LocalDateTime.now()
            );

            SQSEvent sqsEvent = createSqsEvent(objectMapper.writeValueAsString(feedbackEvent));

            // Act
            Consumer<SQSEvent> consumer = service.analyzeFeedback();
            consumer.accept(sqsEvent);

            // Assert
            verify(snsClient, never()).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process multiple messages in batch")
        void shouldProcessMultipleMessagesInBatch() throws Exception {
            // Arrange
            FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                    1L,
                    "Feedback crítico",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            FeedbackEventDTO normalFeedback = new FeedbackEventDTO(
                    2L,
                    "Feedback normal",
                    5,
                    StatusFeedback.NORMAL,
                    LocalDateTime.now()
            );

            SQSEvent sqsEvent = createSqsEventWithMultipleMessages(
                    objectMapper.writeValueAsString(criticalFeedback),
                    objectMapper.writeValueAsString(normalFeedback)
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            Consumer<SQSEvent> consumer = service.analyzeFeedback();
            consumer.accept(sqsEvent);

            // Assert - Only 1 SNS notification (for critical feedback)
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should log error but not throw exception for invalid JSON message")
        void shouldLogErrorButNotThrowExceptionForInvalidJsonMessage() {
            // Arrange
            SQSEvent sqsEvent = createSqsEvent("invalid json {{{");

            // Act & Assert - JsonProcessingException is caught and logged, not rethrown
            Consumer<SQSEvent> consumer = service.analyzeFeedback();
            assertDoesNotThrow(() -> consumer.accept(sqsEvent));

            // Verify no SNS publish since the message couldn't be parsed
            verify(snsClient, never()).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should include correct subject and message in SNS publish")
        void shouldIncludeCorrectSubjectAndMessageInSnsPublish() throws Exception {
            // Arrange
            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    100L,
                    "Feedback crítico para análise",
                    0,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            SQSEvent sqsEvent = createSqsEvent(objectMapper.writeValueAsString(feedbackEvent));

            ArgumentCaptor<PublishRequest> requestCaptor = ArgumentCaptor.forClass(PublishRequest.class);
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            Consumer<SQSEvent> consumer = service.analyzeFeedback();
            consumer.accept(sqsEvent);

            // Assert
            verify(snsClient).publish(requestCaptor.capture());
            PublishRequest capturedRequest = requestCaptor.getValue();

            assertEquals(VALID_TOPIC_ARN, capturedRequest.topicArn());
            assertEquals("ALERTA: Novo Feedback Crítico", capturedRequest.subject());
            assertTrue(capturedRequest.message().contains("100"));
            assertTrue(capturedRequest.message().contains("CRITICAL"));
        }

        @Test
        @DisplayName("Should handle empty SQS event batch")
        void shouldHandleEmptySqsEventBatch() {
            // Arrange
            SQSEvent sqsEvent = new SQSEvent();
            sqsEvent.setRecords(Collections.emptyList());

            // Act
            Consumer<SQSEvent> consumer = service.analyzeFeedback();
            assertDoesNotThrow(() -> consumer.accept(sqsEvent));

            // Assert
            verify(snsClient, never()).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process all messages even when one has invalid JSON")
        void shouldProcessAllMessagesEvenWhenOneHasInvalidJson() throws Exception {
            // Arrange
            FeedbackEventDTO validFeedback = new FeedbackEventDTO(
                    1L,
                    "Valid critical feedback",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            SQSEvent sqsEvent = createSqsEventWithMultipleMessages(
                    "invalid json {{{",
                    objectMapper.writeValueAsString(validFeedback)
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            Consumer<SQSEvent> consumer = service.analyzeFeedback();
            assertDoesNotThrow(() -> consumer.accept(sqsEvent));

            // Assert - Only 1 SNS notification for the valid message
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should handle large batch of messages")
        void shouldHandleLargeBatchOfMessages() throws Exception {
            // Arrange - Create 10 messages (5 critical, 5 normal)
            String[] messages = new String[10];
            for (int i = 0; i < 10; i++) {
                FeedbackEventDTO feedback = new FeedbackEventDTO(
                        (long) i,
                        "Feedback " + i,
                        i < 5 ? 1 : 8,  // First 5 are critical, last 5 are normal
                        i < 5 ? StatusFeedback.CRITICAL : StatusFeedback.NORMAL,
                        LocalDateTime.now()
                );
                messages[i] = objectMapper.writeValueAsString(feedback);
            }

            SQSEvent sqsEvent = createSqsEventWithMultipleMessages(messages);

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            Consumer<SQSEvent> consumer = service.analyzeFeedback();
            consumer.accept(sqsEvent);

            // Assert - 5 SNS notifications for critical feedbacks
            verify(snsClient, times(5)).publish(any(PublishRequest.class));
        }
    }

    @Nested
    @DisplayName("Local Mode - listen()")
    class LocalModeTests {

        @Test
        @DisplayName("Should process critical feedback via SqsListener")
        void shouldProcessCriticalFeedbackViaSqsListener() {
            // Arrange
            FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                    1L,
                    "Feedback crítico via listener",
                    2,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            service.listen(criticalFeedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should not send notification for normal feedback via SqsListener")
        void shouldNotSendNotificationForNormalFeedbackViaSqsListener() {
            // Arrange
            FeedbackEventDTO normalFeedback = new FeedbackEventDTO(
                    2L,
                    "Feedback normal via listener",
                    5,
                    StatusFeedback.NORMAL,
                    LocalDateTime.now()
            );

            // Act
            service.listen(normalFeedback);

            // Assert
            verify(snsClient, never()).publish(any(PublishRequest.class));
        }
    }

    @Nested
    @DisplayName("Topic ARN Validation Tests")
    class TopicArnValidationTests {

        @Test
        @DisplayName("Should throw exception when topic ARN is null")
        void shouldThrowExceptionWhenTopicArnIsNull() {
            // Arrange
            ReflectionTestUtils.setField(service, "topicArn", null);

            FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                    1L,
                    "Feedback crítico",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> service.listen(criticalFeedback));
        }

        @Test
        @DisplayName("Should throw exception when topic ARN is blank")
        void shouldThrowExceptionWhenTopicArnIsBlank() {
            // Arrange
            ReflectionTestUtils.setField(service, "topicArn", "   ");

            FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                    1L,
                    "Feedback crítico",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> service.listen(criticalFeedback));
        }

        @Test
        @DisplayName("Should throw exception when topic ARN is empty")
        void shouldThrowExceptionWhenTopicArnIsEmpty() {
            // Arrange
            ReflectionTestUtils.setField(service, "topicArn", "");

            FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                    1L,
                    "Feedback crítico",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            // Act & Assert
            assertThrows(IllegalStateException.class, () -> service.listen(criticalFeedback));
        }
    }

    @Nested
    @DisplayName("SNS Publish Error Handling Tests")
    class SnsErrorHandlingTests {

        @Test
        @DisplayName("Should throw RuntimeException when SNS publish fails")
        void shouldThrowRuntimeExceptionWhenSnsPublishFails() {
            // Arrange
            FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                    1L,
                    "Feedback crítico",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenThrow(new RuntimeException("SNS connection failed"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> service.listen(criticalFeedback));
        }

        @Test
        @DisplayName("Should log error with proper context when SNS publish fails")
        void shouldLogErrorWithProperContextWhenSnsPublishFails() {
            // Arrange
            FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                    99L,
                    "Feedback crítico para teste de erro",
                    0,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenThrow(new RuntimeException("Network timeout"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> service.listen(criticalFeedback));
            assertEquals("Erro na publicação SNS", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw RuntimeException when SNS returns error in Lambda mode")
        void shouldThrowRuntimeExceptionWhenSnsReturnsErrorInLambdaMode() throws Exception {
            // Arrange
            FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                    1L,
                    "Feedback crítico",
                    2,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            SQSEvent sqsEvent = createSqsEvent(objectMapper.writeValueAsString(criticalFeedback));

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenThrow(new RuntimeException("SNS service unavailable"));

            // Act
            Consumer<SQSEvent> consumer = service.analyzeFeedback();

            // Assert
            assertThrows(RuntimeException.class, () -> consumer.accept(sqsEvent));
        }
    }

    @Nested
    @DisplayName("Feedback Description Edge Cases")
    class FeedbackDescriptionEdgeCases {

        @Test
        @DisplayName("Should process feedback with empty description")
        void shouldProcessFeedbackWithEmptyDescription() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    1L,
                    "",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process feedback with very long description")
        void shouldProcessFeedbackWithVeryLongDescription() {
            // Arrange
            String longDescription = "a".repeat(1000);
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    2L,
                    longDescription,
                    2,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-456").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process feedback with special characters in description")
        void shouldProcessFeedbackWithSpecialCharactersInDescription() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    3L,
                    "Feedback com caracteres especiais: @#$%^&*()[]{}|\\<>?/~`",
                    3,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-789").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process feedback with unicode characters in description")
        void shouldProcessFeedbackWithUnicodeCharactersInDescription() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    4L,
                    "Feedback com emojis: 😀😃😄😁 e acentos: àáâãäåçèéêë",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-abc").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process feedback with line breaks in description")
        void shouldProcessFeedbackWithLineBreaksInDescription() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    5L,
                    "Feedback com\nquebras\nde\nlinha",
                    2,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-def").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }
    }

    @Nested
    @DisplayName("Timestamp Tests")
    class TimestampTests {

        @Test
        @DisplayName("Should process feedback with past timestamp")
        void shouldProcessFeedbackWithPastTimestamp() {
            // Arrange
            LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 0, 0);
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    1L,
                    "Feedback antigo",
                    1,
                    StatusFeedback.CRITICAL,
                    pastDate
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process feedback with future timestamp")
        void shouldProcessFeedbackWithFutureTimestamp() {
            // Arrange
            LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59);
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    2L,
                    "Feedback futuro",
                    0,
                    StatusFeedback.CRITICAL,
                    futureDate
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-456").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }
    }

    @Nested
    @DisplayName("Message ID Tests")
    class MessageIdTests {

        @Test
        @DisplayName("Should process feedback with very large ID")
        void shouldProcessFeedbackWithVeryLargeId() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    Long.MAX_VALUE,
                    "Feedback com ID máximo",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process feedback with ID zero")
        void shouldProcessFeedbackWithIdZero() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    0L,
                    "Feedback com ID zero",
                    2,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-456").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }
    }

    @Nested
    @DisplayName("Rating Boundary Tests")
    class RatingBoundaryTests {

        @Test
        @DisplayName("Should process critical feedback with rating 0")
        void shouldProcessCriticalFeedbackWithRating0() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    1L,
                    "Pior experiência possível",
                    0,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process critical feedback with rating 4")
        void shouldProcessCriticalFeedbackWithRating4() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    2L,
                    "Experiência abaixo da média",
                    4,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-456").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should not send notification for normal feedback with rating 5")
        void shouldNotSendNotificationForNormalFeedbackWithRating5() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    3L,
                    "Experiência perfeita",
                    5,
                    StatusFeedback.NORMAL,
                    LocalDateTime.now()
            );

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, never()).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process critical feedback with rating 1")
        void shouldProcessCriticalFeedbackWithRating1() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    4L,
                    "Muito insatisfeito",
                    1,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-789").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should process critical feedback with rating 3")
        void shouldProcessCriticalFeedbackWithRating3() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    5L,
                    "Insatisfeito",
                    3,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-abc").build());

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should not send notification for normal feedback with rating 6")
        void shouldNotSendNotificationForNormalFeedbackWithRating6() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    6L,
                    "Experiência satisfatória",
                    6,
                    StatusFeedback.NORMAL,
                    LocalDateTime.now()
            );

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, never()).publish(any(PublishRequest.class));
        }

        @Test
        @DisplayName("Should not send notification for normal feedback with rating 10")
        void shouldNotSendNotificationForNormalFeedbackWithRating10() {
            // Arrange
            FeedbackEventDTO feedback = new FeedbackEventDTO(
                    7L,
                    "Experiência excepcional",
                    10,
                    StatusFeedback.NORMAL,
                    LocalDateTime.now()
            );

            // Act
            service.listen(feedback);

            // Assert
            verify(snsClient, never()).publish(any(PublishRequest.class));
        }
    }

    // Helper methods
    private SQSEvent createSqsEvent(String messageBody) {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setMessageId("test-msg-id");
        sqsMessage.setBody(messageBody);
        sqsEvent.setRecords(Collections.singletonList(sqsMessage));
        return sqsEvent;
    }

    private SQSEvent createSqsEventWithMultipleMessages(String... messageBodies) {
        SQSEvent sqsEvent = new SQSEvent();
        List<SQSEvent.SQSMessage> messages = new java.util.ArrayList<>();

        for (int i = 0; i < messageBodies.length; i++) {
            SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
            sqsMessage.setMessageId("test-msg-id-" + i);
            sqsMessage.setBody(messageBodies[i]);
            messages.add(sqsMessage);
        }

        sqsEvent.setRecords(messages);
        return sqsEvent;
    }
}
