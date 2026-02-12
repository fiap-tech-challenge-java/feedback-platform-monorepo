package br.com.postech.feedback.notification.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationResponseDTO Tests")
class NotificationResponseDTOTest {

    @Nested
    @DisplayName("success() Factory Method Tests")
    class SuccessFactoryMethodTests {

        @Test
        @DisplayName("Should create success response with all fields")
        void shouldCreateSuccessResponseWithAllFields() {
            // Act
            NotificationResponseDTO response = NotificationResponseDTO.success(1L, "CRITICAL", true);

            // Assert
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Notificação processada com sucesso", response.getMessage());
            assertEquals(1L, response.getFeedbackId());
            assertEquals("CRITICAL", response.getPriority());
            assertTrue(response.getEmailSent());
            assertNotNull(response.getProcessedAt());
            assertNull(response.getError());
        }

        @Test
        @DisplayName("Should create success response with NORMAL priority")
        void shouldCreateSuccessResponseWithNormalPriority() {
            // Act
            NotificationResponseDTO response = NotificationResponseDTO.success(2L, "NORMAL", true);

            // Assert
            assertEquals("NORMAL", response.getPriority());
        }

        @Test
        @DisplayName("Should create success response with emailSent false")
        void shouldCreateSuccessResponseWithEmailSentFalse() {
            // Act
            NotificationResponseDTO response = NotificationResponseDTO.success(3L, "CRITICAL", false);

            // Assert
            assertFalse(response.getEmailSent());
        }
    }

    @Nested
    @DisplayName("error() Factory Method Tests")
    class ErrorFactoryMethodTests {

        @Test
        @DisplayName("Should create error response with error message")
        void shouldCreateErrorResponseWithErrorMessage() {
            // Act
            NotificationResponseDTO response = NotificationResponseDTO.error("Connection failed");

            // Assert
            assertEquals("ERROR", response.getStatus());
            assertEquals("Erro ao processar notificação", response.getMessage());
            assertEquals("Connection failed", response.getError());
            assertNotNull(response.getProcessedAt());
            assertNull(response.getFeedbackId());
            assertNull(response.getPriority());
            assertNull(response.getEmailSent());
        }

        @Test
        @DisplayName("Should create error response with detailed error message")
        void shouldCreateErrorResponseWithDetailedErrorMessage() {
            // Act
            NotificationResponseDTO response = NotificationResponseDTO.error("SES: Email address not verified");

            // Assert
            assertEquals("SES: Email address not verified", response.getError());
        }
    }

    @Nested
    @DisplayName("rejected() Factory Method Tests")
    class RejectedFactoryMethodTests {

        @Test
        @DisplayName("Should create rejected response with reason")
        void shouldCreateRejectedResponseWithReason() {
            // Act
            NotificationResponseDTO response = NotificationResponseDTO.rejected("Payload vazio ou nulo");

            // Assert
            assertEquals("REJECTED", response.getStatus());
            assertEquals("Notificação rejeitada", response.getMessage());
            assertEquals("Payload vazio ou nulo", response.getError());
            assertNotNull(response.getProcessedAt());
        }

        @Test
        @DisplayName("Should create rejected response for invalid JSON")
        void shouldCreateRejectedResponseForInvalidJson() {
            // Act
            NotificationResponseDTO response = NotificationResponseDTO.rejected("Payload não é JSON válido");

            // Assert
            assertEquals("REJECTED", response.getStatus());
            assertEquals("Payload não é JSON válido", response.getError());
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create DTO using builder")
        void shouldCreateDtoUsingBuilder() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            NotificationResponseDTO response = NotificationResponseDTO.builder()
                    .status("SUCCESS")
                    .message("Custom message")
                    .feedbackId(100L)
                    .priority("CRITICAL")
                    .emailSent(true)
                    .processedAt(now)
                    .error(null)
                    .build();

            // Assert
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Custom message", response.getMessage());
            assertEquals(100L, response.getFeedbackId());
            assertEquals("CRITICAL", response.getPriority());
            assertTrue(response.getEmailSent());
            assertEquals(now, response.getProcessedAt());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get all properties")
        void shouldSetAndGetAllProperties() {
            // Arrange
            NotificationResponseDTO response = new NotificationResponseDTO();
            LocalDateTime now = LocalDateTime.now();

            // Act
            response.setStatus("SUCCESS");
            response.setMessage("Test message");
            response.setFeedbackId(1L);
            response.setPriority("CRITICAL");
            response.setEmailSent(true);
            response.setProcessedAt(now);
            response.setError("Test error");

            // Assert
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Test message", response.getMessage());
            assertEquals(1L, response.getFeedbackId());
            assertEquals("CRITICAL", response.getPriority());
            assertTrue(response.getEmailSent());
            assertEquals(now, response.getProcessedAt());
            assertEquals("Test error", response.getError());
        }
    }

    @Nested
    @DisplayName("No-Args Constructor Tests")
    class NoArgsConstructorTests {

        @Test
        @DisplayName("Should create empty DTO with no-args constructor")
        void shouldCreateEmptyDtoWithNoArgsConstructor() {
            // Act
            NotificationResponseDTO response = new NotificationResponseDTO();

            // Assert
            assertNotNull(response);
            assertNull(response.getStatus());
            assertNull(response.getMessage());
            assertNull(response.getFeedbackId());
        }
    }

    @Nested
    @DisplayName("All-Args Constructor Tests")
    class AllArgsConstructorTests {

        @Test
        @DisplayName("Should create DTO with all-args constructor")
        void shouldCreateDtoWithAllArgsConstructor() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            NotificationResponseDTO response = new NotificationResponseDTO(
                    "SUCCESS",
                    "Message",
                    1L,
                    "CRITICAL",
                    now,
                    true,
                    null
            );

            // Assert
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("Message", response.getMessage());
            assertEquals(1L, response.getFeedbackId());
            assertEquals("CRITICAL", response.getPriority());
            assertEquals(now, response.getProcessedAt());
            assertTrue(response.getEmailSent());
        }
    }
}
