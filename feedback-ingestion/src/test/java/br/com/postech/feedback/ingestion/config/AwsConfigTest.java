package br.com.postech.feedback.ingestion.config;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("AwsConfig Tests")
class AwsConfigTest {
    @Nested
    @DisplayName("SqsClient Bean Tests")
    class SqsClientBeanTests {
        @Test
        @DisplayName("Should create SqsClient with default region when no endpoint")
        void shouldCreateSqsClientWithDefaultRegion() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpoint", "");
            ReflectionTestUtils.setField(awsConfig, "accessKey", "");
            ReflectionTestUtils.setField(awsConfig, "secretKey", "");
            SqsClient sqsClient = awsConfig.sqsClient();
            assertNotNull(sqsClient);
            sqsClient.close();
        }
        @Test
        @DisplayName("Should create SqsClient with custom endpoint for LocalStack")
        void shouldCreateSqsClientWithCustomEndpoint() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(awsConfig, "accessKey", "test");
            ReflectionTestUtils.setField(awsConfig, "secretKey", "test");
            SqsClient sqsClient = awsConfig.sqsClient();
            assertNotNull(sqsClient);
            sqsClient.close();
        }
        @Test
        @DisplayName("Should handle null endpoint")
        void shouldHandleNullEndpoint() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpoint", null);
            ReflectionTestUtils.setField(awsConfig, "accessKey", "");
            ReflectionTestUtils.setField(awsConfig, "secretKey", "");
            SqsClient sqsClient = awsConfig.sqsClient();
            assertNotNull(sqsClient);
            sqsClient.close();
        }
    }
    @Nested
    @DisplayName("Region Configuration Tests")
    class RegionConfigurationTests {
        @Test
        @DisplayName("Should use configured region")
        void shouldUseConfiguredRegion() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "eu-west-1");
            ReflectionTestUtils.setField(awsConfig, "endpoint", "");
            ReflectionTestUtils.setField(awsConfig, "accessKey", "");
            ReflectionTestUtils.setField(awsConfig, "secretKey", "");
            SqsClient sqsClient = awsConfig.sqsClient();
            assertNotNull(sqsClient);
            sqsClient.close();
        }
    }
}
