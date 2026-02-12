package br.com.postech.feedback.notification.config;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("ValidatorConfig Tests")
class ValidatorConfigTest {
    @Test
    @DisplayName("Should create Validator bean")
    void shouldCreateValidatorBean() {
        // Arrange
        ValidatorConfig config = new ValidatorConfig();
        // Act
        Validator validator = config.validator();
        // Assert
        assertNotNull(validator);
    }
    @Test
    @DisplayName("Validator should be able to validate objects")
    void validatorShouldBeAbleToValidateObjects() {
        // Arrange
        ValidatorConfig config = new ValidatorConfig();
        Validator validator = config.validator();
        // Test with a simple record
        record TestRecord(String value) {}
        TestRecord testObj = new TestRecord("test");
        // Act
        var violations = validator.validate(testObj);
        // Assert - no violations for valid object
        assertTrue(violations.isEmpty());
    }
}
