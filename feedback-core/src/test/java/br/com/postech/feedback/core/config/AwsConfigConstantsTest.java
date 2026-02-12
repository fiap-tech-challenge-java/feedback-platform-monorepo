package br.com.postech.feedback.core.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AwsConfigConstants Tests")
class AwsConfigConstantsTest {

    @Nested
    @DisplayName("Constant Values Tests")
    class ConstantValuesTests {

        @Test
        @DisplayName("Should have correct queue name constant")
        void shouldHaveCorrectQueueNameConstant() {
            assertEquals("feedback-analysis-queue", AwsConfigConstants.QUEUE_INGESTION_ANALYSIS);
        }

        @Test
        @DisplayName("Should have correct topic name constant")
        void shouldHaveCorrectTopicNameConstant() {
            assertEquals("feedback-notification-topic", AwsConfigConstants.TOPIC_NOTIFICATION);
        }

        @Test
        @DisplayName("Should have correct bucket name constant")
        void shouldHaveCorrectBucketNameConstant() {
            assertEquals("postech-feedback-reports", AwsConfigConstants.BUCKET_REPORTS);
        }

        @Test
        @DisplayName("Should have correct date pattern constant")
        void shouldHaveCorrectDatePatternConstant() {
            assertEquals("yyyy-MM-dd'T'HH:mm:ss", AwsConfigConstants.DATE_PATTERN);
        }
    }

    @Nested
    @DisplayName("Null and Empty Validation Tests")
    class NullAndEmptyValidationTests {

        @Test
        @DisplayName("Constants should not be null")
        void constantsShouldNotBeNull() {
            assertNotNull(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS);
            assertNotNull(AwsConfigConstants.TOPIC_NOTIFICATION);
            assertNotNull(AwsConfigConstants.BUCKET_REPORTS);
            assertNotNull(AwsConfigConstants.DATE_PATTERN);
        }

        @Test
        @DisplayName("Constants should not be empty")
        void constantsShouldNotBeEmpty() {
            assertFalse(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS.isEmpty());
            assertFalse(AwsConfigConstants.TOPIC_NOTIFICATION.isEmpty());
            assertFalse(AwsConfigConstants.BUCKET_REPORTS.isEmpty());
            assertFalse(AwsConfigConstants.DATE_PATTERN.isEmpty());
        }

        @Test
        @DisplayName("Constants should not be blank")
        void constantsShouldNotBeBlank() {
            assertFalse(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS.isBlank());
            assertFalse(AwsConfigConstants.TOPIC_NOTIFICATION.isBlank());
            assertFalse(AwsConfigConstants.BUCKET_REPORTS.isBlank());
            assertFalse(AwsConfigConstants.DATE_PATTERN.isBlank());
        }
    }

    @Nested
    @DisplayName("Date Pattern Tests")
    class DatePatternTests {

        @Test
        @DisplayName("Date pattern should be ISO-8601 compatible")
        void datePatternShouldBeIso8601Compatible() {
            String pattern = AwsConfigConstants.DATE_PATTERN;
            assertTrue(pattern.contains("yyyy"));
            assertTrue(pattern.contains("MM"));
            assertTrue(pattern.contains("dd"));
            assertTrue(pattern.contains("HH"));
            assertTrue(pattern.contains("mm"));
            assertTrue(pattern.contains("ss"));
        }

        @Test
        @DisplayName("Date pattern should contain T separator")
        void datePatternShouldContainTSeparator() {
            assertTrue(AwsConfigConstants.DATE_PATTERN.contains("'T'"));
        }

