package br.com.postech.feedback.core.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StatusFeedback Enum Tests")
class StatusFeedbackTest {

    @Nested
    @DisplayName("Basic Enum Tests")
    class BasicEnumTests {

        @Test
        @DisplayName("Should have CRITICAL status")
        void shouldHaveCriticalStatus() {
            // Assert
            assertNotNull(StatusFeedback.CRITICAL);
            assertEquals("CRITICAL", StatusFeedback.CRITICAL.name());
        }

        @Test
        @DisplayName("Should have NORMAL status")
        void shouldHaveNormalStatus() {
            // Assert
            assertNotNull(StatusFeedback.NORMAL);
            assertEquals("NORMAL", StatusFeedback.NORMAL.name());
        }

        @Test
        @DisplayName("Should have exactly 2 values")
        void shouldHaveExactly2Values() {
            // Assert
            assertEquals(2, StatusFeedback.values().length);
        }

        @Test
        @DisplayName("Should convert from string using valueOf")
        void shouldConvertFromStringUsingValueOf() {
            // Assert
            assertEquals(StatusFeedback.CRITICAL, StatusFeedback.valueOf("CRITICAL"));
            assertEquals(StatusFeedback.NORMAL, StatusFeedback.valueOf("NORMAL"));
        }

        @Test
        @DisplayName("Should throw exception for invalid value")
        void shouldThrowExceptionForInvalidValue() {
            // Assert
            assertThrows(IllegalArgumentException.class, () -> StatusFeedback.valueOf("INVALID"));
        }

        @Test
        @DisplayName("Should have correct ordinal values")
        void shouldHaveCorrectOrdinalValues() {
            // Assert
            assertEquals(0, StatusFeedback.CRITICAL.ordinal());
            assertEquals(1, StatusFeedback.NORMAL.ordinal());
        }
    }

    @Nested
    @DisplayName("Comparison Tests")
    class ComparisonTests {

        @Test
        @DisplayName("Should compare using compareTo")
        void shouldCompareUsingCompareTo() {
            // CRITICAL (ordinal 0) should come before NORMAL (ordinal 1)
            assertTrue(StatusFeedback.CRITICAL.compareTo(StatusFeedback.NORMAL) < 0);
            assertTrue(StatusFeedback.NORMAL.compareTo(StatusFeedback.CRITICAL) > 0);
            assertEquals(0, StatusFeedback.CRITICAL.compareTo(StatusFeedback.CRITICAL));
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            assertEquals(StatusFeedback.CRITICAL, StatusFeedback.CRITICAL);
            assertEquals(StatusFeedback.NORMAL, StatusFeedback.NORMAL);
        }

        @Test
        @DisplayName("Should not be equal to different status")
        void shouldNotBeEqualToDifferentStatus() {
            assertNotEquals(StatusFeedback.CRITICAL, StatusFeedback.NORMAL);
            assertNotEquals(StatusFeedback.NORMAL, StatusFeedback.CRITICAL);
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            assertEquals(StatusFeedback.CRITICAL.hashCode(), StatusFeedback.CRITICAL.hashCode());
            assertEquals(StatusFeedback.NORMAL.hashCode(), StatusFeedback.NORMAL.hashCode());
        }
    }

    @Nested
    @DisplayName("Collection and Stream Tests")
    class CollectionTests {

        @Test
        @DisplayName("Should work with List")
        void shouldWorkWithList() {
            List<StatusFeedback> statuses = Arrays.asList(StatusFeedback.values());

            assertEquals(2, statuses.size());
            assertTrue(statuses.contains(StatusFeedback.CRITICAL));
            assertTrue(statuses.contains(StatusFeedback.NORMAL));
        }

        @Test
        @DisplayName("Should work with streams")
        void shouldWorkWithStreams() {
            long criticalCount = Arrays.stream(StatusFeedback.values())
                    .filter(s -> s == StatusFeedback.CRITICAL)
                    .count();

            assertEquals(1, criticalCount);
        }

        @Test
        @DisplayName("Should filter by name")
        void shouldFilterByName() {
            StatusFeedback result = Arrays.stream(StatusFeedback.values())
                    .filter(s -> s.name().equals("CRITICAL"))
                    .findFirst()
                    .orElse(null);

            assertEquals(StatusFeedback.CRITICAL, result);
        }
    }

    @Nested
    @DisplayName("Switch Statement Tests")
    class SwitchStatementTests {

        @Test
        @DisplayName("Should work in switch statement with CRITICAL")
        void shouldWorkInSwitchWithCritical() {
            String result = switch (StatusFeedback.CRITICAL) {
                case CRITICAL -> "Critical";
                case NORMAL -> "Normal";
            };

            assertEquals("Critical", result);
        }

        @Test
        @DisplayName("Should work in switch statement with NORMAL")
        void shouldWorkInSwitchWithNormal() {
            String result = switch (StatusFeedback.NORMAL) {
                case CRITICAL -> "Critical";
                case NORMAL -> "Normal";
            };

            assertEquals("Normal", result);
        }

        @Test
        @DisplayName("Should handle all cases in switch")
        void shouldHandleAllCasesInSwitch() {
            for (StatusFeedback status : StatusFeedback.values()) {
                String result = switch (status) {
                    case CRITICAL -> "Alert";
                    case NORMAL -> "OK";
                };
                assertNotNull(result);
            }
        }
    }

