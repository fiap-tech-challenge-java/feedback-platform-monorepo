package br.com.postech.feedback.notification.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportReadyEventDTO Tests")
class ReportReadyEventDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("Record Creation Tests")
    class RecordCreationTests {

        @Test
        @DisplayName("Should create DTO with all fields")
        void shouldCreateDtoWithAllFields() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);

            // Act
            ReportReadyEventDTO dto = new ReportReadyEventDTO(
                    "ReportReady",
                    "Weekly report is ready",
                    "https://s3.amazonaws.com/bucket/report.pdf",
                    "postech-feedback-reports",
                    "reports/weekly/2026-02-09.pdf",
                    150L,
                    4.5,
                    now
            );

            // Assert
            assertEquals("ReportReady", dto.eventType());
            assertEquals("Weekly report is ready", dto.message());
            assertEquals("https://s3.amazonaws.com/bucket/report.pdf", dto.reportLink());
            assertEquals("postech-feedback-reports", dto.bucketName());
            assertEquals("reports/weekly/2026-02-09.pdf", dto.s3Key());
            assertEquals(150L, dto.totalFeedbacks());
            assertEquals(4.5, dto.averageScore());
            assertEquals(now, dto.generatedAt());
        }

        @Test
        @DisplayName("Should allow null values")
        void shouldAllowNullValues() {
            // Act
            ReportReadyEventDTO dto = new ReportReadyEventDTO(
                    null, null, null, null, null, null, null, null
            );

            // Assert
            assertNull(dto.eventType());
            assertNull(dto.message());
            assertNull(dto.reportLink());
        }
    }

    @Nested
    @DisplayName("isReportReadyEvent() Tests")
    class IsReportReadyEventTests {

        @Test
        @DisplayName("Should return true when eventType is ReportReady")
        void shouldReturnTrueWhenEventTypeIsReportReady() {
            // Arrange
            ReportReadyEventDTO dto = new ReportReadyEventDTO(
                    "ReportReady",
                    "Report ready",
                    "https://link.com",
                    "bucket",
                    "key",
                    100L,
                    4.0,
                    LocalDateTime.now()
            );

            // Assert
            assertTrue(dto.isReportReadyEvent());
        }

        @Test
        @DisplayName("Should return false when eventType is not ReportReady")
        void shouldReturnFalseWhenEventTypeIsNotReportReady() {
            // Arrange
            ReportReadyEventDTO dto = new ReportReadyEventDTO(
                    "OtherEvent",
                    "Other message",
                    null, null, null, null, null, null
            );

            // Assert
            assertFalse(dto.isReportReadyEvent());
        }

        @Test
        @DisplayName("Should return false when eventType is null")
        void shouldReturnFalseWhenEventTypeIsNull() {
            // Arrange
            ReportReadyEventDTO dto = new ReportReadyEventDTO(
                    null, null, null, null, null, null, null, null
            );

            // Assert
            assertFalse(dto.isReportReadyEvent());
        }
    }

    @Nested
    @DisplayName("JSON Deserialization Tests")
    class JsonDeserializationTests {

        @Test
        @DisplayName("Should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            // Arrange
            String json = """
                    {
                        "eventType": "ReportReady",
                        "message": "Weekly report generated",
                        "reportLink": "https://s3.amazonaws.com/bucket/report.pdf",
                        "bucketName": "postech-feedback-reports",
                        "s3Key": "reports/weekly/report.pdf",
                        "totalFeedbacks": 200,
                        "averageScore": 4.2,
                        "generatedAt": "2026-02-09T10:30:00Z"
                    }
                    """;

            // Act
            ReportReadyEventDTO dto = objectMapper.readValue(json, ReportReadyEventDTO.class);

            // Assert
            assertEquals("ReportReady", dto.eventType());
            assertEquals("Weekly report generated", dto.message());
            assertEquals("https://s3.amazonaws.com/bucket/report.pdf", dto.reportLink());
            assertEquals("postech-feedback-reports", dto.bucketName());
            assertEquals(200L, dto.totalFeedbacks());
            assertEquals(4.2, dto.averageScore());
        }

        @Test
        @DisplayName("Should ignore unknown properties")
        void shouldIgnoreUnknownProperties() throws Exception {
            // Arrange
            String json = """
                    {
                        "eventType": "ReportReady",
                        "message": "Report ready",
                        "unknownField": "should be ignored",
                        "anotherUnknown": 123
                    }
                    """;

            // Act & Assert - should not throw
            ReportReadyEventDTO dto = assertDoesNotThrow(() ->
                    objectMapper.readValue(json, ReportReadyEventDTO.class)
            );

            assertEquals("ReportReady", dto.eventType());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);

            ReportReadyEventDTO dto1 = new ReportReadyEventDTO(
                    "ReportReady", "Message", "link", "bucket", "key", 100L, 4.0, now
            );

            ReportReadyEventDTO dto2 = new ReportReadyEventDTO(
                    "ReportReady", "Message", "link", "bucket", "key", 100L, 4.0, now
            );

            // Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            // Arrange
            ReportReadyEventDTO dto1 = new ReportReadyEventDTO(
                    "ReportReady", "Message1", "link1", "bucket", "key", 100L, 4.0, null
            );

            ReportReadyEventDTO dto2 = new ReportReadyEventDTO(
                    "ReportReady", "Message2", "link2", "bucket", "key", 200L, 4.5, null
            );

            // Assert
            assertNotEquals(dto1, dto2);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Arrange
            ReportReadyEventDTO dto = new ReportReadyEventDTO(
                    "ReportReady",
                    "Report ready",
                    "https://link.com",
                    "bucket-name",
                    "key/path",
                    100L,
                    4.5,
                    LocalDateTime.now()
            );

            // Act
            String toString = dto.toString();

            // Assert
            assertTrue(toString.contains("ReportReadyEventDTO"));
            assertTrue(toString.contains("ReportReady"));
            assertTrue(toString.contains("https://link.com"));
        }
    }
}
