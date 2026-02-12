package br.com.postech.feedback.core.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AwsResourceInitializer Tests")
class AwsResourceInitializerTest {

    private TestAwsResourceInitializer initializer;

    // Test implementation of abstract class
    private static class TestAwsResourceInitializer extends AwsResourceInitializer {
        private boolean resourcesInitialized = false;

        @Override
        protected void initializeResources() {
            resourcesInitialized = true;
        }

        public boolean isResourcesInitialized() {
            return resourcesInitialized;
        }

        public void resetInitialization() {
            resourcesInitialized = false;
        }
    }

    @BeforeEach
    void setUp() {
        initializer = new TestAwsResourceInitializer();
    }

    @Nested
    @DisplayName("LocalStack Mode Tests")
    class LocalStackModeTests {

        @Test
        @DisplayName("Should initialize resources when endpoint is configured")
        void shouldInitializeResourcesWhenEndpointIsConfigured() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act
            initializer.run();

            // Assert
            assertTrue(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Should initialize resources with different LocalStack endpoints")
        void shouldInitializeResourcesWithDifferentLocalStackEndpoints() throws Exception {
            // Arrange
            String[] endpoints = {
                "http://localhost:4566",
                "http://localhost:4567",
                "http://localstack:4566",
                "https://localstack.local:4566"
            };

            for (String endpoint : endpoints) {
                initializer.resetInitialization();
                ReflectionTestUtils.setField(initializer, "endpoint", endpoint);
                ReflectionTestUtils.setField(initializer, "autoInit", true);

                // Act
                initializer.run();

                // Assert
                assertTrue(initializer.isResourcesInitialized(),
                    "Should initialize with endpoint: " + endpoint);
            }
        }
    }

    @Nested
    @DisplayName("AWS Mode Tests")
    class AwsModeTests {

        @Test
        @DisplayName("Should skip initialization when endpoint is null")
        void shouldSkipInitializationWhenEndpointIsNull() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", null);
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act
            initializer.run();

            // Assert
            assertFalse(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Should skip initialization when endpoint is empty")
        void shouldSkipInitializationWhenEndpointIsEmpty() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act
            initializer.run();

            // Assert
            assertFalse(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Should initialize with blank endpoint (isEmpty check)")
        void shouldInitializeWithBlankEndpoint() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "   ");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act
            initializer.run();

            // Assert
            // Note: The code checks isEmpty() not isBlank(), so "   " is considered valid
            assertTrue(initializer.isResourcesInitialized());
        }
    }

    @Nested
    @DisplayName("Auto-Init Configuration Tests")
    class AutoInitConfigurationTests {

        @Test
        @DisplayName("Should skip initialization when autoInit is false")
        void shouldSkipInitializationWhenAutoInitIsFalse() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(initializer, "autoInit", false);

            // Act
            initializer.run();

            // Assert
            assertFalse(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Should skip initialization when autoInit is false even with valid endpoint")
        void shouldSkipInitializationWhenAutoInitIsFalseEvenWithValidEndpoint() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(initializer, "autoInit", false);

            // Act
            initializer.run();

            // Assert
            assertFalse(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Should initialize when autoInit is true and endpoint is set")
        void shouldInitializeWhenAutoInitIsTrueAndEndpointIsSet() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act
            initializer.run();

            // Assert
            assertTrue(initializer.isResourcesInitialized());
        }
    }

    @Nested
    @DisplayName("Command Line Arguments Tests")
    class CommandLineArgumentsTests {

        @Test
        @DisplayName("Should handle run with no arguments")
        void shouldHandleRunWithNoArguments() {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act & Assert
            assertDoesNotThrow(() -> initializer.run());
            assertTrue(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Should handle run with empty array")
        void shouldHandleRunWithEmptyArray() {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act & Assert
            assertDoesNotThrow(() -> initializer.run(new String[]{}));
            assertTrue(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Should handle run with multiple arguments")
        void shouldHandleRunWithMultipleArguments() {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act & Assert
            assertDoesNotThrow(() -> initializer.run("arg1", "arg2", "arg3"));
            assertTrue(initializer.isResourcesInitialized());
        }
    }

    @Nested
    @DisplayName("Logger Tests")
    class LoggerTests {

        @Test
        @DisplayName("Logger should not be null")
        void loggerShouldNotBeNull() {
            assertNotNull(ReflectionTestUtils.getField(initializer, "logger"));
        }

        @Test
        @DisplayName("Logger class should match implementation class")
        void loggerClassShouldMatchImplementationClass() {
            Object logger = ReflectionTestUtils.getField(initializer, "logger");
            assertNotNull(logger);
            assertTrue(logger.toString().contains("TestAwsResourceInitializer"));
        }
    }

    @Nested
    @DisplayName("Initialization Priority Tests")
    class InitializationPriorityTests {

        @Test
        @DisplayName("Auto-init check should take priority over endpoint check")
        void autoInitCheckShouldTakePriorityOverEndpointCheck() throws Exception {
            // Arrange - autoInit false with valid endpoint
            ReflectionTestUtils.setField(initializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(initializer, "autoInit", false);

            // Act
            initializer.run();

            // Assert - Should not initialize even with valid endpoint
            assertFalse(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Endpoint check should happen after auto-init check")
        void endpointCheckShouldHappenAfterAutoInitCheck() throws Exception {
            // Arrange - autoInit true with null endpoint
            ReflectionTestUtils.setField(initializer, "endpoint", null);
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act
            initializer.run();

            // Assert - Should not initialize with null endpoint
            assertFalse(initializer.isResourcesInitialized());
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        private static class ExceptionThrowingInitializer extends AwsResourceInitializer {
            @Override
            protected void initializeResources() {
                throw new RuntimeException("Initialization failed");
            }
        }

        @Test
        @DisplayName("Should propagate exceptions from initializeResources")
        void shouldPropagateExceptionsFromInitializeResources() {
            // Arrange
            ExceptionThrowingInitializer exceptionInitializer = new ExceptionThrowingInitializer();
            ReflectionTestUtils.setField(exceptionInitializer, "endpoint", "http://localhost:4566");
            ReflectionTestUtils.setField(exceptionInitializer, "autoInit", true);

            // Act & Assert
            assertThrows(Exception.class, () -> exceptionInitializer.run());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should initialize with endpoint containing only spaces")
        void shouldInitializeWithEndpointContainingOnlySpaces() throws Exception {
            // Arrange
            ReflectionTestUtils.setField(initializer, "endpoint", "     ");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act
            initializer.run();

            // Assert
            // Note: The code checks isEmpty() not isBlank(), so spaces are considered valid
            assertTrue(initializer.isResourcesInitialized());
        }

        @Test
        @DisplayName("Should initialize with endpoint containing whitespace around URL")
        void shouldInitializeWithEndpointContainingWhitespaceAroundUrl() throws Exception {
            // Arrange - Spring typically trims property values, but testing the behavior
            ReflectionTestUtils.setField(initializer, "endpoint", "  http://localhost:4566  ");
            ReflectionTestUtils.setField(initializer, "autoInit", true);

            // Act
            initializer.run();

            // Assert
            assertTrue(initializer.isResourcesInitialized());
        }
    }
}