    @Nested
    @DisplayName("JSON Serialization Tests")
    class JsonSerializationTests {

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        @DisplayName("Should serialize CRITICAL to JSON")
        void shouldSerializeCriticalToJson() throws Exception {
            String json = objectMapper.writeValueAsString(StatusFeedback.CRITICAL);
            assertEquals("\"CRITICAL\"", json);
        }

        @Test
        @DisplayName("Should serialize NORMAL to JSON")
        void shouldSerializeNormalToJson() throws Exception {
            String json = objectMapper.writeValueAsString(StatusFeedback.NORMAL);
            assertEquals("\"NORMAL\"", json);
        }

        @Test
        @DisplayName("Should deserialize CRITICAL from JSON")
        void shouldDeserializeCriticalFromJson() throws Exception {
            StatusFeedback status = objectMapper.readValue("\"CRITICAL\"", StatusFeedback.class);
            assertEquals(StatusFeedback.CRITICAL, status);
        }

        @Test
        @DisplayName("Should deserialize NORMAL from JSON")
        void shouldDeserializeNormalFromJson() throws Exception {
            StatusFeedback status = objectMapper.readValue("\"NORMAL\"", StatusFeedback.class);
            assertEquals(StatusFeedback.NORMAL, status);
        }

        @Test
        @DisplayName("Should throw exception for invalid JSON value")
        void shouldThrowExceptionForInvalidJsonValue() {
            assertThrows(Exception.class, () ->
                objectMapper.readValue("\"INVALID_STATUS\"", StatusFeedback.class)
            );
        }
    }

    @Nested
    @DisplayName("String Representation Tests")
    class StringRepresentationTests {

        @Test
        @DisplayName("toString should return name")
        void toStringShouldReturnName() {
            assertEquals("CRITICAL", StatusFeedback.CRITICAL.toString());
            assertEquals("NORMAL", StatusFeedback.NORMAL.toString());
        }

        @Test
        @DisplayName("name() should return correct value")
        void nameShouldReturnCorrectValue() {
            assertEquals("CRITICAL", StatusFeedback.CRITICAL.name());
            assertEquals("NORMAL", StatusFeedback.NORMAL.name());
        }

        @Test
        @DisplayName("name() and toString() should be consistent")
        void nameAndToStringShouldBeConsistent() {
            for (StatusFeedback status : StatusFeedback.values()) {
                assertEquals(status.name(), status.toString());
            }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should throw NullPointerException for valueOf(null)")
        void shouldThrowNullPointerExceptionForValueOfNull() {
            assertThrows(NullPointerException.class, () -> StatusFeedback.valueOf(null));
        }

        @Test
        @DisplayName("Should be case-sensitive for valueOf")
        void shouldBeCaseSensitiveForValueOf() {
            assertThrows(IllegalArgumentException.class, () -> StatusFeedback.valueOf("critical"));
            assertThrows(IllegalArgumentException.class, () -> StatusFeedback.valueOf("normal"));
            assertThrows(IllegalArgumentException.class, () -> StatusFeedback.valueOf("Critical"));
        }

        @Test
        @DisplayName("Should throw for empty string valueOf")
        void shouldThrowForEmptyStringValueOf() {
            assertThrows(IllegalArgumentException.class, () -> StatusFeedback.valueOf(""));
        }

        @Test
        @DisplayName("Should throw for whitespace valueOf")
        void shouldThrowForWhitespaceValueOf() {
            assertThrows(IllegalArgumentException.class, () -> StatusFeedback.valueOf("   "));
        }
    }

    @Nested
    @DisplayName("Array Operations Tests")
    class ArrayOperationsTests {

        @Test
        @DisplayName("values() should return new array each time")
        void valuesShouldReturnNewArrayEachTime() {
            StatusFeedback[] array1 = StatusFeedback.values();
            StatusFeedback[] array2 = StatusFeedback.values();

            assertNotSame(array1, array2);
            assertArrayEquals(array1, array2);
        }

        @Test
        @DisplayName("Modifying values() array should not affect enum")
        void modifyingValuesArrayShouldNotAffectEnum() {
            StatusFeedback[] array = StatusFeedback.values();
            array[0] = null;

            // Original enum should be unaffected
            assertNotNull(StatusFeedback.CRITICAL);
            assertEquals(2, StatusFeedback.values().length);
        }

        @Test
        @DisplayName("Should contain both values in correct order")
        void shouldContainBothValuesInCorrectOrder() {
            StatusFeedback[] values = StatusFeedback.values();

            assertEquals(StatusFeedback.CRITICAL, values[0]);
            assertEquals(StatusFeedback.NORMAL, values[1]);
        }
    }

    @Nested
    @DisplayName("Enum Class Tests")
    class EnumClassTests {

        @Test
        @DisplayName("Should return correct declaring class")
        void shouldReturnCorrectDeclaringClass() {
            assertEquals(StatusFeedback.class, StatusFeedback.CRITICAL.getDeclaringClass());
            assertEquals(StatusFeedback.class, StatusFeedback.NORMAL.getDeclaringClass());
        }

        @Test
        @DisplayName("Should be instance of Enum")
        void shouldBeInstanceOfEnum() {
            assertTrue(StatusFeedback.CRITICAL instanceof Enum);
            assertTrue(StatusFeedback.NORMAL instanceof Enum);
        }

        @Test
        @DisplayName("Should be of type StatusFeedback")
        void shouldBeOfTypeStatusFeedback() {
            assertTrue(StatusFeedback.CRITICAL instanceof StatusFeedback);
            assertTrue(StatusFeedback.NORMAL instanceof StatusFeedback);
        }
    }
}

