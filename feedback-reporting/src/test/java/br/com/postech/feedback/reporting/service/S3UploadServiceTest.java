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

        @Test
        @DisplayName("Should throw RuntimeException with descriptive message when S3 fails")
        void shouldThrowRuntimeExceptionWithDescriptiveMessageWhenS3Fails() {
            // Arrange
            byte[] content = "Test".getBytes();

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new RuntimeException("Network timeout"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> service.uploadReport(content, "key", "text/csv"));
            assertTrue(exception.getMessage().contains("Failed to upload report to S3"));
        }
    }

    @Nested
    @DisplayName("Presigned URL Tests")
    class PresignedUrlTests {

        @Test
        @DisplayName("Should return presigned URL after successful upload")
        void shouldReturnPresignedUrlAfterSuccessfulUpload() throws Exception {
            // Arrange
            byte[] content = "Test content".getBytes();
            String expectedUrl = "https://test-bucket.s3.amazonaws.com/reports/test.csv";

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"abc\"").build());
            when(presignedGetObjectRequest.url()).thenReturn(new URL(expectedUrl));
            when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                    .thenReturn(presignedGetObjectRequest);

            // Act
            String result = service.uploadReport(content, "reports/test.csv", "text/csv");

            // Assert
            assertEquals(expectedUrl, result);
        }

        @Test
        @DisplayName("Should call presigner with correct parameters")
        void shouldCallPresignerWithCorrectParameters() {
            // Arrange
            byte[] content = "Test content".getBytes();

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"abc\"").build());

            // Act
            service.uploadReport(content, "reports/test.csv", "text/csv");

            // Assert
            verify(s3Presigner, times(1)).presignGetObject(any(GetObjectPresignRequest.class));
        }
    }

    @Nested
    @DisplayName("Content Type Tests")
    class ContentTypeTests {

        @Test
        @DisplayName("Should set correct content type for CSV")
        void shouldSetCorrectContentTypeForCsv() {
            // Arrange
            byte[] content = "CSV content".getBytes();
            String contentType = "text/csv; charset=UTF-8";

            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"test\"").build());

            // Act
            service.uploadReport(content, "report.csv", contentType);

            // Assert
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
            assertEquals("text/csv; charset=UTF-8", requestCaptor.getValue().contentType());
        }

        @Test
        @DisplayName("Should set correct content type for Excel")
        void shouldSetCorrectContentTypeForExcel() {
            // Arrange
            byte[] content = "Excel content".getBytes();
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"test\"").build());

            // Act
            service.uploadReport(content, "report.xlsx", contentType);

            // Assert
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
            assertEquals(contentType, requestCaptor.getValue().contentType());
        }
    }

    @Nested
    @DisplayName("Large Content Tests")
    class LargeContentTests {

        @Test
        @DisplayName("Should handle large byte array content")
        void shouldHandleLargeByteArrayContent() {
            // Arrange
            byte[] largeContent = new byte[1024 * 1024]; // 1MB
            java.util.Arrays.fill(largeContent, (byte) 'A');

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"large\"").build());

            // Act
            String result = service.uploadReport(largeContent, "large-report.csv", "text/csv");

            // Assert
            assertNotNull(result);
            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should handle empty byte array content")
        void shouldHandleEmptyByteArrayContent() {
            // Arrange
            byte[] emptyContent = new byte[0];

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"empty\"").build());

            // Act
            String result = service.uploadReport(emptyContent, "empty-report.csv", "text/csv");

            // Assert
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("S3 Key Tests")
    class S3KeyTests {

        @Test
        @DisplayName("Should handle S3 key with multiple path segments")
        void shouldHandleS3KeyWithMultiplePathSegments() {
            // Arrange
            byte[] content = "Test".getBytes();
            String s3Key = "reports/2026/02/11/weekly/relatorio.csv";

            ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"test\"").build());

            // Act
            service.uploadReport(content, s3Key, "text/csv");

            // Assert
            verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
            assertEquals(s3Key, requestCaptor.getValue().key());
        }

        @Test
        @DisplayName("Should handle S3 key with special characters")
        void shouldHandleS3KeyWithSpecialCharacters() {
            // Arrange
            byte[] content = "Test".getBytes();
            String s3Key = "reports/relatório-semanal_2026.csv";

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().eTag("\"test\"").build());

            // Act
            String result = service.uploadReport(content, s3Key, "text/csv");

            // Assert
            assertNotNull(result);
        }
    }
}
