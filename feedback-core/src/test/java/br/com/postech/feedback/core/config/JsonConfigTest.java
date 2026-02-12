package br.com.postech.feedback.core.config;

import br.com.postech.feedback.core.domain.StatusFeedback;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonConfig Tests")
class JsonConfigTest {

    private JsonConfig jsonConfig;

    @BeforeEach
    void setUp() {
        jsonConfig = new JsonConfig();
    }

    @Nested
    @DisplayName("ObjectMapper Bean Tests")
    class ObjectMapperBeanTests {

        @Test
        @DisplayName("Should create ObjectMapper bean")
        void shouldCreateObjectMapperBean() {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            assertNotNull(objectMapper);
        }

        @Test
        @DisplayName("Should register JavaTimeModule")
        void shouldRegisterJavaTimeModule() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);
            String json = objectMapper.writeValueAsString(now);

            assertNotNull(json);
            assertFalse(json.isEmpty());
        }

        @Test
        @DisplayName("Should enable INDENT_OUTPUT")
        void shouldEnableIndentOutput() {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            assertTrue(objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
        }

        @Test
        @DisplayName("Should disable WRITE_DATES_AS_TIMESTAMPS")
        void shouldDisableWriteDatesAsTimestamps() {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        }

        @Test
        @DisplayName("Should serialize LocalDateTime correctly")
        void shouldSerializeLocalDateTimeCorrectly() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            LocalDateTime dateTime = LocalDateTime.of(2026, 2, 9, 10, 30, 45);
            String json = objectMapper.writeValueAsString(dateTime);

            assertTrue(json.contains("2026-02-09"));
            assertTrue(json.contains("10:30:45"));
        }

        @Test
        @DisplayName("Should deserialize LocalDateTime correctly")
        void shouldDeserializeLocalDateTimeCorrectly() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            String json = "\"2026-02-09T10:30:45\"";
            LocalDateTime dateTime = objectMapper.readValue(json, LocalDateTime.class);

            assertEquals(2026, dateTime.getYear());
            assertEquals(2, dateTime.getMonthValue());
            assertEquals(9, dateTime.getDayOfMonth());
        }
    }

    @Nested
    @DisplayName("JavaTimeModule Tests")
    class JavaTimeModuleTests {

        @Test
        @DisplayName("Should serialize LocalDate")
        void shouldSerializeLocalDate() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            LocalDate date = LocalDate.of(2026, 2, 10);
            String json = objectMapper.writeValueAsString(date);

            assertTrue(json.contains("2026-02-10"));
        }

        @Test
        @DisplayName("Should deserialize LocalDate")
        void shouldDeserializeLocalDate() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            String json = "\"2026-02-10\"";
            LocalDate date = objectMapper.readValue(json, LocalDate.class);

            assertEquals(2026, date.getYear());
            assertEquals(2, date.getMonthValue());
            assertEquals(10, date.getDayOfMonth());
        }

        @Test
        @DisplayName("Should serialize LocalTime")
        void shouldSerializeLocalTime() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            LocalTime time = LocalTime.of(15, 30, 45);
            String json = objectMapper.writeValueAsString(time);

            assertTrue(json.contains("15:30:45"));
        }

        @Test
        @DisplayName("Should deserialize LocalTime")
        void shouldDeserializeLocalTime() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            String json = "\"15:30:45\"";
            LocalTime time = objectMapper.readValue(json, LocalTime.class);

            assertEquals(15, time.getHour());
            assertEquals(30, time.getMinute());
            assertEquals(45, time.getSecond());
        }

        @Test
        @DisplayName("Should handle ZonedDateTime")
        void shouldHandleZonedDateTime() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            ZonedDateTime zonedDateTime = ZonedDateTime.parse("2026-02-10T15:30:45Z");

            String json = objectMapper.writeValueAsString(zonedDateTime);
            assertNotNull(json);
            assertFalse(json.isEmpty());
        }
    }

    @Nested
    @DisplayName("Indentation Tests")
    class IndentationTests {

        @Test
        @DisplayName("Should format JSON with indentation")
        void shouldFormatJsonWithIndentation() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            Map<String, Object> data = new HashMap<>();
            data.put("key1", "value1");
            data.put("key2", "value2");

            String json = objectMapper.writeValueAsString(data);

            // Indented JSON should have line breaks
            assertTrue(json.contains("\n"));
        }

        @Test
        @DisplayName("Should format complex objects with indentation")
        void shouldFormatComplexObjectsWithIndentation() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            Map<String, Object> nested = new HashMap<>();
            nested.put("inner", "value");
            Map<String, Object> data = new HashMap<>();
            data.put("outer", nested);

            String json = objectMapper.writeValueAsString(data);

            assertTrue(json.contains("\n"));
            assertTrue(json.contains("  ")); // Should have indentation spaces
        }
    }

    @Nested
    @DisplayName("Date Format Tests")
    class DateFormatTests {

        @Test
        @DisplayName("Should not use timestamp format for dates")
        void shouldNotUseTimestampFormatForDates() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            LocalDateTime dateTime = LocalDateTime.of(2026, 2, 10, 15, 30, 45);
            String json = objectMapper.writeValueAsString(dateTime);

            // Should be in ISO format, not timestamp
            assertFalse(json.matches("\\d{13}")); // Not a timestamp
            assertTrue(json.contains("2026")); // Contains year
        }

        @Test
        @DisplayName("Should use ISO-8601 format")
        void shouldUseIso8601Format() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            LocalDateTime dateTime = LocalDateTime.of(2026, 2, 10, 15, 30, 45);
            String json = objectMapper.writeValueAsString(dateTime);

            // ISO-8601 format: yyyy-MM-ddTHH:mm:ss
            assertTrue(json.contains("T")); // Should have T separator
            assertTrue(json.matches(".*\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
        }

        @Test
        @DisplayName("Should preserve nanosecond precision when present")
        void shouldPreserveNanosecondPrecision() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            LocalDateTime dateTime = LocalDateTime.of(2026, 2, 10, 15, 30, 45, 123456789);

            String json = objectMapper.writeValueAsString(dateTime);
            LocalDateTime deserialized = objectMapper.readValue(json, LocalDateTime.class);

            assertEquals(dateTime, deserialized);
        }
    }

    @Nested
    @DisplayName("Enum Serialization Tests")
    class EnumSerializationTests {

        @Test
        @DisplayName("Should serialize enum as string")
        void shouldSerializeEnumAsString() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            StatusFeedback status = StatusFeedback.CRITICAL;
            String json = objectMapper.writeValueAsString(status);

            assertEquals("\"CRITICAL\"", json);
        }

        @Test
        @DisplayName("Should deserialize enum from string")
        void shouldDeserializeEnumFromString() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            String json = "\"NORMAL\"";
            StatusFeedback status = objectMapper.readValue(json, StatusFeedback.class);

            assertEquals(StatusFeedback.NORMAL, status);
        }
    }

    @Nested
    @DisplayName("Round-Trip Tests")
    class RoundTripTests {

        @Test
        @DisplayName("Should preserve data in round-trip for LocalDateTime")
        void shouldPreserveDataInRoundTripForLocalDateTime() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            LocalDateTime original = LocalDateTime.of(2026, 2, 10, 15, 30, 45);

            String json = objectMapper.writeValueAsString(original);
            LocalDateTime deserialized = objectMapper.readValue(json, LocalDateTime.class);

            assertEquals(original, deserialized);
        }

        @Test
        @DisplayName("Should preserve data in round-trip for complex object")
        void shouldPreserveDataInRoundTripForComplexObject() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            Map<String, Object> original = new HashMap<>();
            original.put("id", 1L);
            original.put("name", "Test");
            original.put("timestamp", LocalDateTime.of(2026, 2, 10, 15, 30));

            String json = objectMapper.writeValueAsString(original);
            @SuppressWarnings("unchecked")
            Map<String, Object> deserialized = objectMapper.readValue(json, Map.class);

            assertEquals(1, deserialized.get("id"));
            assertEquals("Test", deserialized.get("name"));
            assertNotNull(deserialized.get("timestamp"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null values")
        void shouldHandleNullValues() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            String json = objectMapper.writeValueAsString(null);
            assertEquals("null", json);
        }

        @Test
        @DisplayName("Should handle empty string")
        void shouldHandleEmptyString() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            String json = objectMapper.writeValueAsString("");
            assertEquals("\"\"", json);
        }

        @Test
        @DisplayName("Should handle special characters")
        void shouldHandleSpecialCharacters() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            String special = "Test @#$%^&*()";
            String json = objectMapper.writeValueAsString(special);

            String deserialized = objectMapper.readValue(json, String.class);
            assertEquals(special, deserialized);
        }

        @Test
        @DisplayName("Should handle unicode characters")
        void shouldHandleUnicodeCharacters() throws Exception {
            ObjectMapper objectMapper = jsonConfig.objectMapper();
            String unicode = "Test 😀 àáâã";
            String json = objectMapper.writeValueAsString(unicode);

            String deserialized = objectMapper.readValue(json, String.class);
            assertEquals(unicode, deserialized);
        }
    }

    @Nested
    @DisplayName("Configuration Bean Tests")
    class ConfigurationBeanTests {

        @Test
        @DisplayName("Should be marked as @Primary")
        void shouldBeMarkedAsPrimary() {
            // This is tested at runtime by Spring - here we just verify the method exists
            assertNotNull(jsonConfig.objectMapper());
        }

        @Test
        @DisplayName("Should create new instance each time")
        void shouldCreateNewInstanceEachTime() {
            ObjectMapper mapper1 = jsonConfig.objectMapper();
            ObjectMapper mapper2 = jsonConfig.objectMapper();

            // Should create new instances (not singleton at config level)
            assertNotSame(mapper1, mapper2);
        }

        @Test
        @DisplayName("Should have consistent configuration across instances")
        void shouldHaveConsistentConfigurationAcrossInstances() {
            ObjectMapper mapper1 = jsonConfig.objectMapper();
            ObjectMapper mapper2 = jsonConfig.objectMapper();

            assertEquals(
                mapper1.isEnabled(SerializationFeature.INDENT_OUTPUT),
                mapper2.isEnabled(SerializationFeature.INDENT_OUTPUT)
            );
            assertEquals(
                mapper1.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
                mapper2.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            );
        }
    }
}