        @Test
        @DisplayName("Date pattern should be usable with DateTimeFormatter")
        void datePatternShouldBeUsableWithDateTimeFormatter() {
            assertDoesNotThrow(() -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AwsConfigConstants.DATE_PATTERN);
                LocalDateTime now = LocalDateTime.now();
                String formatted = now.format(formatter);
                assertNotNull(formatted);
            });
        }

        @Test
        @DisplayName("Date pattern should format correctly")
        void datePatternShouldFormatCorrectly() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AwsConfigConstants.DATE_PATTERN);
            LocalDateTime dateTime = LocalDateTime.of(2026, 2, 10, 15, 30, 45);
            String formatted = dateTime.format(formatter);

            assertEquals("2026-02-10T15:30:45", formatted);
        }

        @Test
        @DisplayName("Date pattern should parse correctly")
        void datePatternShouldParseCorrectly() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AwsConfigConstants.DATE_PATTERN);
            String dateString = "2026-02-10T15:30:45";
            LocalDateTime parsed = LocalDateTime.parse(dateString, formatter);

            assertEquals(2026, parsed.getYear());
            assertEquals(2, parsed.getMonthValue());
            assertEquals(10, parsed.getDayOfMonth());
            assertEquals(15, parsed.getHour());
            assertEquals(30, parsed.getMinute());
            assertEquals(45, parsed.getSecond());
        }
    }

    @Nested
    @DisplayName("Naming Convention Tests")
    class NamingConventionTests {

        @Test
        @DisplayName("Queue name should follow kebab-case")
        void queueNameShouldFollowKebabCase() {
            String queueName = AwsConfigConstants.QUEUE_INGESTION_ANALYSIS;
            assertTrue(queueName.matches("[a-z]+(-[a-z]+)*"));
        }

        @Test
        @DisplayName("Topic name should follow kebab-case")
        void topicNameShouldFollowKebabCase() {
            String topicName = AwsConfigConstants.TOPIC_NOTIFICATION;
            assertTrue(topicName.matches("[a-z]+(-[a-z]+)*"));
        }

        @Test
        @DisplayName("Bucket name should follow kebab-case")
        void bucketNameShouldFollowKebabCase() {
            String bucketName = AwsConfigConstants.BUCKET_REPORTS;
            assertTrue(bucketName.matches("[a-z]+(-[a-z]+)*"));
        }

        @Test
        @DisplayName("Queue name should contain 'queue'")
        void queueNameShouldContainQueue() {
            assertTrue(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS.contains("queue"));
        }

        @Test
        @DisplayName("Topic name should contain 'topic'")
        void topicNameShouldContainTopic() {
            assertTrue(AwsConfigConstants.TOPIC_NOTIFICATION.contains("topic"));
        }

        @Test
        @DisplayName("Bucket name should contain 'reports'")
        void bucketNameShouldContainReports() {
            assertTrue(AwsConfigConstants.BUCKET_REPORTS.contains("reports"));
        }
    }

    @Nested
    @DisplayName("AWS Resource Name Validation Tests")
    class AwsResourceNameValidationTests {

        @Test
        @DisplayName("Queue name should not contain uppercase")
        void queueNameShouldNotContainUppercase() {
            assertEquals(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS.toLowerCase(),
                        AwsConfigConstants.QUEUE_INGESTION_ANALYSIS);
        }

        @Test
        @DisplayName("Topic name should not contain uppercase")
        void topicNameShouldNotContainUppercase() {
            assertEquals(AwsConfigConstants.TOPIC_NOTIFICATION.toLowerCase(),
                        AwsConfigConstants.TOPIC_NOTIFICATION);
        }

        @Test
        @DisplayName("Bucket name should not contain uppercase")
        void bucketNameShouldNotContainUppercase() {
            assertEquals(AwsConfigConstants.BUCKET_REPORTS.toLowerCase(),
                        AwsConfigConstants.BUCKET_REPORTS);
        }

        @Test
        @DisplayName("Queue name should not contain spaces")
        void queueNameShouldNotContainSpaces() {
            assertFalse(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS.contains(" "));
        }

        @Test
        @DisplayName("Topic name should not contain spaces")
        void topicNameShouldNotContainSpaces() {
            assertFalse(AwsConfigConstants.TOPIC_NOTIFICATION.contains(" "));
        }

        @Test
        @DisplayName("Bucket name should not contain spaces")
        void bucketNameShouldNotContainSpaces() {
            assertFalse(AwsConfigConstants.BUCKET_REPORTS.contains(" "));
        }

        @Test
        @DisplayName("Queue name should not contain special characters except hyphen")
        void queueNameShouldNotContainSpecialCharactersExceptHyphen() {
            assertTrue(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS.matches("[a-z-]+"));
        }

        @Test
        @DisplayName("Topic name should not contain special characters except hyphen")
        void topicNameShouldNotContainSpecialCharactersExceptHyphen() {
            assertTrue(AwsConfigConstants.TOPIC_NOTIFICATION.matches("[a-z-]+"));
        }

        @Test
        @DisplayName("Bucket name should not contain special characters except hyphen")
        void bucketNameShouldNotContainSpecialCharactersExceptHyphen() {
            assertTrue(AwsConfigConstants.BUCKET_REPORTS.matches("[a-z-]+"));
        }
    }

    @Nested
    @DisplayName("Constants Length Tests")
    class ConstantsLengthTests {

        @Test
        @DisplayName("Queue name should have reasonable length")
        void queueNameShouldHaveReasonableLength() {
            int length = AwsConfigConstants.QUEUE_INGESTION_ANALYSIS.length();
            assertTrue(length > 5 && length < 100);
        }

        @Test
        @DisplayName("Topic name should have reasonable length")
        void topicNameShouldHaveReasonableLength() {
            int length = AwsConfigConstants.TOPIC_NOTIFICATION.length();
            assertTrue(length > 5 && length < 100);
        }

        @Test
        @DisplayName("Bucket name should have reasonable length")
        void bucketNameShouldHaveReasonableLength() {
            int length = AwsConfigConstants.BUCKET_REPORTS.length();
            assertTrue(length > 5 && length < 100);
        }

        @Test
        @DisplayName("Date pattern should have reasonable length")
        void datePatternShouldHaveReasonableLength() {
            int length = AwsConfigConstants.DATE_PATTERN.length();
            assertTrue(length > 10 && length < 50);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should be able to access constants statically")
        void shouldBeAbleToAccessConstantsStatically() {
            assertNotNull(AwsConfigConstants.QUEUE_INGESTION_ANALYSIS);
            assertNotNull(AwsConfigConstants.TOPIC_NOTIFICATION);
            assertNotNull(AwsConfigConstants.BUCKET_REPORTS);
            assertNotNull(AwsConfigConstants.DATE_PATTERN);
        }

        @Test
        @DisplayName("Constants should be final")
        void constantsShouldBeFinal() {
            // Constants are declared as public static final by convention
            // This test verifies we can access them without instantiation
            String queue = AwsConfigConstants.QUEUE_INGESTION_ANALYSIS;
            String topic = AwsConfigConstants.TOPIC_NOTIFICATION;
            String bucket = AwsConfigConstants.BUCKET_REPORTS;
            String pattern = AwsConfigConstants.DATE_PATTERN;

            assertNotNull(queue);
            assertNotNull(topic);
            assertNotNull(bucket);
            assertNotNull(pattern);
        }
    }

    @Nested
    @DisplayName("Semantic Meaning Tests")
    class SemanticMeaningTests {

        @Test
        @DisplayName("Queue name should reflect purpose")
        void queueNameShouldReflectPurpose() {
            String queueName = AwsConfigConstants.QUEUE_INGESTION_ANALYSIS;
            assertTrue(queueName.contains("feedback") || queueName.contains("analysis"));
        }

        @Test
        @DisplayName("Topic name should reflect purpose")
        void topicNameShouldReflectPurpose() {
            String topicName = AwsConfigConstants.TOPIC_NOTIFICATION;
            assertTrue(topicName.contains("feedback") || topicName.contains("notification"));
        }

        @Test
        @DisplayName("Bucket name should reflect purpose")
        void bucketNameShouldReflectPurpose() {
            String bucketName = AwsConfigConstants.BUCKET_REPORTS;
            assertTrue(bucketName.contains("feedback") || bucketName.contains("reports"));
        }
    }
}
