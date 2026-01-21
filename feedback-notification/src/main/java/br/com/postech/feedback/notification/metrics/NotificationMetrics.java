package br.com.postech.feedback.notification.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Classe responsável por gerenciar métricas customizadas do serviço de notificação
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMetrics {

    private final MeterRegistry meterRegistry;

    // Contadores
    private Counter emailsSentCounter;
    private Counter emailsFailedCounter;
    private Counter messagesReceivedCounter;
    private Counter messagesProcessedCounter;
    private Counter messagesRejectedCounter;

    // Timer
    private Timer processingTimer;

    /**
     * Inicializa as métricas customizadas
     */
    @PostConstruct
    public void init() {
        // Contador de e-mails enviados com sucesso
        emailsSentCounter = Counter.builder("notification.emails.sent")
                .description("Total de e-mails enviados com sucesso")
                .tag("service", "notification")
                .register(meterRegistry);

        // Contador de e-mails que falharam
        emailsFailedCounter = Counter.builder("notification.emails.failed")
                .description("Total de e-mails que falharam no envio")
                .tag("service", "notification")
                .register(meterRegistry);

        // Contador de mensagens SNS recebidas
        messagesReceivedCounter = Counter.builder("notification.messages.received")
                .description("Total de mensagens SNS recebidas")
                .tag("service", "notification")
                .register(meterRegistry);

        // Contador de mensagens processadas com sucesso
        messagesProcessedCounter = Counter.builder("notification.messages.processed")
                .description("Total de mensagens processadas com sucesso")
                .tag("service", "notification")
                .register(meterRegistry);

        // Contador de mensagens rejeitadas (validação falhou)
        messagesRejectedCounter = Counter.builder("notification.messages.rejected")
                .description("Total de mensagens rejeitadas por validação")
                .tag("service", "notification")
                .register(meterRegistry);

        // Timer para medir tempo de processamento
        processingTimer = Timer.builder("notification.processing.time")
                .description("Tempo de processamento de notificações")
                .tag("service", "notification")
                .register(meterRegistry);

        log.info("Métricas customizadas inicializadas com sucesso");
    }

    /**
     * Incrementa contador de e-mails enviados
     */
    public void incrementEmailsSent() {
        emailsSentCounter.increment();
    }

    /**
     * Incrementa contador de e-mails falhados
     */
    public void incrementEmailsFailed() {
        emailsFailedCounter.increment();
    }

    /**
     * Incrementa contador de mensagens recebidas
     */
    public void incrementMessagesReceived() {
        messagesReceivedCounter.increment();
    }

    /**
     * Incrementa contador de mensagens processadas
     */
    public void incrementMessagesProcessed() {
        messagesProcessedCounter.increment();
    }

    /**
     * Incrementa contador de mensagens rejeitadas
     */
    public void incrementMessagesRejected() {
        messagesRejectedCounter.increment();
    }

    /**
     * Registra o tempo de processamento
     */
    public void recordProcessingTime(Runnable task) {
        processingTimer.record(task);
    }

    /**
     * Retorna o timer de processamento
     */
    public Timer getProcessingTimer() {
        return processingTimer;
    }
}
