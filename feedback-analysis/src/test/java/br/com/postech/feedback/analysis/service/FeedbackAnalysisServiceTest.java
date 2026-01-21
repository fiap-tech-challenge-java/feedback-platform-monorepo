package br.com.postech.feedback.analysis.service;

import br.com.postech.feedback.core.domain.StatusFeedback;
import br.com.postech.feedback.core.dto.FeedbackEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackAnalysisServiceTest {

    @Mock
    private SnsClient snsClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FeedbackAnalysisService service;

    private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:123456789012:feedback-critical";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "topicArn", TOPIC_ARN);
    }

    @Test
    @DisplayName("Deve enviar notificação SNS quando feedback de curso for CRITICAL")
    void deveEnviarNotificacaoParaFeedbackCritico() throws JsonProcessingException {
        // Arrange
        FeedbackEventDTO event = new FeedbackEventDTO(
                1L,
                "Professor não domina o conteúdo e as aulas são confusas. Péssimo curso!",
                1,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        String expectedJson = "{\"id\":1,\"description\":\"Professor não domina o conteúdo e as aulas são confusas. Péssimo curso!\",\"rating\":1,\"status\":\"CRITICAL\"}";
        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);
        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("msg-123").build());

        // Act
        service.analyzeFeedback().accept(event);

        // Assert
        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient, times(1)).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertEquals(TOPIC_ARN, request.topicArn());
        assertEquals("ALERTA: Novo Feedback Crítico", request.subject());
        assertEquals(expectedJson, request.message());

        verify(objectMapper, times(1)).writeValueAsString(event);
    }

    @Test
    @DisplayName("Não deve enviar notificação SNS quando feedback de curso for NORMAL")
    void naoDeveEnviarNotificacaoParaFeedbackNormal() throws Exception {
        // Arrange
        FeedbackEventDTO event = new FeedbackEventDTO(
                2L,
                "Curso muito bom! O professor explica muito bem e os materiais são excelentes.",
                5,
                StatusFeedback.NORMAL,
                LocalDateTime.now()
        );

        // Act
        service.analyzeFeedback().accept(event);

        // Assert
        verify(snsClient, never()).publish(any(PublishRequest.class));
        verify(objectMapper, never()).writeValueAsString(any());
    }

    @Test
    @DisplayName("Não deve enviar notificação quando ARN do SNS não estiver configurado")
    void naoDeveEnviarNotificacaoSemArnConfigurado() throws Exception {
        // Arrange
        ReflectionTestUtils.setField(service, "topicArn", "");

        FeedbackEventDTO event = new FeedbackEventDTO(
                3L,
                "Material desatualizado e aulas mal gravadas. Não recomendo!",
                1,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        // Act
        service.analyzeFeedback().accept(event);

        // Assert
        verify(snsClient, never()).publish(any(PublishRequest.class));
        verify(objectMapper, never()).writeValueAsString(any());
    }

    @Test
    @DisplayName("Não deve enviar notificação quando ARN do SNS for null")
    void naoDeveEnviarNotificacaoQuandoArnForNull() {
        // Arrange
        ReflectionTestUtils.setField(service, "topicArn", null);

        FeedbackEventDTO event = new FeedbackEventDTO(
                4L,
                "Plataforma sempre fora do ar. Não consegui assistir às aulas!",
                1,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        // Act
        service.analyzeFeedback().accept(event);

        // Assert
        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("Deve tratar erro ao serializar JSON sem interromper o fluxo")
    void deveTratarErroAoSerializarJson() throws JsonProcessingException {
        // Arrange
        FeedbackEventDTO event = new FeedbackEventDTO(
                5L,
                "Exercícios práticos não funcionam. Código de exemplo está errado!",
                2,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        when(objectMapper.writeValueAsString(event))
                .thenThrow(new RuntimeException("Erro ao serializar JSON"));

        // Act & Assert
        assertDoesNotThrow(() -> service.analyzeFeedback().accept(event));

        verify(objectMapper, times(1)).writeValueAsString(event);
        verify(snsClient, never()).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("Deve tratar erro ao publicar no SNS sem interromper o fluxo")
    void deveTratarErroAoPublicarNoSns() throws JsonProcessingException {
        // Arrange
        FeedbackEventDTO event = new FeedbackEventDTO(
                6L,
                "Certificado não foi emitido após conclusão do curso. Muito insatisfeito!",
                1,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        String expectedJson = "{}";
        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);
        when(snsClient.publish(any(PublishRequest.class)))
                .thenThrow(new RuntimeException("Erro na AWS SNS"));

        // Act & Assert
        assertDoesNotThrow(() -> service.analyzeFeedback().accept(event));

        verify(snsClient, times(1)).publish(any(PublishRequest.class));
    }

    @Test
    @DisplayName("Deve processar feedback crítico de curso via SqsListener (modo local)")
    void deveProcessarFeedbackViaSqsListener() throws JsonProcessingException {
        // Arrange
        FeedbackEventDTO event = new FeedbackEventDTO(
                7L,
                "Suporte técnico não responde e fiquei travado no módulo 3. Péssima experiência!",
                1,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        String expectedJson = "{\"id\":7}";
        when(objectMapper.writeValueAsString(event)).thenReturn(expectedJson);
        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().messageId("msg-456").build());

        // Act
        service.listen(event);

        // Assert
        verify(snsClient, times(1)).publish(any(PublishRequest.class));
        verify(objectMapper, times(1)).writeValueAsString(event);
    }

    @Test
    @DisplayName("SqsListener não deve enviar notificação para feedback positivo de curso")
    void sqsListenerNaoDeveEnviarParaFeedbackNormal() throws Exception {
        // Arrange
        FeedbackEventDTO event = new FeedbackEventDTO(
                8L,
                "Curso excelente! Conteúdo atualizado, professor didático. Recomendo muito!",
                5,
                StatusFeedback.NORMAL,
                LocalDateTime.now()
        );

        // Act
        service.listen(event);

        // Assert
        verify(snsClient, never()).publish(any(PublishRequest.class));
        verify(objectMapper, never()).writeValueAsString(any());
    }

    @Test
    @DisplayName("Deve construir PublishRequest corretamente para feedback crítico de curso")
    void deveConstruirPublishRequestCorretamente() throws JsonProcessingException {
        // Arrange
        FeedbackEventDTO event = new FeedbackEventDTO(
                9L,
                "Vídeos com áudio baixo e legendas incorretas. Impossível acompanhar!",
                1,
                StatusFeedback.CRITICAL,
                LocalDateTime.now()
        );

        String messageJson = "{\"id\":9,\"rating\":1}";
        when(objectMapper.writeValueAsString(event)).thenReturn(messageJson);
        when(snsClient.publish(any(PublishRequest.class)))
                .thenReturn(PublishResponse.builder().build());

        // Act
        service.analyzeFeedback().accept(event);

        // Assert
        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());

        PublishRequest request = captor.getValue();
        assertNotNull(request);
        assertEquals(TOPIC_ARN, request.topicArn());
        assertEquals("ALERTA: Novo Feedback Crítico", request.subject());
        assertEquals(messageJson, request.message());
    }

    @Test
    @DisplayName("Deve retornar Consumer válido do método analyzeFeedback")
    void deveRetornarConsumerValido() {
        // Act
        var consumer = service.analyzeFeedback();

        // Assert
        assertNotNull(consumer);

        // Verifica que o Consumer pode ser usado com feedback positivo de curso
        FeedbackEventDTO event = new FeedbackEventDTO(
                10L,
                "Curso bem estruturado, com bons exercícios práticos. Estou aprendendo muito!",
                5,
                StatusFeedback.NORMAL,
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> consumer.accept(event));
    }
}
