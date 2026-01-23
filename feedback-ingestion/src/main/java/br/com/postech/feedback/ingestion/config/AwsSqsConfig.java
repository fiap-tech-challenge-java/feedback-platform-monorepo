package br.com.postech.feedback.ingestion.config;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.net.URI;

@Configuration
public class AwsSqsConfig {

    private static final Logger logger = LoggerFactory.getLogger(AwsSqsConfig.class);

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.endpoint:#{null}}")
    private String endpoint;

    @Value("${aws.access-key:test}")
    private String accessKey;

    @Value("${aws.secret-key:test}")
    private String secretKey;

    @Bean
    public SqsAsyncClient sqsAsyncClient(SqsClient sqsClient) {
        var builder = SqsAsyncClient.builder()
                .region(Region.of(region));

        if (isLocalStack()) {
            builder.endpointOverride(URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)));
        }

        return builder.build();
    }

    private boolean isLocalStack() {
        return endpoint != null && !endpoint.isEmpty();
    }
}