package br.com.postech.feedback.reporting.dto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("FeedbackDetail Tests")
class FeedbackDetailTest {
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        @Test
        @DisplayName("Should create FeedbackDetail with builder")
        void shouldCreateFeedbackDetailWithBuilder() {
            FeedbackDetail detail = FeedbackDetail.builder()
                    .description("Great product")
                    .urgency("LOW")
                    .createdAt("2026-02-09T10:30:00Z")
                    .build();
            assertEquals("Great product", detail.getDescription());
            assertEquals("LOW", detail.getUrgency());
            assertEquals("2026-02-09T10:30:00Z", detail.getCreatedAt());
        }
        @Test
        @DisplayName("Should create FeedbackDetail with HIGH urgency")
        void shouldCreateFeedbackDetailWithHighUrgency() {
            FeedbackDetail detail = FeedbackDetail.builder()
                    .description("Critical issue")
                    .urgency("HIGH")
                    .createdAt("2026-02-09T14:00:00Z")
                    .build();
            assertEquals("HIGH", detail.getUrgency());
        }
        @Test
        @DisplayName("Should create FeedbackDetail with MEDIUM urgency")
        void shouldCreateFeedbackDetailWithMediumUrgency() {
            FeedbackDetail detail = FeedbackDetail.builder()
                    .description("Medium priority issue")
                    .urgency("MEDIUM")
                    .createdAt("2026-02-09T15:00:00Z")
                    .build();
            assertEquals("MEDIUM", detail.getUrgency());
        }
    }
    @Nested
    @DisplayName("No-Args Constructor Tests")
    class NoArgsConstructorTests {
        @Test
        @DisplayName("Should create empty FeedbackDetail")
        void shouldCreateEmptyFeedbackDetail() {
            FeedbackDetail detail = new FeedbackDetail();
            assertNotNull(detail);
            assertNull(detail.getDescription());
            assertNull(detail.getUrgency());
            assertNull(detail.getCreatedAt());
        }
    }
    @Nested
    @DisplayName("All-Args Constructor Tests")
    class AllArgsConstructorTests {
        @Test
        @DisplayName("Should create FeedbackDetail with all args")
        void shouldCreateFeedbackDetailWithAllArgs() {
            FeedbackDetail detail = new FeedbackDetail(
                    "Test description",
                    "MEDIUM",
                    "2026-02-09T10:00:00Z"
            );
            assertEquals("Test description", detail.getDescription());
            assertEquals("MEDIUM", detail.getUrgency());
            assertEquals("2026-02-09T10:00:00Z", detail.getCreatedAt());
        }
    }
    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {
        @Test
        @DisplayName("Should set and get all properties")
        void shouldSetAndGetAllProperties() {
            FeedbackDetail detail = new FeedbackDetail();
            detail.setDescription("New description");
            detail.setUrgency("HIGH");
            detail.setCreatedAt("2026-02-10T12:00:00Z");
            assertEquals("New description", detail.getDescription());
            assertEquals("HIGH", detail.getUrgency());
            assertEquals("2026-02-10T12:00:00Z", detail.getCreatedAt());
        }
    }
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {
        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            FeedbackDetail detail1 = FeedbackDetail.builder()
                    .description("Test")
                    .urgency("LOW")
                    .createdAt("2026-02-09T10:00:00Z")
                    .build();
            FeedbackDetail detail2 = FeedbackDetail.builder()
                    .description("Test")
                    .urgency("LOW")
                    .createdAt("2026-02-09T10:00:00Z")
                    .build();
            assertEquals(detail1, detail2);
            assertEquals(detail1.hashCode(), detail2.hashCode());
        }
        @Test
        @DisplayName("Should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            FeedbackDetail detail1 = FeedbackDetail.builder()
                    .description("Test1")
                    .urgency("LOW")
                    .build();
            FeedbackDetail detail2 = FeedbackDetail.builder()
                    .description("Test2")
                    .urgency("HIGH")
                    .build();
            assertNotEquals(detail1, detail2);
        }
    }
}
