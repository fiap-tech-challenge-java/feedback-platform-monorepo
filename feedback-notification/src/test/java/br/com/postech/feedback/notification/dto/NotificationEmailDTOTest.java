package br.com.postech.feedback.notification.dto;

import br.com.postech.feedback.core.domain.StatusFeedback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationEmailDTO Tests")
class NotificationEmailDTOTest {

    @Nested
    @DisplayName("fromCriticalFeedback() Factory Method Tests")
    class FromCriticalFeedbackTests {

        @Test
        @DisplayName("Should create DTO for critical feedback")
        void shouldCreateDtoForCriticalFeedback() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            NotificationEmailDTO dto = NotificationEmailDTO.fromCriticalFeedback(
                    1L,
                    "Produto com defeito",
                    2,
                    StatusFeedback.CRITICAL,
                    now
            );

            // Assert
            assertEquals(1L, dto.feedbackId());
            assertEquals("Produto com defeito", dto.description());
            assertEquals(2, dto.rating());
            assertEquals(StatusFeedback.CRITICAL, dto.urgency());
            assertEquals(now, dto.sentDate());
            assertEquals("ALERTA: Novo Feedback Crítico Recebido", dto.subject());
            assertNull(dto.reportLink());
            assertNull(dto.totalFeedbacks());
            assertNull(dto.averageScore());
        }

        @Test
        @DisplayName("Should create DTO with NORMAL status")
        void shouldCreateDtoWithNormalStatus() {
            // Act
            NotificationEmailDTO dto = NotificationEmailDTO.fromCriticalFeedback(
                    2L,
                    "Bom produto",
                    8,
                    StatusFeedback.NORMAL,
                    LocalDateTime.now()
            );

            // Assert
            assertEquals(StatusFeedback.NORMAL, dto.urgency());
        }

        @Test
        @DisplayName("Should not be report notification")
        void shouldNotBeReportNotification() {
            // Act
            NotificationEmailDTO dto = NotificationEmailDTO.fromCriticalFeedback(
                    1L,
                    "Test",
                    3,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            // Assert
            assertFalse(dto.isReportNotification());
        }
    }

    @Nested
    @DisplayName("fromWeeklyReport() Factory Method Tests")
    class FromWeeklyReportTests {

        @Test
        @DisplayName("Should create DTO for weekly report")
        void shouldCreateDtoForWeeklyReport() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            NotificationEmailDTO dto = NotificationEmailDTO.fromWeeklyReport(
                    "https://s3.amazonaws.com/bucket/report.pdf",
                    150L,
                    4.5,
                    now
            );

            // Assert
            assertEquals("https://s3.amazonaws.com/bucket/report.pdf", dto.reportLink());
            assertEquals(150L, dto.totalFeedbacks());
            assertEquals(4.5, dto.averageScore());
            assertEquals(now, dto.sentDate());
            assertEquals("Relatório Semanal de Feedbacks Disponível", dto.subject());
            assertNull(dto.feedbackId());
            assertNull(dto.description());
            assertNull(dto.rating());
            assertNull(dto.urgency());
        }

        @Test
        @DisplayName("Should be report notification")
        void shouldBeReportNotification() {
            // Act
            NotificationEmailDTO dto = NotificationEmailDTO.fromWeeklyReport(
                    "https://s3.amazonaws.com/bucket/report.pdf",
                    100L,
                    4.0,
                    LocalDateTime.now()
            );

            // Assert
            assertTrue(dto.isReportNotification());
        }
    }

    @Nested
    @DisplayName("isReportNotification() Tests")
    class IsReportNotificationTests {

        @Test
        @DisplayName("Should return true when reportLink is not null")
        void shouldReturnTrueWhenReportLinkIsNotNull() {
            // Act
            NotificationEmailDTO dto = new NotificationEmailDTO(
                    null, null, null, null, null, null,
                    "https://link.com",
                    null, null
            );

            // Assert
            assertTrue(dto.isReportNotification());
        }

        @Test
        @DisplayName("Should return false when reportLink is null")
        void shouldReturnFalseWhenReportLinkIsNull() {
            // Act
            NotificationEmailDTO dto = new NotificationEmailDTO(
                    1L, "desc", 5, StatusFeedback.NORMAL, LocalDateTime.now(),
                    "Subject", null, null, null
            );

            // Assert
            assertFalse(dto.isReportNotification());
        }
    }

    @Nested
    @DisplayName("Record Tests")
    class RecordTests {

        @Test
        @DisplayName("Should create record with all fields")
        void shouldCreateRecordWithAllFields() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            NotificationEmailDTO dto = new NotificationEmailDTO(
                    1L,
                    "Description",
                    5,
                    StatusFeedback.NORMAL,
                    now,
                    "Subject",
                    "https://link.com",
                    100L,
                    4.5
            );

            // Assert
            assertEquals(1L, dto.feedbackId());
            assertEquals("Description", dto.description());
            assertEquals(5, dto.rating());
            assertEquals(StatusFeedback.NORMAL, dto.urgency());
            assertEquals(now, dto.sentDate());
            assertEquals("Subject", dto.subject());
            assertEquals("https://link.com", dto.reportLink());
            assertEquals(100L, dto.totalFeedbacks());
            assertEquals(4.5, dto.averageScore());
        }

        @Test
        @DisplayName("Should be equal for same values")
        void shouldBeEqualForSameValues() {
            // Arrange
            LocalDateTime now = LocalDateTime.of(2026, 2, 9, 10, 30, 0);

            NotificationEmailDTO dto1 = NotificationEmailDTO.fromCriticalFeedback(
                    1L, "Test", 3, StatusFeedback.CRITICAL, now
            );

            NotificationEmailDTO dto2 = NotificationEmailDTO.fromCriticalFeedback(
                    1L, "Test", 3, StatusFeedback.CRITICAL, now
            );

            // Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should generate meaningful toString")
        void shouldGenerateMeaningfulToString() {
            // Arrange
            NotificationEmailDTO dto = NotificationEmailDTO.fromCriticalFeedback(
                    1L, "Test description", 3, StatusFeedback.CRITICAL, LocalDateTime.now()
            );

            // Act
            String toString = dto.toString();

            // Assert
            assertTrue(toString.contains("NotificationEmailDTO"));
            assertTrue(toString.contains("1"));
            assertTrue(toString.contains("Test description"));
        }
    }
}
