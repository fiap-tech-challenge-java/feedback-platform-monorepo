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

        // Criar MeterRegistry e metrics
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        metrics = new NotificationMetrics(meterRegistry);
        metrics.init();

        // Criar validator
        validator = Validation.buildDefaultValidatorFactory().getValidator();

        service = new FeedbackNotificationService(sesClient, objectMapper, templateEngine, metrics, validator);

        // Configurar propriedades
        ReflectionTestUtils.setField(service, "senderEmail", "noreply@test.com");
        ReflectionTestUtils.setField(service, "recipientEmail", "admin@test.com");
        ReflectionTestUtils.setField(service, "sesEnabled", true);
    }

    @Test
    void testProcessNotification_ShouldSendEmail() throws Exception {
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
    void testProcessNotification_WithSesDisabled_ShouldNotSendEmail() throws Exception {
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

    @Test
    void testProcessNotification_WithInvalidMessage_ShouldReturnError() {
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
    void testSendEmail_ShouldIncludeCorrectRecipient() throws Exception {
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
        NotificationResponseDTO response = function.apply(snsMessage);

        // Assert
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        verify(sesClient).sendEmail(requestCaptor.capture());
        SendEmailRequest capturedRequest = requestCaptor.getValue();

        assertEquals("noreply@test.com", capturedRequest.source());
        assertTrue(capturedRequest.destination().toAddresses().contains("admin@test.com"));
        assertNotNull(capturedRequest.message());
    }
}
