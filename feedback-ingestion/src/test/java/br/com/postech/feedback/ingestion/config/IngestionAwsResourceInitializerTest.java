package br.com.postech.feedback.ingestion.config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("IngestionAwsResourceInitializer Tests")
class IngestionAwsResourceInitializerTest {
    @Mock
    private SqsClient sqsClient;
    private IngestionAwsResourceInitializer initializer;
    @BeforeEach
    void setUp() {
        initializer = new IngestionAwsResourceInitializer(sqsClient);
        ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
        ReflectionTestUtils.setField(initializer, "autoInit", true);
    }
    @Nested
    @DisplayName("SQS Queue Creation Tests")
    class SqsQueueCreationTests {
        @Test
        @DisplayName("Should create SQS queue successfully")
        void shouldCreateSqsQueueSuccessfully() throws Exception {
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenReturn(CreateQueueResponse.builder()
                            .queueUrl("http://localhost:4566/queue/feedback-analysis-queue")
                            .build());
            initializer.run();
            verify(sqsClient, times(1)).createQueue(any(CreateQueueRequest.class));
        }
        @Test
        @DisplayName("Should handle queue already exists")
        void shouldHandleQueueAlreadyExists() throws Exception {
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenThrow(QueueNameExistsException.builder()
                            .message("Queue already exists")
                            .build());
            assertDoesNotThrow(() -> initializer.run());
            verify(sqsClient, times(1)).createQueue(any(CreateQueueRequest.class));
        }
        @Test
        @DisplayName("Should handle generic SQS error")
        void shouldHandleGenericSqsError() throws Exception {
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenThrow(new RuntimeException("Connection error"));
            assertDoesNotThrow(() -> initializer.run());
            verify(sqsClient, times(1)).createQueue(any(CreateQueueRequest.class));
        }
    }
    @Nested
    @DisplayName("Initialization Control Tests")
    class InitializationControlTests {
        @Test
        @DisplayName("Should skip initialization when autoInit is disabled")
        void shouldSkipInitializationWhenAutoInitDisabled() throws Exception {
            ReflectionTestUtils.setField(initializer, "autoInit", false);
            initializer.run();
            verify(sqsClient, never()).createQueue(any(CreateQueueRequest.class));
        }
        @Test
        @DisplayName("Should skip initialization when endpoint is null (AWS mode)")
        void shouldSkipInitializationWhenEndpointIsNull() throws Exception {
            ReflectionTestUtils.setField(initializer, "endpoint", null);
            initializer.run();
            verify(sqsClient, never()).createQueue(any(CreateQueueRequest.class));
        }
        @Test
        @DisplayName("Should skip initialization when endpoint is empty")
        void shouldSkipInitializationWhenEndpointIsEmpty() throws Exception {
            ReflectionTestUtils.setField(initializer, "endpoint", "");
            initializer.run();
            verify(sqsClient, never()).createQueue(any(CreateQueueRequest.class));
        }
    }
    @Nested
    @DisplayName("ensureQueueExists() Tests")
    class EnsureQueueExistsTests {
        @Test
        @DisplayName("Should call createQueue when ensureQueueExists is called")
        void shouldCallCreateQueueWhenEnsureQueueExistsIsCalled() {
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenReturn(CreateQueueResponse.builder()
                            .queueUrl("http://localhost:4566/queue/feedback-analysis-queue")
                            .build());
            initializer.ensureQueueExists();
            verify(sqsClient, times(1)).createQueue(any(CreateQueueRequest.class));
        }
    }
}
