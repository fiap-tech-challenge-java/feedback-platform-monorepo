package br.com.postech.feedback.reporting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("S3UploadService Tests")
class S3UploadServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private PresignedGetObjectRequest presignedGetObjectRequest;

    private S3UploadService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new S3UploadService(s3Client, s3Presigner);
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(service, "region", "us-east-2");
        ReflectionTestUtils.setField(service, "presignedUrlExpirationDays", 7);

        // Setup default mock for presigner (lenient because not all tests use these)
        lenient().when(presignedGetObjectRequest.url()).thenReturn(new URL("https://test-bucket.s3.amazonaws.com/test-key"));
        lenient().when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGetObjectRequest);
    }

    @Nested
    @DisplayName("uploadReport() - Byte Array Tests")
    class UploadReportBytesTests {

        @Test
        @DisplayName("Should upload report bytes to S3 successfully")
        void shouldUploadReportBytesToS3Successfully() {
            // Arrange
            byte[] content = "CSV content".getBytes(StandardCharsets.UTF_8);
            String s3Key = "reports/2026/01/report.csv";
            String contentType = "text/csv";

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder()
                            .eTag("\"abc123\"")
                            .build());

            // Act
            String result = service.uploadReport(content, s3Key, contentType);

            // Assert
            assertNotNull(result);
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should use correct bucket and key in request")
        void shouldUseCorrectBucketAndKeyInRequest() {
            // Arrange
            byte[] content = "Test content".getBytes();
            String s3Key = "reports/test-report.csv";
            String contentType = "text/csv";

            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"test\"").build());

            // Act
            service.uploadReport(content, s3Key, contentType);

            // Assert
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
            assertEquals("test-bucket", requestCaptor.getValue().bucket());
            assertEquals("reports/test-report.csv", requestCaptor.getValue().key());
            assertEquals("text/csv", requestCaptor.getValue().contentType());
        }
    }

    @Nested
    @DisplayName("uploadReport() - String Content Tests")
    class UploadReportStringTests {

        @Test
        @DisplayName("Should upload string content to S3")
        void shouldUploadStringContentToS3() {
            // Arrange
            String content = "CSV string content";
            String s3Key = "reports/string-report.csv";
            String contentType = "text/csv";

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"test\"").build());

            // Act
            String result = service.uploadReport(content, s3Key, contentType);

            // Assert
            assertNotNull(result);
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }
    }

    @Nested
    @DisplayName("Bucket Configuration Validation Tests")
    class BucketConfigurationTests {

        @Test
        @DisplayName("Should throw exception when bucket name is null")
        void shouldThrowExceptionWhenBucketNameIsNull() {
            // Arrange
            ReflectionTestUtils.setField(service, "bucketName", null);
            byte[] content = "Test".getBytes();

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> service.uploadReport(content, "key", "text/csv"));
        }

        @Test
        @DisplayName("Should throw exception when bucket name is blank")
        void shouldThrowExceptionWhenBucketNameIsBlank() {
            // Arrange
            ReflectionTestUtils.setField(service, "bucketName", "   ");
            byte[] content = "Test".getBytes();

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> service.uploadReport(content, "key", "text/csv"));
        }

        @Test
        @DisplayName("Should throw exception when bucket name is empty")
        void shouldThrowExceptionWhenBucketNameIsEmpty() {
            // Arrange
            ReflectionTestUtils.setField(service, "bucketName", "");
            byte[] content = "Test".getBytes();

            // Act & Assert
            assertThrows(IllegalStateException.class,
                    () -> service.uploadReport(content, "key", "text/csv"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw RuntimeException when S3 upload fails")
        void shouldThrowRuntimeExceptionWhenS3UploadFails() {
            // Arrange
            byte[] content = "Test".getBytes();

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new RuntimeException("S3 connection failed"));

            // Act & Assert
            assertThrows(RuntimeException.class,
                    () -> service.uploadReport(content, "key", "text/csv"));
        }
    }
}
