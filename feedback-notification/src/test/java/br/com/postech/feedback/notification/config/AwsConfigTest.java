package br.com.postech.feedback.notification.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.ses.SesClient;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AwsConfig Tests")
class AwsConfigTest {

    private void setLocalStackCredentials(AwsConfig awsConfig) {
        ReflectionTestUtils.setField(awsConfig, "accessKey", "test-access-key");
        ReflectionTestUtils.setField(awsConfig, "secretKey", "test-secret-key");
    }

    @Nested
    @DisplayName("SesClient Bean Tests")
    class SesClientBeanTests {

        @Test
        @DisplayName("Should create SesClient with default region when no endpoint")
        void shouldCreateSesClientWithDefaultRegion() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            SesClient sesClient = awsConfig.sesClient();

            assertNotNull(sesClient);
            sesClient.close();
        }

        @Test
        @DisplayName("Should create SesClient with custom endpoint for LocalStack")
        void shouldCreateSesClientWithCustomEndpoint() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "http://localhost:4566");
            setLocalStackCredentials(awsConfig);

            SesClient sesClient = awsConfig.sesClient();

            assertNotNull(sesClient);
            sesClient.close();
        }

        @Test
        @DisplayName("Should handle null endpoint")
        void shouldHandleNullEndpoint() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", null);

            SesClient sesClient = awsConfig.sesClient();

            assertNotNull(sesClient);
            sesClient.close();
        }

        @Test
        @DisplayName("Should handle blank endpoint")
        void shouldHandleBlankEndpoint() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "   ");

            SesClient sesClient = awsConfig.sesClient();

            assertNotNull(sesClient);
            sesClient.close();
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
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

            SesClient sesClient = awsConfig.sesClient();

            assertNotNull(sesClient);
            sesClient.close();
        }

        @Test
        @DisplayName("Should use different regions correctly")
        void shouldUseDifferentRegionsCorrectly() {
            String[] regions = {"us-east-1", "us-west-2", "sa-east-1"};

            for (String region : regions) {
                AwsConfig awsConfig = new AwsConfig();
                ReflectionTestUtils.setField(awsConfig, "region", region);
                ReflectionTestUtils.setField(awsConfig, "endpointUrl", "");

                SesClient sesClient = awsConfig.sesClient();

                assertNotNull(sesClient);
                sesClient.close();
            }
        }
    }

    @Nested
    @DisplayName("LocalStack Configuration Tests")
    class LocalStackConfigurationTests {

        @Test
        @DisplayName("Should create SesClient with LocalStack endpoint and credentials")
        void shouldCreateSesClientWithLocalStackEndpointAndCredentials() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "http://localhost:4566");
            setLocalStackCredentials(awsConfig);

            SesClient sesClient = awsConfig.sesClient();

            assertNotNull(sesClient);
            sesClient.close();
        }

        @Test
        @DisplayName("Should handle different LocalStack ports")
        void shouldHandleDifferentLocalStackPorts() {
            String[] endpoints = {
                "http://localhost:4566",
                "http://localhost:4567",
                "http://localstack:4566"
            };

            for (String endpoint : endpoints) {
                AwsConfig awsConfig = new AwsConfig();
                ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
                ReflectionTestUtils.setField(awsConfig, "endpointUrl", endpoint);
                setLocalStackCredentials(awsConfig);

                SesClient sesClient = awsConfig.sesClient();

                assertNotNull(sesClient);
                sesClient.close();
            }
        }

        @Test
        @DisplayName("Should create SesClient with HTTPS LocalStack endpoint")
        void shouldCreateSesClientWithHttpsLocalStackEndpoint() {
            AwsConfig awsConfig = new AwsConfig();
            ReflectionTestUtils.setField(awsConfig, "region", "us-east-2");
            ReflectionTestUtils.setField(awsConfig, "endpointUrl", "https://localstack.local:4566");
            setLocalStackCredentials(awsConfig);

            SesClient sesClient = awsConfig.sesClient();

            assertNotNull(sesClient);
            sesClient.close();
        }
    }
}
