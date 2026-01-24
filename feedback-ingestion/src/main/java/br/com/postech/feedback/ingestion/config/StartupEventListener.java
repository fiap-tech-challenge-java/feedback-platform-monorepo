package br.com.postech.feedback.ingestion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener para rastrear e logar o tempo de startup da aplicação.
 *
 * Útil para debugar problemas de timeout em AWS Lambda.
 * Mostra em qual fase a aplicação está demorando.
 */
@Component
public class StartupEventListener {

    private static final Logger logger = LoggerFactory.getLogger(StartupEventListener.class);
    private static final long startTime = System.currentTimeMillis();

    @EventListener
    public void onApplicationStarting(ApplicationStartingEvent event) {
        logPhase("🚀 Application Starting");
    }

    @EventListener
    public void onApplicationStarted(ApplicationStartedEvent event) {
        logPhase("✓ Application Started (Spring Context Ready)");
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        logPhase("✅ Application Ready (All Init Complete)");
        logTotalTime();
    }

    private void logPhase(String message) {
        long elapsed = System.currentTimeMillis() - startTime;
        logger.info("[{}ms] {}", elapsed, message);
    }

    private void logTotalTime() {
        long totalTime = System.currentTimeMillis() - startTime;
        logger.info("========================================");
        logger.info("⏱️  TOTAL STARTUP TIME: {}ms ({}s)", totalTime, totalTime / 1000);
        logger.info("⚠️  Lambda timeout: 15000ms (15s) - Need to be under this!");
        logger.info("========================================");
        if (totalTime > 10000) {
            logger.warn("⚠️  WARNING: Startup took more than 10 seconds! May timeout in Lambda cold start.");
        }
    }
}
