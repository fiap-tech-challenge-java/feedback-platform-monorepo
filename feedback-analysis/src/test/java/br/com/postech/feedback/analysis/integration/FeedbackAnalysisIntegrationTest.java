package br.com.postech.feedback.analysis.integration;

import br.com.postech.feedback.analysis.service.FeedbackAnalysisService;
import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes de integração para o FeedbackAnalysisService.
 * Testa o fluxo completo com o contexto Spring carregado.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.aws.sqs.enabled=false",
        "spring.cloud.aws.sns.enabled=false",
        "spring.cloud.aws.region.static=us-east-2",
        "spring.cloud.aws.endpoint=",
        "SNS_TOPIC_ARN=arn:aws:sns:us-east-2:123456789012:test-topic"
})
@ExtendWith(MockitoExtension.class)
@DisplayName("Feedback Analysis Integration Tests")
class FeedbackAnalysisIntegrationTest {

    @MockBean
    private SnsClient snsClient;

    @MockBean
    private SqsClient sqsClient;

    @MockBean
    private SqsAsyncClient sqsAsyncClient;

    @Autowired
    private FeedbackAnalysisService feedbackAnalysisService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_TOPIC_ARN = "arn:aws:sns:us-east-2:123456789012:test-topic";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(feedbackAnalysisService, "topicArn", VALID_TOPIC_ARN);
    }

    @Test
    @DisplayName("Should process critical feedback through analyzeFeedback bean")
    void shouldProcessCriticalFeedbackThroughAnalyzeFeedbackBean() throws Exception {
        // Arrange
        FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                1L,
                "Produto com defeito grave",
                1,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        SQSEvent sqsEvent = createSqsEvent(objectMapper.writeValueAsString(criticalFeedback));

        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("msg-123").build());

        // Act
        Consumer<SQSEvent> consumer = feedbackAnalysisService.analyzeFeedback();
        consumer.accept(sqsEvent);

        // Assert
        verify(snsClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("Should process normal feedback without sending SNS notification")
    void shouldProcessNormalFeedbackWithoutSendingSnsNotification() throws Exception {
        // Arrange
        FeedbackEventDTO normalFeedback = new FeedbackEventDTO(
                2L,
                "Produto excelente",
                10,
                StatusFeedback.NORMAL,
                LocalDateTime.now()
        );

        SQSEvent sqsEvent = createSqsEvent(objectMapper.writeValueAsString(normalFeedback));

        // Act
        Consumer<SQSEvent> consumer = feedbackAnalysisService.analyzeFeedback();
        consumer.accept(sqsEvent);

        // Assert
        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("Should process feedback through SqsListener")
    void shouldProcessFeedbackThroughSqsListener() {
        // Arrange
        FeedbackEventDTO criticalFeedback = new FeedbackEventDTO(
                3L,
                "Feedback crítico através do listener",
                2,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("msg-456").build());

        // Act
        feedbackAnalysisService.listen(criticalFeedback);

        // Assert
        verify(snsClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("Should have ObjectMapper bean properly configured")
    void shouldHaveObjectMapperBeanProperlyConfigured() {
        // Assert
        assertNotNull(objectMapper);
        assertNotNull(objectMapper.getRegisteredModuleIds());
        assertFalse(objectMapper.getRegisteredModuleIds().isEmpty());
    }

    @Test
    @DisplayName("Should serialize and deserialize FeedbackEventDTO correctly")
    void shouldSerializeAndDeserializeFeedbackEventDtoCorrectly() throws Exception {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        FeedbackEventDTO original = new FeedbackEventDTO(
                100L,
                "Test feedback",
                5,
                StatusFeedback.NORMAL,
                now
        );

        // Act
        String json = objectMapper.writeValueAsString(original);
        FeedbackEventDTO deserialized = objectMapper.readValue(json, FeedbackEventDTO.class);

        // Assert
        assertNotNull(json);
        assertNotNull(deserialized);
        assertEquals(original.id(), deserialized.id());
        assertEquals(original.description(), deserialized.description());
        assertEquals(original.rating(), deserialized.rating());
        assertEquals(original.status(), deserialized.status());
    }

    @Test
    @DisplayName("Should handle batch processing with mixed feedback types")
    void shouldHandleBatchProcessingWithMixedFeedbackTypes() throws Exception {
        // Arrange
        FeedbackEventDTO critical1 = new FeedbackEventDTO(1L, "Critical 1", 1, StatusFeedback.CRITICAL, LocalDateTime.now());
        FeedbackEventDTO normal1 = new FeedbackEventDTO(2L, "Normal 1", 8, StatusFeedback.NORMAL, LocalDateTime.now());
        FeedbackEventDTO critical2 = new FeedbackEventDTO(3L, "Critical 2", 2, StatusFeedback.CRITICAL, LocalDateTime.now());
        FeedbackEventDTO normal2 = new FeedbackEventDTO(4L, "Normal 2", 9, StatusFeedback.NORMAL, LocalDateTime.now());

        SQSEvent sqsEvent = createSqsEventWithMultipleMessages(
                objectMapper.writeValueAsString(critical1),
                objectMapper.writeValueAsString(normal1),
                objectMapper.writeValueAsString(critical2),
                objectMapper.writeValueAsString(normal2)
        );

        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("msg-123").build());

        // Act
        Consumer<SQSEvent> consumer = feedbackAnalysisService.analyzeFeedback();
        consumer.accept(sqsEvent);

        // Assert - Only 2 SNS notifications for critical feedbacks
        verify(snsClient, times(2)).publish(any(PublishRequest.class));
    }

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
        java.util.List<SQSEvent.SQSMessage> messages = new java.util.ArrayList<>();

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

