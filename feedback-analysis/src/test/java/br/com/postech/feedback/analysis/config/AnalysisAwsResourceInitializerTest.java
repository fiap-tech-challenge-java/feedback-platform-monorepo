package br.com.postech.feedback.analysis.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalysisAwsResourceInitializer Tests")
class AnalysisAwsResourceInitializerTest {

    @Mock
    private SnsClient snsClient;

    @Mock
    private SqsClient sqsClient;

    private AnalysisAwsResourceInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new AnalysisAwsResourceInitializer(snsClient, sqsClient);
        // Configure endpoint to enable LocalStack mode
        ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
        ReflectionTestUtils.setField(initializer, "autoInit", true);
    }

    @Nested
    @DisplayName("SQS Queue Creation Tests")
    class SqsQueueCreationTests {

        @Test
        @DisplayName("Should create SQS queue successfully")
        void shouldCreateSqsQueueSuccessfully() throws Exception {
            // Arrange
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenReturn(CreateQueueResponse.builder()
                            .queueUrl("http://localhost:4566/queue/feedback-analysis-queue")
                            .build());

            when(snsClient.createTopic(any(CreateTopicRequest.class)))
                    .thenReturn(CreateTopicResponse.builder()
                            .topicArn("arn:aws:sns:us-east-2:000000000000:feedback-notifications")
                            .build());

            // Act
            initializer.run();

            // Assert
            verify(sqsClient, times(1)).createQueue(any(CreateQueueRequest.class));
        }

        @Test
        @DisplayName("Should handle queue already exists")
        void shouldHandleQueueAlreadyExists() throws Exception {
            // Arrange
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenThrow(QueueNameExistsException.builder()
                            .message("Queue already exists")
                            .build());

            when(snsClient.createTopic(any(CreateTopicRequest.class)))
                    .thenReturn(CreateTopicResponse.builder()
                            .topicArn("arn:aws:sns:us-east-2:000000000000:feedback-notifications")
                            .build());

            // Act - should not throw exception
            assertDoesNotThrow(() -> initializer.run());

            // Assert
            verify(sqsClient, times(1)).createQueue(any(CreateQueueRequest.class));
        }

        @Test
        @DisplayName("Should handle generic SQS error")
        void shouldHandleGenericSqsError() throws Exception {
            // Arrange
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenThrow(new RuntimeException("Connection error"));

            when(snsClient.createTopic(any(CreateTopicRequest.class)))
                    .thenReturn(CreateTopicResponse.builder()
                            .topicArn("arn:aws:sns:us-east-2:000000000000:feedback-notifications")
                            .build());

            // Act - should not throw exception
            assertDoesNotThrow(() -> initializer.run());

            // Assert
            verify(sqsClient, times(1)).createQueue(any(CreateQueueRequest.class));
        }
    }

    @Nested
    @DisplayName("SNS Topic Creation Tests")
    class SnsTopicCreationTests {

        @Test
        @DisplayName("Should create SNS topic successfully")
        void shouldCreateSnsTopicSuccessfully() throws Exception {
            // Arrange
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenReturn(CreateQueueResponse.builder()
                            .queueUrl("http://localhost:4566/queue/feedback-analysis-queue")
                            .build());

            when(snsClient.createTopic(any(CreateTopicRequest.class)))
                    .thenReturn(CreateTopicResponse.builder()
                            .topicArn("arn:aws:sns:us-east-2:000000000000:feedback-notifications")
                            .build());

            // Act
            initializer.run();

            // Assert
            verify(snsClient, times(1)).createTopic(any(CreateTopicRequest.class));
        }

        @Test
        @DisplayName("Should handle SNS topic creation error")
        void shouldHandleSnsTopicCreationError() throws Exception {
            // Arrange
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenReturn(CreateQueueResponse.builder()
                            .queueUrl("http://localhost:4566/queue/feedback-analysis-queue")
                            .build());

            when(snsClient.createTopic(any(CreateTopicRequest.class)))
                    .thenThrow(new RuntimeException("SNS connection error"));

            // Act - should not throw exception
            assertDoesNotThrow(() -> initializer.run());

            // Assert
            verify(snsClient, times(1)).createTopic(any(CreateTopicRequest.class));
        }
    }

    @Nested
    @DisplayName("Full Initialization Tests")
    class FullInitializationTests {

        @Test
        @DisplayName("Should initialize all resources successfully")
        void shouldInitializeAllResourcesSuccessfully() throws Exception {
            // Arrange
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenReturn(CreateQueueResponse.builder()
                            .queueUrl("http://localhost:4566/queue/feedback-analysis-queue")
                            .build());

            when(snsClient.createTopic(any(CreateTopicRequest.class)))
                    .thenReturn(CreateTopicResponse.builder()
                            .topicArn("arn:aws:sns:us-east-2:000000000000:feedback-notifications")
                            .build());

            // Act
            initializer.run();

            // Assert
            verify(sqsClient, times(1)).createQueue(any(CreateQueueRequest.class));
            verify(snsClient, times(1)).createTopic(any(CreateTopicRequest.class));
        }

        @Test
        @DisplayName("Should initialize resources in correct order (queue first, then topic)")
        void shouldInitializeResourcesInCorrectOrder() throws Exception {
            // Arrange
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenReturn(CreateQueueResponse.builder()
                            .queueUrl("http://localhost:4566/queue/feedback-analysis-queue")
                            .build());

            when(snsClient.createTopic(any(CreateTopicRequest.class)))
                    .thenReturn(CreateTopicResponse.builder()
                            .topicArn("arn:aws:sns:us-east-2:000000000000:feedback-notifications")
                            .build());

            // Act
            initializer.run();

            // Assert
            var inOrder = inOrder(sqsClient, snsClient);
            inOrder.verify(sqsClient).createQueue(any(CreateQueueRequest.class));
            inOrder.verify(snsClient).createTopic(any(CreateTopicRequest.class));
        }

        @Test
        @DisplayName("Should continue topic creation even if queue creation fails")
        void shouldContinueTopicCreationEvenIfQueueCreationFails() throws Exception {
            // Arrange
            when(sqsClient.createQueue(any(CreateQueueRequest.class)))
                    .thenThrow(new RuntimeException("Queue creation failed"));

            when(snsClient.createTopic(any(CreateTopicRequest.class)))
                    .thenReturn(CreateTopicResponse.builder()
                            .topicArn("arn:aws:sns:us-east-2:000000000000:feedback-notifications")
                            .build());

            // Act
            initializer.run();

            // Assert - topic creation should still be called
            verify(snsClient, times(1)).createTopic(any(CreateTopicRequest.class));
        }

        @Test
        @DisplayName("Should skip initialization when autoInit is disabled")
        void shouldSkipInitializationWhenAutoInitDisabled() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "autoInit", false);

            // Act
            initializer.run();

            // Assert - no AWS calls should be made
            verify(sqsClient, never()).createQueue(any(CreateQueueRequest.class));
            verify(snsClient, never()).createTopic(any(CreateTopicRequest.class));
        }

        @Test
        @DisplayName("Should skip initialization when endpoint is null (AWS mode)")
        void shouldSkipInitializationWhenEndpointIsNull() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", null);

            // Act
            initializer.run();

            // Assert - no AWS calls should be made
            verify(sqsClient, never()).createQueue(any(CreateQueueRequest.class));
            verify(snsClient, never()).createTopic(any(CreateTopicRequest.class));
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create initializer with valid clients")
        void shouldCreateInitializerWithValidClients() {
            // Arrange & Act
            AnalysisAwsResourceInitializer newInitializer = new AnalysisAwsResourceInitializer(snsClient, sqsClient);

            // Assert
            assertNotNull(newInitializer);
        }
    }
}
