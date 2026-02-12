package br.com.postech.feedback.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

public abstract class AwsResourceInitializer implements CommandLineRunner {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${aws.endpoint:#{null}}")
    protected String endpoint;

    @Value("${aws.resources.auto-init:true}")
    protected boolean autoInit;

    @Override
    public void run(String... args) throws Exception {
        if (!autoInit) {
            logger.info("AWS resource auto-initialization is disabled");
            return;
        }

        if (endpoint == null || endpoint.isEmpty()) {
            logger.info("Running in AWS mode - skipping resource initialization");
            return;
        }

        logger.info("Running in LocalStack mode - initializing resources...");
        initializeResources();
        logger.info("✅ AWS resources initialized successfully");
    }

    protected abstract void initializeResources();
}
