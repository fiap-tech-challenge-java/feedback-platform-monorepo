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
    }
}
