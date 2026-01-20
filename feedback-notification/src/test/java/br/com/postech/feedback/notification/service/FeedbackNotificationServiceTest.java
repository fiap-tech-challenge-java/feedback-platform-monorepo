package br.com.postech.feedback.notification.service;

import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.function.Consumer;

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
    private FeedbackNotificationService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        service = new FeedbackNotificationService(sesClient, objectMapper, templateEngine);

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
                "Produto com defeito",
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
        Consumer<String> function = service.processNotification();
        function.accept(snsMessage);

        // Assert
        verify(sesClient, times(1)).sendEmail(any(SendEmailRequest.class));
        verify(templateEngine, times(1)).process(eq("critical-feedback-email"), any(Context.class));
    }

    @Test
    void testProcessNotification_WithSesDisabled_ShouldNotSendEmail() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(service, "sesEnabled", false);

        FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                1L,
                "Produto com defeito",
                3,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        String messageBody = objectMapper.writeValueAsString(feedbackEvent);
        String snsMessage = String.format("{\"Message\": \"%s\"}",
                messageBody.replace("\"", "\\\""));

        // Act
        Consumer<String> function = service.processNotification();
        function.accept(snsMessage);

        // Assert
        verify(sesClient, never()).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void testProcessNotification_WithInvalidMessage_ShouldThrowException() {
        // Arrange
        String invalidSnsMessage = "{\"Message\": \"invalid json}";

        // Act & Assert
        Consumer<String> function = service.processNotification();
        assertThrows(RuntimeException.class, () -> function.accept(invalidSnsMessage));
    }

    @Test
    void testSendEmail_ShouldIncludeCorrectRecipient() throws Exception {
        // Arrange
        FeedbackEventDTO feedbackEvent = new FeedbackEventDTO(
                1L,
                "Produto com defeito",
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
        Consumer<String> function = service.processNotification();
        function.accept(snsMessage);

        // Assert
        verify(sesClient).sendEmail(requestCaptor.capture());
        SendEmailRequest capturedRequest = requestCaptor.getValue();

        assertEquals("noreply@test.com", capturedRequest.source());
        assertTrue(capturedRequest.destination().toAddresses().contains("admin@test.com"));
        assertNotNull(capturedRequest.message());
    }
}
