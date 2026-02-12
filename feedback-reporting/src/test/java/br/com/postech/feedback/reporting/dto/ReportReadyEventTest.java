package br.com.postech.feedback.reporting.dto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("ReportReadyEvent Tests")
class ReportReadyEventTest {
    private ObjectMapper objectMapper;
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        @Test
        @DisplayName("Should create ReportReadyEvent with builder")
        void shouldCreateReportReadyEventWithBuilder() {
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            ReportReadyEvent event = ReportReadyEvent.builder()
                    .eventType("ReportReady")
                    .message("Weekly report available")
                    .reportLink("https://s3.amazonaws.com/bucket/report.pdf")
                    .bucketName("postech-feedback-reports")
                    .s3Key("reports/2026/02/report.csv")
                    .totalFeedbacks(150L)
                    .averageScore(4.5)
                    .generatedAt(now)
                    .build();
            assertEquals("ReportReady", event.getEventType());
            assertEquals("Weekly report available", event.getMessage());
            assertEquals("https://s3.amazonaws.com/bucket/report.pdf", event.getReportLink());
            assertEquals("postech-feedback-reports", event.getBucketName());
            assertEquals("reports/2026/02/report.csv", event.getS3Key());
            assertEquals(150L, event.getTotalFeedbacks());
            assertEquals(4.5, event.getAverageScore());
            assertEquals(now, event.getGeneratedAt());
        }
    }
    @Nested
    @DisplayName("No-Args Constructor Tests")
    class NoArgsConstructorTests {
        @Test
        @DisplayName("Should create empty ReportReadyEvent")
        void shouldCreateEmptyReportReadyEvent() {
            ReportReadyEvent event = new ReportReadyEvent();
            assertNotNull(event);
            assertNull(event.getEventType());
            assertNull(event.getMessage());
        }
    }
    @Nested
    @DisplayName("All-Args Constructor Tests")
    class AllArgsConstructorTests {
        @Test
        @DisplayName("Should create ReportReadyEvent with all args")
        void shouldCreateReportReadyEventWithAllArgs() {
            LocalDateTime now = LocalDateTime.now();
            ReportReadyEvent event = new ReportReadyEvent(
                    "ReportReady",
                    "Report ready",
                    "https://link.com",
                    "bucket",
                    "key",
                    100L,
                    4.0,
                    now
            );
            assertEquals("ReportReady", event.getEventType());
            assertEquals("Report ready", event.getMessage());
            assertEquals(100L, event.getTotalFeedbacks());
        }
    }
    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {
        @Test
        @DisplayName("Should serialize to JSON")
        void shouldSerializeToJson() throws Exception {
            ReportReadyEvent event = ReportReadyEvent.builder()
                    .eventType("ReportReady")
                    .message("Test")
                    .reportLink("https://link.com")
                    .totalFeedbacks(50L)
                    .averageScore(3.5)
                    .build();
            String json = objectMapper.writeValueAsString(event);
            assertTrue(json.contains("ReportReady"));
            assertTrue(json.contains("https://link.com"));
            assertTrue(json.contains("50"));
        }
    }
    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {
        @Test
        @DisplayName("Should set and get all properties")
        void shouldSetAndGetAllProperties() {
            ReportReadyEvent event = new ReportReadyEvent();
            LocalDateTime now = LocalDateTime.now();
            event.setEventType("ReportReady");
            event.setMessage("Test message");
            event.setReportLink("https://test.com");
            event.setBucketName("test-bucket");
            event.setS3Key("test/key");
            event.setTotalFeedbacks(200L);
            event.setAverageScore(4.8);
            event.setGeneratedAt(now);
            assertEquals("ReportReady", event.getEventType());
            assertEquals("Test message", event.getMessage());
            assertEquals("https://test.com", event.getReportLink());
            assertEquals("test-bucket", event.getBucketName());
            assertEquals("test/key", event.getS3Key());
            assertEquals(200L, event.getTotalFeedbacks());
            assertEquals(4.8, event.getAverageScore());
            assertEquals(now, event.getGeneratedAt());
        }
    }
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {
        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 0, 0);
            ReportReadyEvent event1 = ReportReadyEvent.builder()
                    .eventType("ReportReady")
                    .message("Test")
                    .generatedAt(now)
                    .build();
            ReportReadyEvent event2 = ReportReadyEvent.builder()
                    .eventType("ReportReady")
                    .message("Test")
                    .generatedAt(now)
                    .build();
            assertEquals(event1, event2);
            assertEquals(event1.hashCode(), event2.hashCode());
        }
    }
}
