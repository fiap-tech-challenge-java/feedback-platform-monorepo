package br.com.postech.feedback.core.dto;

import br.com.postech.feedback.core.domain.StatusFeedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeedbackEventDTO Tests")
class FeedbackEventDTOTest {

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
        @DisplayName("Should create FeedbackEventDTO with all fields")
        void shouldCreateFeedbackEventDTOWithAllFields() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);

            // Act
            FeedbackEventDTO dto = new FeedbackEventDTO(
                    1L,
                    "Test description",
                    5,
                    StatusFeedback.NORMAL,
                    now
            );

            // Assert
            assertEquals(1L, dto.id());
            assertEquals("Test description", dto.description());
            assertEquals(5, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
            assertEquals(now, dto.createdAt());
        }

        @Test
        @DisplayName("Should allow null values")
        void shouldAllowNullValues() {
            // Act
            FeedbackEventDTO dto = new FeedbackEventDTO(null, null, null, null, null);

            // Assert
            assertNull(dto.id());
            assertNull(dto.description());
            assertNull(dto.rating());
            assertNull(dto.status());
            assertNull(dto.createdAt());
        }

        @Test
        @DisplayName("Should create DTO with CRITICAL status")
        void shouldCreateDTOWithCriticalStatus() {
            // Act
            FeedbackEventDTO dto = new FeedbackEventDTO(
                    2L,
                    "Bad experience",
                    2,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            // Assert
            assertEquals(StatusFeedback.CRITICAL, dto.status());
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
            FeedbackEventDTO dto1 = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, now);
            FeedbackEventDTO dto2 = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, now);

            // Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            FeedbackEventDTO dto1 = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, now);
            FeedbackEventDTO dto2 = new FeedbackEventDTO(2L, "Test", 5, StatusFeedback.NORMAL, now);

            // Assert
            assertNotEquals(dto1, dto2);
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        @Test
        @DisplayName("Should serialize to JSON")
        void shouldSerializeToJson() throws Exception {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            FeedbackEventDTO dto = new FeedbackEventDTO(
                    1L,
                    "Test description",
                    5,
                    StatusFeedback.NORMAL,
                    now
            );

            // Act
            String json = objectMapper.writeValueAsString(dto);

            // Assert
            assertTrue(json.contains("\"id\":1"));
            assertTrue(json.contains("\"description\":\"Test description\""));
            assertTrue(json.contains("\"rating\":5"));
            assertTrue(json.contains("\"status\":\"NORMAL\""));
        }

        @Test
        @DisplayName("Should deserialize from JSON")
        void shouldDeserializeFromJson() throws Exception {
            // Arrange
            String json = """
                    {
                        "id": 1,
                        "description": "Test description",
                        "rating": 5,
                        "status": "NORMAL",
                        "createdAt": "2026-02-09T10:30:00"
                    }
                    """;

            // Act
            FeedbackEventDTO dto = objectMapper.readValue(json, FeedbackEventDTO.class);

            // Assert
            assertEquals(1L, dto.id());
            assertEquals("Test description", dto.description());
            assertEquals(5, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.status());
            assertNotNull(dto.createdAt());
        }

        @Test
        @DisplayName("Should deserialize CRITICAL status from JSON")
        void shouldDeserializeCriticalStatusFromJson() throws Exception {
            // Arrange
            String json = """
                    {
                        "id": 2,
                        "description": "Bad product",
                        "rating": 2,
                        "status": "CRITICAL",
                        "createdAt": "2026-02-09T10:30:00"
                    }
                    """;

            // Act
            FeedbackEventDTO dto = objectMapper.readValue(json, FeedbackEventDTO.class);

            // Assert
            assertEquals(StatusFeedback.CRITICAL, dto.status());
        }
    }

    @Nested
    @DisplayName("Record Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Record should be immutable")
        void recordShouldBeImmutable() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, now);

            // Act & Assert - Record fields are final by design
            // Attempting to modify would result in compilation error
            assertEquals(1L, dto.id());
            assertEquals("Test", dto.description());
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, now);

            // Act
            String toString = dto.toString();

            // Assert
            assertTrue(toString.contains("FeedbackEventDTO"));
            assertTrue(toString.contains("1"));
            assertTrue(toString.contains("Test"));
            assertTrue(toString.contains("5"));
            assertTrue(toString.contains("NORMAL"));
        }
    }

    @Nested
    @DisplayName("Field Validation Tests")
    class FieldValidationTests {

        @Test
        @DisplayName("Should accept all rating values 0-10")
        void shouldAcceptAllRatingValues() {
            for (int rating = 0; rating <= 10; rating++) {
                FeedbackEventDTO dto = new FeedbackEventDTO(
                        1L,
                        "Test",
                        rating,
                        StatusFeedback.NORMAL,
                        LocalDateTime.now()
                );
                assertEquals(rating, dto.rating());
            }
        }

        @Test
        @DisplayName("Should accept empty description")
        void shouldAcceptEmptyDescription() {
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "", 5, StatusFeedback.NORMAL, LocalDateTime.now());
            assertEquals("", dto.description());
        }

        @Test
        @DisplayName("Should accept very long description")
        void shouldAcceptVeryLongDescription() {
            String longDesc = "a".repeat(10000);
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, longDesc, 5, StatusFeedback.NORMAL, LocalDateTime.now());
            assertEquals(longDesc, dto.description());
        }

        @Test
        @DisplayName("Should accept description with special characters")
        void shouldAcceptDescriptionWithSpecialCharacters() {
            String special = "Test @#$%^&*()_+-=[]{}|;':\",./<>?~`";
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, special, 5, StatusFeedback.NORMAL, LocalDateTime.now());
            assertEquals(special, dto.description());
        }

        @Test
        @DisplayName("Should accept description with unicode")
        void shouldAcceptDescriptionWithUnicode() {
            String unicode = "Feedback 😀😃 àáâã";
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, unicode, 5, StatusFeedback.NORMAL, LocalDateTime.now());
            assertEquals(unicode, dto.description());
        }
    }

    @Nested
    @DisplayName("JSON Edge Cases Tests")
    class JsonEdgeCasesTests {

        @Test
        @DisplayName("Should serialize with null fields")
        void shouldSerializeWithNullFields() throws Exception {
            FeedbackEventDTO dto = new FeedbackEventDTO(null, null, null, null, null);
            String json = objectMapper.writeValueAsString(dto);

            assertTrue(json.contains("\"id\":null"));
            assertTrue(json.contains("\"description\":null"));
        }

        @Test
        @DisplayName("Should deserialize with missing optional fields")
        void shouldDeserializeWithMissingOptionalFields() throws Exception {
            String json = """
                    {
                        "id": 1,
                        "description": "Test"
                    }
                    """;

            FeedbackEventDTO dto = objectMapper.readValue(json, FeedbackEventDTO.class);

            assertEquals(1L, dto.id());
            assertEquals("Test", dto.description());
            assertNull(dto.rating());
            assertNull(dto.status());
            assertNull(dto.createdAt());
        }

        @Test
        @DisplayName("Should serialize and deserialize with all nulls")
        void shouldSerializeAndDeserializeWithAllNulls() throws Exception {
            FeedbackEventDTO original = new FeedbackEventDTO(null, null, null, null, null);
            String json = objectMapper.writeValueAsString(original);
            FeedbackEventDTO deserialized = objectMapper.readValue(json, FeedbackEventDTO.class);

            assertEquals(original, deserialized);
        }

        @Test
        @DisplayName("Should reject JSON with extra fields")
        void shouldRejectJsonWithExtraFields() throws Exception {
            String json = """
                    {
                        "id": 1,
                        "description": "Test",
                        "rating": 5,
                        "status": "NORMAL",
                        "createdAt": "2026-02-09T10:30:00",
                        "extraField": "should cause error"
                    }
                    """;

            // By default, Jackson throws exception for unrecognized properties
            assertThrows(Exception.class, () ->
                objectMapper.readValue(json, FeedbackEventDTO.class)
            );
        }

        @Test
        @DisplayName("Should serialize rating 0 correctly")
        void shouldSerializeRating0Correctly() throws Exception {
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 0, StatusFeedback.CRITICAL, LocalDateTime.now());
            String json = objectMapper.writeValueAsString(dto);

            assertTrue(json.contains("\"rating\":0"));
        }

        @Test
        @DisplayName("Should serialize rating 10 correctly")
        void shouldSerializeRating10Correctly() throws Exception {
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 10, StatusFeedback.NORMAL, LocalDateTime.now());
            String json = objectMapper.writeValueAsString(dto);

            assertTrue(json.contains("\"rating\":10"));
        }
    }

    @Nested
    @DisplayName("Timestamp JSON Tests")
    class TimestampJsonTests {

        @Test
        @DisplayName("Should serialize timestamp with ISO format")
        void shouldSerializeTimestampWithIsoFormat() throws Exception {
            LocalDateTime timestamp = LocalDateTime.of(2026, 2, 10, 15, 30, 45);
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, timestamp);

            String json = objectMapper.writeValueAsString(dto);

            assertTrue(json.contains("2026-02-10"));
            assertTrue(json.contains("15:30:45"));
        }

        @Test
        @DisplayName("Should deserialize timestamp from ISO format")
        void shouldDeserializeTimestampFromIsoFormat() throws Exception {
            String json = """
                    {
                        "id": 1,
                        "description": "Test",
                        "rating": 5,
                        "status": "NORMAL",
                        "createdAt": "2026-02-10T15:30:45"
                    }
                    """;

            FeedbackEventDTO dto = objectMapper.readValue(json, FeedbackEventDTO.class);

            assertEquals(2026, dto.createdAt().getYear());
            assertEquals(2, dto.createdAt().getMonthValue());
            assertEquals(10, dto.createdAt().getDayOfMonth());
            assertEquals(15, dto.createdAt().getHour());
            assertEquals(30, dto.createdAt().getMinute());
            assertEquals(45, dto.createdAt().getSecond());
        }

        @Test
        @DisplayName("Should handle null timestamp in JSON")
        void shouldHandleNullTimestampInJson() throws Exception {
            String json = """
                    {
                        "id": 1,
                        "description": "Test",
                        "rating": 5,
                        "status": "NORMAL",
                        "createdAt": null
                    }
                    """;

            FeedbackEventDTO dto = objectMapper.readValue(json, FeedbackEventDTO.class);
            assertNull(dto.createdAt());
        }
    }

    @Nested
    @DisplayName("Status Enum JSON Tests")
    class StatusEnumJsonTests {

        @Test
        @DisplayName("Should serialize CRITICAL status")
        void shouldSerializeCriticalStatus() throws Exception {
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 2, StatusFeedback.CRITICAL, LocalDateTime.now());
            String json = objectMapper.writeValueAsString(dto);

            assertTrue(json.contains("\"status\":\"CRITICAL\""));
        }

        @Test
        @DisplayName("Should serialize NORMAL status")
        void shouldSerializeNormalStatus() throws Exception {
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 8, StatusFeedback.NORMAL, LocalDateTime.now());
            String json = objectMapper.writeValueAsString(dto);

            assertTrue(json.contains("\"status\":\"NORMAL\""));
        }

        @Test
        @DisplayName("Should throw exception for invalid status case")
        void shouldThrowExceptionForInvalidStatusCase() throws Exception {
            String json = """
                    {
                        "id": 1,
                        "description": "Test",
                        "rating": 5,
                        "status": "normal",
                        "createdAt": "2026-02-10T15:30:45"
                    }
                    """;

            // Jackson is case-sensitive by default for enums
            assertThrows(Exception.class, () ->
                objectMapper.readValue(json, FeedbackEventDTO.class)
            );
        }
    }

    @Nested
    @DisplayName("Complete Round-Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve all data in round-trip serialization")
        void shouldPreserveAllDataInRoundTrip() throws Exception {
            LocalDateTime timestamp = LocalDateTime.of(2026, 2, 10, 15, 30, 45, 123456789);
            FeedbackEventDTO original = new FeedbackEventDTO(
                    123L,
                    "Complex test with special chars: @#$%",
                    7,
                    StatusFeedback.NORMAL,
                    timestamp
            );

            String json = objectMapper.writeValueAsString(original);
            FeedbackEventDTO deserialized = objectMapper.readValue(json, FeedbackEventDTO.class);

            assertEquals(original.id(), deserialized.id());
            assertEquals(original.description(), deserialized.description());
            assertEquals(original.rating(), deserialized.rating());
            assertEquals(original.status(), deserialized.status());
            // Note: nanosecond precision might be lost in JSON
            assertEquals(original.createdAt().withNano(0), deserialized.createdAt().withNano(0));
        }

        @Test
        @DisplayName("Should handle multiple round-trips")
        void shouldHandleMultipleRoundTrips() throws Exception {
            FeedbackEventDTO original = new FeedbackEventDTO(
                    1L,
                    "Test",
                    5,
                    StatusFeedback.NORMAL,
                    LocalDateTime.of(2026, 2, 10, 10, 0)
            );

            // First round-trip
            String json1 = objectMapper.writeValueAsString(original);
            FeedbackEventDTO dto1 = objectMapper.readValue(json1, FeedbackEventDTO.class);

            // Second round-trip
            String json2 = objectMapper.writeValueAsString(dto1);
            FeedbackEventDTO dto2 = objectMapper.readValue(json2, FeedbackEventDTO.class);

            // Third round-trip
            String json3 = objectMapper.writeValueAsString(dto2);
            FeedbackEventDTO dto3 = objectMapper.readValue(json3, FeedbackEventDTO.class);

            assertEquals(original.id(), dto3.id());
            assertEquals(original.description(), dto3.description());
            assertEquals(original.rating(), dto3.rating());
        }
    }

    @Nested
    @DisplayName("Equality Edge Cases Tests")
    class EqualityEdgeCasesTests {

        @Test
        @DisplayName("Should not equal null")
        void shouldNotEqualNull() {
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, LocalDateTime.now());
            assertNotEquals(dto, null);
        }

        @Test
        @DisplayName("Should equal itself")
        void shouldEqualItself() {
            FeedbackEventDTO dto = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, LocalDateTime.now());
            assertEquals(dto, dto);
        }

        @Test
        @DisplayName("Should be equal with all null fields")
        void shouldBeEqualWithAllNullFields() {
            FeedbackEventDTO dto1 = new FeedbackEventDTO(null, null, null, null, null);
            FeedbackEventDTO dto2 = new FeedbackEventDTO(null, null, null, null, null);
            assertEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal with different IDs")
        void shouldNotBeEqualWithDifferentIds() {
            LocalDateTime now = LocalDateTime.now();
            FeedbackEventDTO dto1 = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, now);
            FeedbackEventDTO dto2 = new FeedbackEventDTO(2L, "Test", 5, StatusFeedback.NORMAL, now);
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal with different ratings")
        void shouldNotBeEqualWithDifferentRatings() {
            LocalDateTime now = LocalDateTime.now();
            FeedbackEventDTO dto1 = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, now);
            FeedbackEventDTO dto2 = new FeedbackEventDTO(1L, "Test", 6, StatusFeedback.NORMAL, now);
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal with different statuses")
        void shouldNotBeEqualWithDifferentStatuses() {
            LocalDateTime now = LocalDateTime.now();
            FeedbackEventDTO dto1 = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.NORMAL, now);
            FeedbackEventDTO dto2 = new FeedbackEventDTO(1L, "Test", 5, StatusFeedback.CRITICAL, now);
            assertNotEquals(dto1, dto2);
        }
    }
}
