package br.com.postech.feedback.reporting.service;
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
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("SnsPublishService Tests")
class SnsPublishServiceTest {
    @Mock
    private SnsClient snsClient;
    private ObjectMapper objectMapper;
    private SnsPublishService service;
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        service = new SnsPublishService(snsClient, objectMapper);
        ReflectionTestUtils.setField(service, "topicArn", "arn:aws:sns:us-east-2:123456789012:test-topic");
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
    }
    @Nested
    @DisplayName("publishReportReadyEvent() Tests")
    class PublishReportReadyEventTests {
        @Test
        @DisplayName("Should publish report ready event successfully")
        void shouldPublishReportReadyEventSuccessfully() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-12345").build());
            assertDoesNotThrow(() -> service.publishReportReadyEvent(
                    "https://bucket.s3.amazonaws.com/report.csv",
                    "reports/2026/01/report.csv",
                    LocalDateTime.of(2026, 2, 9, 10, 30, 0),
                    100L, 4.5
            ));
            verify(snsClient, times(1)).publish(any(PublishRequest.class));
        }
        @Test
        @DisplayName("Should include correct topic ARN in request")
        void shouldIncludeCorrectTopicArnInRequest() {
            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());
            service.publishReportReadyEvent("https://url.com", "key", LocalDateTime.now(), 50L, 3.5);
            verify(snsClient).publish(captor.capture());
            assertEquals("arn:aws:sns:us-east-2:123456789012:test-topic", captor.getValue().topicArn());
        }
    }
    @Nested
    @DisplayName("Configuration Validation Tests")
    class ConfigurationValidationTests {
        @Test
        @DisplayName("Should throw exception when topic ARN is null")
        void shouldThrowExceptionWhenTopicArnIsNull() {
            ReflectionTestUtils.setField(service, "topicArn", null);
            assertThrows(IllegalStateException.class, () ->
                    service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 1L, 1.0));
        }
        @Test
        @DisplayName("Should throw exception when bucket name is null")
        void shouldThrowExceptionWhenBucketNameIsNull() {
            ReflectionTestUtils.setField(service, "bucketName", null);
            assertThrows(IllegalStateException.class, () ->
                    service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 1L, 1.0));
        }
    }
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        @Test
        @DisplayName("Should throw RuntimeException when SNS publish fails")
        void shouldThrowRuntimeExceptionWhenSnsPublishFails() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenThrow(new RuntimeException("SNS connection failed"));
            assertThrows(RuntimeException.class, () ->
                    service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 1L, 1.0));
        }

        @Test
        @DisplayName("Should throw RuntimeException with descriptive message")
        void shouldThrowRuntimeExceptionWithDescriptiveMessage() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenThrow(new RuntimeException("Network error"));

            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                    service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 1L, 1.0));

            assertTrue(exception.getMessage().contains("Failed to publish event to SNS"));
        }
    }

    @Nested
    @DisplayName("Message Content Tests")
    class MessageContentTests {

        @Test
        @DisplayName("Should include reportLink in message")
        void shouldIncludeReportLinkInMessage() {
            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            String reportUrl = "https://bucket.s3.amazonaws.com/reports/2026/02/report.csv";
            service.publishReportReadyEvent(reportUrl, "key", LocalDateTime.now(), 50L, 3.5);

            verify(snsClient).publish(captor.capture());
            assertTrue(captor.getValue().message().contains(reportUrl));
        }

        @Test
        @DisplayName("Should include totalFeedbacks in message")
        void shouldIncludeTotalFeedbacksInMessage() {
            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 999L, 3.5);

            verify(snsClient).publish(captor.capture());
            assertTrue(captor.getValue().message().contains("999"));
        }

        @Test
        @DisplayName("Should include averageScore in message")
        void shouldIncludeAverageScoreInMessage() {
            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 50L, 8.75);

            verify(snsClient).publish(captor.capture());
            assertTrue(captor.getValue().message().contains("8.75"));
        }

        @Test
        @DisplayName("Should include subject in publish request")
        void shouldIncludeSubjectInPublishRequest() {
            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 50L, 3.5);

            verify(snsClient).publish(captor.capture());
            assertEquals("Weekly Report Ready", captor.getValue().subject());
        }

        @Test
        @DisplayName("Should include eventType as ReportReady")
        void shouldIncludeEventTypeAsReportReady() {
            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 50L, 3.5);

            verify(snsClient).publish(captor.capture());
            assertTrue(captor.getValue().message().contains("ReportReady"));
        }

        @Test
        @DisplayName("Should include s3Key in message")
        void shouldIncludeS3KeyInMessage() {
            ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            String s3Key = "reports/2026/02/relatorio-semanal.csv";
            service.publishReportReadyEvent("url", s3Key, LocalDateTime.now(), 50L, 3.5);

            verify(snsClient).publish(captor.capture());
            assertTrue(captor.getValue().message().contains(s3Key));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero total feedbacks")
        void shouldHandleZeroTotalFeedbacks() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            assertDoesNotThrow(() -> service.publishReportReadyEvent(
                    "url", "key", LocalDateTime.now(), 0L, 0.0));
        }

        @Test
        @DisplayName("Should handle very large total feedbacks")
        void shouldHandleVeryLargeTotalFeedbacks() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            assertDoesNotThrow(() -> service.publishReportReadyEvent(
                    "url", "key", LocalDateTime.now(), Long.MAX_VALUE, 10.0));
        }

        @Test
        @DisplayName("Should handle maximum average score")
        void shouldHandleMaximumAverageScore() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            assertDoesNotThrow(() -> service.publishReportReadyEvent(
                    "url", "key", LocalDateTime.now(), 100L, 10.0));
        }

        @Test
        @DisplayName("Should handle minimum average score")
        void shouldHandleMinimumAverageScore() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            assertDoesNotThrow(() -> service.publishReportReadyEvent(
                    "url", "key", LocalDateTime.now(), 100L, 0.0));
        }

        @Test
        @DisplayName("Should handle past date")
        void shouldHandlePastDate() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            LocalDateTime pastDate = LocalDateTime.of(2020, 1, 1, 10, 0);
            assertDoesNotThrow(() -> service.publishReportReadyEvent(
                    "url", "key", pastDate, 100L, 5.0));
        }

        @Test
        @DisplayName("Should handle future date")
        void shouldHandleFutureDate() {
            when(snsClient.publish(any(PublishRequest.class)))
                    .thenReturn(PublishResponse.builder().messageId("msg-123").build());

            LocalDateTime futureDate = LocalDateTime.of(2030, 12, 31, 23, 59);
            assertDoesNotThrow(() -> service.publishReportReadyEvent(
                    "url", "key", futureDate, 100L, 5.0));
        }
    }

    @Nested
    @DisplayName("Configuration Edge Cases Tests")
    class ConfigurationEdgeCasesTests {

        @Test
        @DisplayName("Should throw exception when topic ARN is blank")
        void shouldThrowExceptionWhenTopicArnIsBlank() {
            ReflectionTestUtils.setField(service, "topicArn", "   ");
            assertThrows(IllegalStateException.class, () ->
                    service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 1L, 1.0));
        }

        @Test
        @DisplayName("Should throw exception when topic ARN is empty")
        void shouldThrowExceptionWhenTopicArnIsEmpty() {
            ReflectionTestUtils.setField(service, "topicArn", "");
            assertThrows(IllegalStateException.class, () ->
                    service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 1L, 1.0));
        }

        @Test
        @DisplayName("Should throw exception when bucket name is blank")
        void shouldThrowExceptionWhenBucketNameIsBlank() {
            ReflectionTestUtils.setField(service, "bucketName", "   ");
            assertThrows(IllegalStateException.class, () ->
                    service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 1L, 1.0));
        }

        @Test
        @DisplayName("Should throw exception when bucket name is empty")
        void shouldThrowExceptionWhenBucketNameIsEmpty() {
            ReflectionTestUtils.setField(service, "bucketName", "");
            assertThrows(IllegalStateException.class, () ->
                    service.publishReportReadyEvent("url", "key", LocalDateTime.now(), 1L, 1.0));
        }
    }
}
