package br.com.postech.feedback.notification.service;

import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import br.com.postech.feedback.notification.dto.NotificationResponseDTO;
import br.com.postech.feedback.notification.metrics.NotificationMetrics;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

import java.time.LocalDateTime;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackNotificationService Tests")
class FeedbackNotificationServiceTest {

    @Mock
    private SesClient sesClient;

    @Mock
    private TemplateEngine templateEngine;

    private ObjectMapper objectMapper;
    private NotificationMetrics metrics;
    private Validator validator;
    private FeedbackNotificationService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        metrics = new NotificationMetrics(meterRegistry);
        metrics.init();

        validator = Validation.buildDefaultValidatorFactory().getValidator();

        service = new FeedbackNotificationService(sesClient, objectMapper, templateEngine, metrics, validator);

        ReflectionTestUtils.setField(service, "senderEmail", "noreply@test.com");
        ReflectionTestUtils.setField(service, "recipientEmail", "admin@test.com");
        ReflectionTestUtils.setField(service, "sesEnabled", true);
    }

    @Nested
    @DisplayName("processNotification() - Success Cases")
    class ProcessNotificationSuccessTests {

        @Test
        @DisplayName("Should send email for critical feedback")
        void shouldSendEmailForCriticalFeedback() throws Exception {
            // Arrange
            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    1L,
                    "Produto com defeito grave que precisa de reparo urgente",
                    3,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            String messageBody = objectMapper.writeValueAsString(feedbackEvent);
            String snsMessage = String.format("{\"Message\": \"%s\"}",
                    messageBody.replace("\"", "\\\""));

            when(templateEngine.process(eq("critical-feedback-email"), any(Context.class)))
                    .thenReturn("<html>Email HTML</html>");

            when(sesClient.sendEmail(any(SendEmailRequest.class)))
                    .thenReturn(SendEmailResponse.builder()
                            .messageId("test-message-id")
                            .build());

            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply(snsMessage);

            // Assert
            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertEquals(1L, response.getFeedbackId());
            assertTrue(response.getEmailSent());
            verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
            verify(templateEngine, times(1)).process(eq("critical-feedback-email"), any(Context.class));
        }

        @Test
        @DisplayName("Should process feedback with NORMAL status")
        void shouldProcessFeedbackWithNormalStatus() throws Exception {
            // Arrange
            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    2L,
                    "Produto bom, atendeu expectativas",
                    8,
                    StatusFeedback.NORMAL,
                    LocalDateTime.now()
            );

            String messageBody = objectMapper.writeValueAsString(feedbackEvent);
            String snsMessage = String.format("{\"Message\": \"%s\"}",
                    messageBody.replace("\"", "\\\""));

            when(templateEngine.process(eq("critical-feedback-email"), any(Context.class)))
                    .thenReturn("<html>Email HTML</html>");

            when(sesClient.sendEmail(any(SendEmailRequest.class)))
                    .thenReturn(SendEmailResponse.builder()
                            .messageId("test-message-id")
                            .build());

            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply(snsMessage);

            // Assert
            assertEquals("SUCCESS", response.getStatus());
            assertEquals("NORMAL", response.getPriority());
        }

        @Test
        @DisplayName("Should include correct recipient in email request")
        void shouldIncludeCorrectRecipientInEmailRequest() throws Exception {
            // Arrange
            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    1L,
                    "Produto com defeito muito grave que requer atenção imediata",
                    3,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            String messageBody = objectMapper.writeValueAsString(feedbackEvent);
            String snsMessage = String.format("{\"Message\": \"%s\"}",
                    messageBody.replace("\"", "\\\""));

            when(templateEngine.process(eq("critical-feedback-email"), any(Context.class)))
                    .thenReturn("<html>Email HTML</html>");

            when(sesClient.sendEmail(any(SendEmailRequest.class)))
                    .thenReturn(SendEmailResponse.builder()
                            .messageId("test-message-id")
                            .build());

            ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);

            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            function.apply(snsMessage);

            // Assert
            verify(sesClient).sendEmail(requestCaptor.capture());
            SendEmailRequest capturedRequest = requestCaptor.getValue();

            assertEquals("noreply@test.com", capturedRequest.source());
            assertTrue(capturedRequest.destination().toAddresses().contains("admin@test.com"));
            assertNotNull(capturedRequest.message());
        }
    }

    @Nested
    @DisplayName("processNotification() - SES Disabled")
    class ProcessNotificationSesDisabledTests {

        @Test
        @DisplayName("Should not send email when SES is disabled")
        void shouldNotSendEmailWhenSesIsDisabled() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(service, "sesEnabled", false);

            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    1L,
                    "Produto com defeito grave que precisa de atenção urgente",
                    3,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            String messageBody = objectMapper.writeValueAsString(feedbackEvent);
            String snsMessage = String.format("{\"Message\": \"%s\"}",
                    messageBody.replace("\"", "\\\""));

            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply(snsMessage);

            // Assert
            assertNotNull(response);
            assertEquals("SUCCESS", response.getStatus());
            assertFalse(response.getEmailSent());
            verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
        }
    }

    @Nested
    @DisplayName("processNotification() - Error Cases")
    class ProcessNotificationErrorTests {

        @Test
        @DisplayName("Should return error for invalid JSON message")
        void shouldReturnErrorForInvalidJsonMessage() {
            // Arrange
            String invalidSnsMessage = "{\"Message\": \"invalid json}";

            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply(invalidSnsMessage);

            // Assert
            assertNotNull(response);
            assertEquals("ERROR", response.getStatus());
            assertNotNull(response.getError());
        }

        @Test
        @DisplayName("Should return rejected for null payload")
        void shouldReturnRejectedForNullPayload() {
            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply(null);

            // Assert
            assertEquals("REJECTED", response.getStatus());
            assertEquals("Payload vazio ou nulo", response.getError());
        }

        @Test
        @DisplayName("Should return rejected for empty payload")
        void shouldReturnRejectedForEmptyPayload() {
            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply("");

            // Assert
            assertEquals("REJECTED", response.getStatus());
            assertEquals("Payload vazio ou nulo", response.getError());
        }

        @Test
        @DisplayName("Should return rejected for whitespace payload")
        void shouldReturnRejectedForWhitespacePayload() {
            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply("   ");

            // Assert
            assertEquals("REJECTED", response.getStatus());
        }

        @Test
        @DisplayName("Should return rejected for non-JSON payload")
        void shouldReturnRejectedForNonJsonPayload() {
            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply("plain text message");

            // Assert
            assertEquals("REJECTED", response.getStatus());
            assertEquals("Payload não é JSON válido", response.getError());
        }
    }


    @Nested
    @DisplayName("processNotification() - SNS Message Extraction")
    class SnsMessageExtractionTests {

        @Test
        @DisplayName("Should extract message from SNS wrapper")
        void shouldExtractMessageFromSnsWrapper() throws Exception {
            // Arrange
            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    10L,
                    "Test feedback for SNS wrapper",
                    4,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            String messageBody = objectMapper.writeValueAsString(feedbackEvent);
            String snsMessage = String.format("{\"Message\": \"%s\"}",
                    messageBody.replace("\"", "\\\""));

            when(templateEngine.process(eq("critical-feedback-email"), any(Context.class)))
                    .thenReturn("<html>Email</html>");

            when(sesClient.sendEmail(any(SendEmailRequest.class)))
                    .thenReturn(SendEmailResponse.builder().messageId("msg-1").build());

            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply(snsMessage);

            // Assert
            assertEquals("SUCCESS", response.getStatus());
            assertEquals(10L, response.getFeedbackId());
        }

        @Test
        @DisplayName("Should handle direct message without SNS wrapper")
        void shouldHandleDirectMessageWithoutSnsWrapper() throws Exception {
            // Arrange
            FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                    20L,
                    "Direct message without wrapper",
                    3,
                    StatusFeedback.CRITICAL,
                    LocalDateTime.now()
            );

            String directMessage = objectMapper.writeValueAsString(feedbackEvent);

            when(templateEngine.process(eq("critical-feedback-email"), any(Context.class)))
                    .thenReturn("<html>Email</html>");

            when(sesClient.sendEmail(any(SendEmailRequest.class)))
                    .thenReturn(SendEmailResponse.builder().messageId("msg-2").build());

            // Act
            Function<String, NotificationResponseDTO> function = service.processNotification();
            NotificationResponseDTO response = function.apply(directMessage);

            // Assert
            assertEquals("SUCCESS", response.getStatus());
            assertEquals(20L, response.getFeedbackId());
        }
    }
}
