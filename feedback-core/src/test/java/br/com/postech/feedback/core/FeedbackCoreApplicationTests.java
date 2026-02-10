package br.com.postech.feedback.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class FeedbackCoreApplicationTests {

    @Test
    void basePackageConstantIsAvailable() {
        assertNotNull(FeedbackCoreModule.BASE_PACKAGE);
    }
}
