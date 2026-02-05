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

    private Counter emailsSentCounter;
    private Counter emailsFailedCounter;
    private Counter messagesReceivedCounter;
    private Counter messagesProcessedCounter;
    private Counter messagesRejectedCounter;

    private Timer processingTimer;

    /**
     * Inicializa as métricas customizadas
     */
    @PostConstruct
    public void init() {
        emailsSentCounter = Counter.builder("notification.emails.sent")
                .description("Total de e-mails enviados com sucesso")
                .tag("service", "notification")
                .register(meterRegistry);

        emailsFailedCounter = Counter.builder("notification.emails.failed")
                .description("Total de e-mails que falharam no envio")
                .tag("service", "notification")
                .register(meterRegistry);

        messagesReceivedCounter = Counter.builder("notification.messages.received")
                .description("Total de mensagens SNS recebidas")
                .tag("service", "notification")
                .register(meterRegistry);

        messagesProcessedCounter = Counter.builder("notification.messages.processed")
                .description("Total de mensagens processadas com sucesso")
                .tag("service", "notification")
                .register(meterRegistry);

        messagesRejectedCounter = Counter.builder("notification.messages.rejected")
                .description("Total de mensagens rejeitadas por validação")
                .tag("service", "notification")
                .register(meterRegistry);

        processingTimer = Timer.builder("notification.processing.time")
                .description("Tempo de processamento de notificações")
                .tag("service", "notification")
                .register(meterRegistry);

        log.info("Métricas customizadas inicializadas com sucesso");
    }

    public void incrementEmailsSent() {
        emailsSentCounter.increment();
    }

    public void incrementEmailsFailed() {
        emailsFailedCounter.increment();
    }

    public void incrementMessagesReceived() {
        messagesReceivedCounter.increment();
    }

    public void incrementMessagesProcessed() {
        messagesProcessedCounter.increment();
    }

    public void incrementMessagesRejected() {
        messagesRejectedCounter.increment();
    }

    public void recordProcessingTime(Runnable task) {
        processingTimer.record(task);
    }

    public Timer getProcessingTimer() {
        return processingTimer;
    }
}
