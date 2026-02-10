package br.com.postech.feedback.ingestion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class FeedbackIngestionApplicationTests {

    @Test
    void applicationClassIsInstantiable() {
        assertNotNull(new FeedbackIngestionApplication());
    }
}
