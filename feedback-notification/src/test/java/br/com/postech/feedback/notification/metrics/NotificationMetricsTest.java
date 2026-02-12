
package br.com.postech.feedback.notification.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationMetrics Tests")
class NotificationMetricsTest {

    private MeterRegistry meterRegistry;
    private NotificationMetrics metrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metrics = new NotificationMetrics(meterRegistry);
        metrics.init();
    }

    @Nested
    @DisplayName("init() Tests")
    class InitTests {

        @Test
        @DisplayName("Should initialize all counters")
        void shouldInitializeAllCounters() {
            // Assert - counters should exist in registry
            assertNotNull(meterRegistry.find("notification.emails.sent").counter());
            assertNotNull(meterRegistry.find("notification.emails.failed").counter());
            assertNotNull(meterRegistry.find("notification.messages.received").counter());
            assertNotNull(meterRegistry.find("notification.messages.processed").counter());
            assertNotNull(meterRegistry.find("notification.messages.rejected").counter());
        }

        @Test
        @DisplayName("Should initialize processing timer")
        void shouldInitializeProcessingTimer() {
            // Assert
            assertNotNull(meterRegistry.find("notification.processing.time").timer());
        }
    }

    @Nested
    @DisplayName("incrementEmailsSent() Tests")
    class IncrementEmailsSentTests {

        @Test
        @DisplayName("Should increment emails sent counter")
        void shouldIncrementEmailsSentCounter() {
            // Arrange
            Counter counter = meterRegistry.find("notification.emails.sent").counter();
            double initialCount = counter.count();

            // Act
            metrics.incrementEmailsSent();

            // Assert
            assertEquals(initialCount + 1, counter.count());
        }

        @Test
        @DisplayName("Should increment multiple times")
        void shouldIncrementMultipleTimes() {
            // Arrange
            Counter counter = meterRegistry.find("notification.emails.sent").counter();

            // Act
            metrics.incrementEmailsSent();
            metrics.incrementEmailsSent();
            metrics.incrementEmailsSent();

            // Assert
            assertEquals(3, counter.count());
        }
    }

    @Nested
    @DisplayName("incrementEmailsFailed() Tests")
    class IncrementEmailsFailedTests {

        @Test
        @DisplayName("Should increment emails failed counter")
        void shouldIncrementEmailsFailedCounter() {
            // Arrange
            Counter counter = meterRegistry.find("notification.emails.failed").counter();
            double initialCount = counter.count();

            // Act
            metrics.incrementEmailsFailed();

            // Assert
            assertEquals(initialCount + 1, counter.count());
        }
    }

    @Nested
    @DisplayName("incrementMessagesReceived() Tests")
    class IncrementMessagesReceivedTests {

        @Test
        @DisplayName("Should increment messages received counter")
        void shouldIncrementMessagesReceivedCounter() {
            // Arrange
            Counter counter = meterRegistry.find("notification.messages.received").counter();
            double initialCount = counter.count();

            // Act
            metrics.incrementMessagesReceived();

            // Assert
            assertEquals(initialCount + 1, counter.count());
        }
    }

    @Nested
    @DisplayName("incrementMessagesProcessed() Tests")
    class IncrementMessagesProcessedTests {

        @Test
        @DisplayName("Should increment messages processed counter")
        void shouldIncrementMessagesProcessedCounter() {
            // Arrange
            Counter counter = meterRegistry.find("notification.messages.processed").counter();
            double initialCount = counter.count();

            // Act
            metrics.incrementMessagesProcessed();

            // Assert
            assertEquals(initialCount + 1, counter.count());
        }
    }

    @Nested
    @DisplayName("incrementMessagesRejected() Tests")
    class IncrementMessagesRejectedTests {

        @Test
        @DisplayName("Should increment messages rejected counter")
        void shouldIncrementMessagesRejectedCounter() {
            // Arrange
            Counter counter = meterRegistry.find("notification.messages.rejected").counter();
            double initialCount = counter.count();

            // Act
            metrics.incrementMessagesRejected();

            // Assert
            assertEquals(initialCount + 1, counter.count());
        }
    }

    @Nested
    @DisplayName("getProcessingTimer() Tests")
    class GetProcessingTimerTests {

        @Test
        @DisplayName("Should return processing timer")
        void shouldReturnProcessingTimer() {
            // Act
            Timer timer = metrics.getProcessingTimer();

            // Assert
            assertNotNull(timer);
        }
    }

    @Nested
    @DisplayName("recordProcessingTime() Tests")
    class RecordProcessingTimeTests {

        @Test
        @DisplayName("Should record processing time")
        void shouldRecordProcessingTime() {
            // Arrange
            Timer timer = meterRegistry.find("notification.processing.time").timer();
            long initialCount = timer.count();

            // Act
            metrics.recordProcessingTime(() -> {
                // Simulate some work
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Assert
            assertEquals(initialCount + 1, timer.count());
            assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) > 0);
        }
    }

    @Nested
    @DisplayName("Counter Tags Tests")
    class CounterTagsTests {

        @Test
        @DisplayName("Should have service tag on counters")
        void shouldHaveServiceTagOnCounters() {
            // Assert
            Counter emailsSent = meterRegistry.find("notification.emails.sent")
                    .tag("service", "notification")
                    .counter();

            assertNotNull(emailsSent);
        }
    }
}
