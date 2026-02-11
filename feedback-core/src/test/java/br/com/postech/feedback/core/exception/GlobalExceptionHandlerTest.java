package br.com.postech.feedback.core.exception;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler exceptionHandler;
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }
    @Nested
    @DisplayName("handleArgumentException Tests")
    class HandleArgumentExceptionTests {
        @Test
        @DisplayName("Should return BAD_REQUEST status for IllegalArgumentException")
        void shouldReturnBadRequestStatusForIllegalArgumentException() {
            IllegalArgumentException exception = new IllegalArgumentException("Invalid input");
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
        @Test
        @DisplayName("Should include error message in response body")
        @SuppressWarnings("unchecked")
        void shouldIncludeErrorMessageInResponseBody() {
            IllegalArgumentException exception = new IllegalArgumentException("Rating deve estar entre 0 e 10");
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Rating deve estar entre 0 e 10", body.get("message"));
        }
        @Test
        @DisplayName("Should include status code in response body")
        @SuppressWarnings("unchecked")
        void shouldIncludeStatusCodeInResponseBody() {
            IllegalArgumentException exception = new IllegalArgumentException("Test");
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(400, body.get("status"));
        }
    }
    @Nested
    @DisplayName("handleGeneralException Tests")
    class HandleGeneralExceptionTests {
        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR status for general Exception")
        void shouldReturnInternalServerErrorStatusForGeneralException() {
            Exception exception = new Exception("Something went wrong");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
        @Test
        @DisplayName("Should include prefixed error message in response body")
        @SuppressWarnings("unchecked")
        void shouldIncludePrefixedErrorMessageInResponseBody() {
            Exception exception = new Exception("Database connection failed");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Erro interno no servidor: Database connection failed", body.get("message"));
        }
        @Test
        @DisplayName("Should include status code 500 in response body")
        @SuppressWarnings("unchecked")
        void shouldIncludeStatusCode500InResponseBody() {
            Exception exception = new Exception("Test");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(500, body.get("status"));
        }
    }
    @Nested
    @DisplayName("Response Structure Tests")
    class ResponseStructureTests {
        @Test
        @DisplayName("Response body should contain required fields")
        @SuppressWarnings("unchecked")
        void responseBodyShouldContainRequiredFields() {
            Exception exception = new Exception("Test");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertTrue(body.containsKey("timestamp"));
            assertTrue(body.containsKey("status"));
            assertTrue(body.containsKey("error"));
            assertTrue(body.containsKey("message"));
        }

        @Test
        @DisplayName("Response body should have correct error field for BAD_REQUEST")
        @SuppressWarnings("unchecked")
        void responseBodyShouldHaveCorrectErrorFieldForBadRequest() {
            IllegalArgumentException exception = new IllegalArgumentException("Test");
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Bad Request", body.get("error"));
        }

        @Test
        @DisplayName("Response body should have correct error field for INTERNAL_SERVER_ERROR")
        @SuppressWarnings("unchecked")
        void responseBodyShouldHaveCorrectErrorFieldForInternalServerError() {
            Exception exception = new Exception("Test");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Internal Server Error", body.get("error"));
        }

        @Test
        @DisplayName("Timestamp should not be null")
        @SuppressWarnings("unchecked")
        void timestampShouldNotBeNull() {
            Exception exception = new Exception("Test");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body.get("timestamp"));
        }

        @Test
        @DisplayName("Response body should not be null")
        void responseBodyShouldNotBeNull() {
            Exception exception = new Exception("Test");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            assertNotNull(response.getBody());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty error message")
        @SuppressWarnings("unchecked")
        void shouldHandleEmptyErrorMessage() {
            IllegalArgumentException exception = new IllegalArgumentException("");
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("", body.get("message"));
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle null error message")
        @SuppressWarnings("unchecked")
        void shouldHandleNullErrorMessage() {
            IllegalArgumentException exception = new IllegalArgumentException((String) null);
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNull(body.get("message"));
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle very long error message")
        @SuppressWarnings("unchecked")
        void shouldHandleVeryLongErrorMessage() {
            String longMessage = "a".repeat(1000);
            IllegalArgumentException exception = new IllegalArgumentException(longMessage);
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(longMessage, body.get("message"));
        }

        @Test
        @DisplayName("Should handle error message with special characters")
        @SuppressWarnings("unchecked")
        void shouldHandleErrorMessageWithSpecialCharacters() {
            String specialMessage = "Error with special chars: @#$%^&*(){}[]|\\<>?/~`";
            IllegalArgumentException exception = new IllegalArgumentException(specialMessage);
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(specialMessage, body.get("message"));
        }

        @Test
        @DisplayName("Should handle error message with unicode characters")
        @SuppressWarnings("unchecked")
        void shouldHandleErrorMessageWithUnicodeCharacters() {
            String unicodeMessage = "Erro com acentuação: àáâãäå e émojis: 😀😃";
            Exception exception = new Exception(unicodeMessage);
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertTrue(((String) body.get("message")).contains(unicodeMessage));
        }
    }

    @Nested
    @DisplayName("Different Exception Types Tests")
    class DifferentExceptionTypesTests {

        @Test
        @DisplayName("Should handle RuntimeException as general exception")
        void shouldHandleRuntimeExceptionAsGeneralException() {
            RuntimeException exception = new RuntimeException("Runtime error");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle NullPointerException as general exception")
        @SuppressWarnings("unchecked")
        void shouldHandleNullPointerExceptionAsGeneralException() {
            NullPointerException exception = new NullPointerException("Null pointer");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertTrue(((String) body.get("message")).contains("Null pointer"));
        }

        @Test
        @DisplayName("Should handle NumberFormatException as general exception")
        void shouldHandleNumberFormatExceptionAsGeneralException() {
            NumberFormatException exception = new NumberFormatException("Invalid number");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }

        @Test
        @DisplayName("Should handle ArithmeticException as general exception")
        void shouldHandleArithmeticExceptionAsGeneralException() {
            ArithmeticException exception = new ArithmeticException("Division by zero");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("Message Format Tests")
    class MessageFormatTests {

        @Test
        @DisplayName("General exception message should be prefixed correctly")
        @SuppressWarnings("unchecked")
        void generalExceptionMessageShouldBePrefixedCorrectly() {
            Exception exception = new Exception("Original message");
            ResponseEntity<Object> response = exceptionHandler.handleGeneralException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            String message = (String) body.get("message");
            assertTrue(message.startsWith("Erro interno no servidor: "));
            assertTrue(message.endsWith("Original message"));
        }

        @Test
        @DisplayName("IllegalArgumentException message should not be prefixed")
        @SuppressWarnings("unchecked")
        void illegalArgumentExceptionMessageShouldNotBePrefixed() {
            IllegalArgumentException exception = new IllegalArgumentException("Direct message");
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals("Direct message", body.get("message"));
        }

        @Test
        @DisplayName("Should preserve line breaks in error message")
        @SuppressWarnings("unchecked")
        void shouldPreserveLineBreaksInErrorMessage() {
            String multilineMessage = "Line 1\nLine 2\nLine 3";
            IllegalArgumentException exception = new IllegalArgumentException(multilineMessage);
            ResponseEntity<Object> response = exceptionHandler.handleArgumentException(exception);
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertEquals(multilineMessage, body.get("message"));
        }
    }
}
