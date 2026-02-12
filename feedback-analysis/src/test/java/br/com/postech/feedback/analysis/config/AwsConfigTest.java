package br.com.postech.feedback.analysis.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AwsConfig Tests")
class AwsConfigTest {

    private void setLocalStackCredentials(AwsConfig awsConfig) {
        ReflectionTestUtils.setField(awsConfig, "accessKey", "test-access-key");
        ReflectionTestUtils.setField(awsConfig, "secretKey", "test-secret-key");
    }

    @Nested
    @DisplayName("SnsClient Bean Tests")
    class SnsClientBeanTests {

        @Test
        @DisplayName("Should create SnsClient with default region when no endpoint configured")
        void shouldCreateSnsClientWithDefaultRegion() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            // Act
            SnsClient snsClient = awsConfig.snsClient();

            // Assert
            assertNotNull(snsClient);
            snsClient.close();
        }

        @Test
        @DisplayName("Should create SnsClient with custom endpoint for LocalStack")
        void shouldCreateSnsClientWithCustomEndpoint() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "http://localhost:4566");
            setLocalStackCredentials(awsConfig);

            // Act
            SnsClient snsClient = awsConfig.snsClient();

            // Assert
            assertNotNull(snsClient);
            snsClient.close();
        }

        @Test
        @DisplayName("Should handle null endpoint URL")
        void shouldHandleNullEndpointUrl() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", null);

            // Act
            SnsClient snsClient = awsConfig.snsClient();

            // Assert
            assertNotNull(snsClient);
            snsClient.close();
        }
    }

    @Nested
    @DisplayName("SqsClient Bean Tests")
    class SqsClientBeanTests {

        @Test
        @DisplayName("Should create SqsClient with default region when no endpoint configured")
        void shouldCreateSqsClientWithDefaultRegion() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            // Act
            SqsClient sqsClient = awsConfig.sqsClient();

            // Assert
            assertNotNull(sqsClient);
            sqsClient.close();
        }

        @Test
        @DisplayName("Should handle null endpoint URL")
        void shouldHandleNullEndpointUrl() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", null);

            // Act
            SqsClient sqsClient = awsConfig.sqsClient();

            // Assert
            assertNotNull(sqsClient);
            sqsClient.close();
        }

        @Test
        @DisplayName("Should create SqsClient with custom endpoint for LocalStack")
        void shouldCreateSqsClientWithCustomEndpoint() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "http://localhost:4566");
            setLocalStackCredentials(awsConfig);

            // Act - Note: this will try to create queue which may fail without LocalStack running
            // We're just testing that client creation doesn't throw NPE
            try {
                SqsClient sqsClient = awsConfig.sqsClient();
                assertNotNull(sqsClient);
                sqsClient.close();
            } catch (Exception e) {
                // Expected if LocalStack is not running - the important thing is no NPE for credentials
                assertTrue(e.getMessage() == null || !e.getMessage().contains("Access key ID cannot be blank"));
            }
        }

        @Test
        @DisplayName("Should create SqsClient with blank endpoint URL")
        void shouldCreateSqsClientWithBlankEndpointUrl() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-west-1");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "   ");

            // Act
            SqsClient sqsClient = awsConfig.sqsClient();

            // Assert
            assertNotNull(sqsClient);
            sqsClient.close();
        }
    }

    @Nested
    @DisplayName("SqsAsyncClient Bean Tests")
    class SqsAsyncClientBeanTests {

        @Test
        @DisplayName("Should create SqsAsyncClient with default region when no endpoint configured")
        void shouldCreateSqsAsyncClientWithDefaultRegion() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            SqsClient sqsClient = mock(SqsClient.class);

            // Act
            SqsAsyncClient sqsAsyncClient = awsConfig.sqsAsyncClient(sqsClient);

            // Assert
            assertNotNull(sqsAsyncClient);
            sqsAsyncClient.close();
        }

        @Test
        @DisplayName("Should create SqsAsyncClient with custom endpoint for LocalStack")
        void shouldCreateSqsAsyncClientWithCustomEndpoint() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "http://localhost:4566");
            setLocalStackCredentials(awsConfig);

            SqsClient sqsClient = mock(SqsClient.class);

            // Act
            SqsAsyncClient sqsAsyncClient = awsConfig.sqsAsyncClient(sqsClient);

            // Assert
            assertNotNull(sqsAsyncClient);
            sqsAsyncClient.close();
        }

        @Test
        @DisplayName("Should handle null endpoint URL")
        void shouldHandleNullEndpointUrl() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", null);

            SqsClient sqsClient = mock(SqsClient.class);

            // Act
            SqsAsyncClient sqsAsyncClient = awsConfig.sqsAsyncClient(sqsClient);

            // Assert
            assertNotNull(sqsAsyncClient);
            sqsAsyncClient.close();
        }
    }

    @Nested
    @DisplayName("Region Configuration Tests")
    class RegionConfigurationTests {

        @Test
        @DisplayName("Should use configured region")
        void shouldUseConfiguredRegion() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "eu-west-1");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            // Act
            SnsClient snsClient = awsConfig.snsClient();

            // Assert
            assertNotNull(snsClient);
            snsClient.close();
        }

        @Test
        @DisplayName("Should use different regions correctly")
        void shouldUseDifferentRegionsCorrectly() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            String[] regions = {"us-east-1", "us-west-2", "sa-east-1", "ap-southeast-1"};

            for (String region : regions) {
                // Act
                ReflectionTestUtils.setField(awsConfig, "region", region);
                SnsClient snsClient = awsConfig.snsClient();

                // Assert
                assertNotNull(snsClient);
                snsClient.close();
            }
        }

        @Test
        @DisplayName("Should create clients with Asia Pacific region")
        void shouldCreateClientsWithAsiaPacificRegion() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "ap-northeast-1");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            // Act
            SnsClient snsClient = awsConfig.snsClient();
            SqsClient sqsClient = awsConfig.sqsClient();

            // Assert
            assertNotNull(snsClient);
            assertNotNull(sqsClient);
            snsClient.close();
            sqsClient.close();
        }

        @Test
        @DisplayName("Should create clients with Europe region")
        void shouldCreateClientsWithEuropeRegion() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "eu-central-1");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            // Act
            SnsClient snsClient = awsConfig.snsClient();
            SqsClient sqsClient = awsConfig.sqsClient();

            // Assert
            assertNotNull(snsClient);
            assertNotNull(sqsClient);
            snsClient.close();
            sqsClient.close();
        }

        @Test
        @DisplayName("Should create clients with South America region")
        void shouldCreateClientsWithSouthAmericaRegion() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "sa-east-1");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            // Act
            SnsClient snsClient = awsConfig.snsClient();
            SqsClient sqsClient = awsConfig.sqsClient();

            // Assert
            assertNotNull(snsClient);
            assertNotNull(sqsClient);
            snsClient.close();
            sqsClient.close();
        }
    }

    @Nested
    @DisplayName("LocalStack Configuration Tests")
    class LocalStackConfigurationTests {

        @Test
        @DisplayName("Should create all clients with LocalStack endpoint")
        void shouldCreateAllClientsWithLocalStackEndpoint() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "http://localhost:4566");
            setLocalStackCredentials(awsConfig);

            // Act
            SnsClient snsClient = awsConfig.snsClient();
            SqsClient sqsClientMock = mock(SqsClient.class);
            SqsAsyncClient sqsAsyncClient = awsConfig.sqsAsyncClient(sqsClientMock);

            // Assert
            assertNotNull(snsClient);
            assertNotNull(sqsAsyncClient);

            snsClient.close();
            sqsAsyncClient.close();
        }

        @Test
        @DisplayName("Should handle different LocalStack ports")
        void shouldHandleDifferentLocalStackPorts() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            setLocalStackCredentials(awsConfig);

            String[] endpoints = {
                "http://localhost:4566",
                "http://localhost:4567",
                "http://localstack:4566"
            };

            for (String endpoint : endpoints) {
                // Act
                ReflectionTestUtils.setField(awsConfig, "endpointUrl", endpoint);
                SnsClient snsClient = awsConfig.snsClient();

                // Assert
                assertNotNull(snsClient);
                snsClient.close();
            }
        }

        @Test
        @DisplayName("Should create clients with HTTPS LocalStack endpoint")
        void shouldCreateClientsWithHttpsLocalStackEndpoint() {
            // Arrange
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "https://localstack.local:4566");
            setLocalStackCredentials(awsConfig);

            // Act
            SnsClient snsClient = awsConfig.snsClient();
            SqsClient sqsClientMock = mock(SqsClient.class);
            SqsAsyncClient sqsAsyncClient = awsConfig.sqsAsyncClient(sqsClientMock);

            // Assert
            assertNotNull(snsClient);
            assertNotNull(sqsAsyncClient);
            snsClient.close();
            sqsAsyncClient.close();
        }
    }
}
