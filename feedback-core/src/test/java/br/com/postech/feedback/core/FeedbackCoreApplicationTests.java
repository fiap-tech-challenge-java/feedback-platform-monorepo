package br.com.postech.feedback.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FeedbackCoreModule Tests")
class FeedbackCoreApplicationTests {

    @Nested
    @DisplayName("BASE_PACKAGE Constant Tests")
    class BasePackageConstantTests {

        @Test
        @DisplayName("BASE_PACKAGE should not be null")
        void basePackageConstantShouldNotBeNull() {
            assertNotNull(FeedbackCoreModule.BASE_PACKAGE);
        }

        @Test
        @DisplayName("BASE_PACKAGE should have correct value")
        void basePackageConstantShouldHaveCorrectValue() {
            assertEquals("br.com.postech.feedback.core", FeedbackCoreModule.BASE_PACKAGE);
        }

        @Test
        @DisplayName("BASE_PACKAGE should not be empty")
        void basePackageConstantShouldNotBeEmpty() {
            assertFalse(FeedbackCoreModule.BASE_PACKAGE.isEmpty());
        }

        @Test
        @DisplayName("BASE_PACKAGE should not be blank")
        void basePackageConstantShouldNotBeBlank() {
            assertFalse(FeedbackCoreModule.BASE_PACKAGE.isBlank());
        }

        @Test
        @DisplayName("BASE_PACKAGE should start with 'br.com.postech'")
        void basePackageConstantShouldStartWithCorrectPrefix() {
            assertTrue(FeedbackCoreModule.BASE_PACKAGE.startsWith("br.com.postech"));
        }

        @Test
        @DisplayName("BASE_PACKAGE should end with 'core'")
        void basePackageConstantShouldEndWithCore() {
            assertTrue(FeedbackCoreModule.BASE_PACKAGE.endsWith("core"));
        }

        @Test
        @DisplayName("BASE_PACKAGE should match the actual package structure")
        void basePackageConstantShouldMatchActualPackage() {
            String actualPackage = FeedbackCoreModule.class.getPackage().getName();
            assertEquals(actualPackage, FeedbackCoreModule.BASE_PACKAGE);
        }
    }

    @Nested
    @DisplayName("Utility Class Design Tests")
    class UtilityClassDesignTests {

        @Test
        @DisplayName("FeedbackCoreModule should be a final class")
        void feedbackCoreModuleShouldBeFinalClass() {
            assertTrue(Modifier.isFinal(FeedbackCoreModule.class.getModifiers()));
        }

        @Test
        @DisplayName("FeedbackCoreModule should have private constructor")
        void feedbackCoreModuleShouldHavePrivateConstructor() throws NoSuchMethodException {
            Constructor<FeedbackCoreModule> constructor = FeedbackCoreModule.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }

        @Test
        @DisplayName("FeedbackCoreModule constructor can be invoked via reflection but is private")
        void constructorCanBeInvokedViaReflectionButIsPrivate() throws Exception {
            Constructor<FeedbackCoreModule> constructor = FeedbackCoreModule.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()),
                "Constructor should be private");

            // With setAccessible(true), we can invoke the private constructor
            constructor.setAccessible(true);
            FeedbackCoreModule instance = constructor.newInstance();

            // Instance is created, but this is not the intended usage
            assertNotNull(instance, "Instance can be created via reflection (not recommended)");
        }

        @Test
        @DisplayName("FeedbackCoreModule should have exactly one constructor")
        void feedbackCoreModuleShouldHaveExactlyOneConstructor() {
            Constructor<?>[] constructors = FeedbackCoreModule.class.getDeclaredConstructors();
            assertEquals(1, constructors.length);
        }

        @Test
        @DisplayName("FeedbackCoreModule should have only static final fields")
        void feedbackCoreModuleShouldHaveOnlyStaticFinalFields() {
            java.lang.reflect.Field[] fields = FeedbackCoreModule.class.getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                assertTrue(Modifier.isStatic(field.getModifiers()),
                    "Field " + field.getName() + " should be static");
                assertTrue(Modifier.isFinal(field.getModifiers()),
                    "Field " + field.getName() + " should be final");
            }
        }
    }

    @Nested
    @DisplayName("Package Structure Tests")
    class PackageStructureTests {

        @Test
        @DisplayName("BASE_PACKAGE should contain valid package separator")
        void basePackageShouldContainValidPackageSeparator() {
            assertTrue(FeedbackCoreModule.BASE_PACKAGE.contains("."));
        }

        @Test
        @DisplayName("BASE_PACKAGE should have correct number of package levels")
        void basePackageShouldHaveCorrectNumberOfPackageLevels() {
            String[] parts = FeedbackCoreModule.BASE_PACKAGE.split("\\.");
            assertEquals(5, parts.length); // br, com, postech, feedback, core
        }

        @Test
        @DisplayName("BASE_PACKAGE parts should all be lowercase")
        void basePackagePartsShouldAllBeLowercase() {
            String[] parts = FeedbackCoreModule.BASE_PACKAGE.split("\\.");
            for (String part : parts) {
                assertEquals(part.toLowerCase(), part);
            }
        }

        @Test
        @DisplayName("BASE_PACKAGE should not contain spaces")
        void basePackageShouldNotContainSpaces() {
            assertFalse(FeedbackCoreModule.BASE_PACKAGE.contains(" "));
        }

        @Test
        @DisplayName("BASE_PACKAGE should not contain special characters except dots")
        void basePackageShouldNotContainSpecialCharactersExceptDots() {
            String withoutDots = FeedbackCoreModule.BASE_PACKAGE.replace(".", "");
            assertTrue(withoutDots.matches("[a-z]+"));
        }
    }

    @Nested
    @DisplayName("Usage Pattern Tests")
    class UsagePatternTests {

        @Test
        @DisplayName("BASE_PACKAGE can be used for component scanning")
        void basePackageCanBeUsedForComponentScanning() {
            // Verifica que o pacote pode ser usado para scanning
            String scanPackage = FeedbackCoreModule.BASE_PACKAGE;
            assertNotNull(scanPackage);
            assertFalse(scanPackage.isEmpty());

            // Simula uso em @ComponentScan
            String[] basePackages = { FeedbackCoreModule.BASE_PACKAGE };
            assertEquals(1, basePackages.length);
            assertEquals("br.com.postech.feedback.core", basePackages[0]);
        }

        @Test
        @DisplayName("BASE_PACKAGE is consistent across multiple accesses")
        void basePackageIsConsistentAcrossMultipleAccesses() {
            String first = FeedbackCoreModule.BASE_PACKAGE;
            String second = FeedbackCoreModule.BASE_PACKAGE;
            String third = FeedbackCoreModule.BASE_PACKAGE;

            assertSame(first, second);
            assertSame(second, third);
        }
    }
}
