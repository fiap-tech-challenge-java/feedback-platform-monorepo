package br.com.postech.feedback.ingestion.config;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.net.URI;

@Configuration
public class AwsConfig {

    private static final Logger logger = LoggerFactory.getLogger(AwsConfig.class);

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.endpoint:#{null}}")
    private String endpoint;

    @Value("${aws.access-key:#{null}}")
    private String accessKey;

    @Value("${aws.secret-key:#{null}}")
    private String secretKey;

    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());

        if (isLocalStack()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        SqsClient client = builder.build();

        // Criar a fila ANTES do SqsTemplate tentar enviar mensagens
        if (isLocalStack()) {
            createQueueIfNotExists(client);
        }

        return client;
    }

    private void createQueueIfNotExists(SqsClient sqsClient) {
        String queueName = AwsConfigConstants.QUEUE_INGESTION_ANALYSIS;
        try {
            var response = sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
            logger.info("✓ SQS queue '{}' created: {}", queueName, response.queueUrl());
        } catch (Exception e) {
            logger.info("✓ SQS queue '{}' already exists or error: {}", queueName, e.getMessage());
        }
    }

    private AwsCredentialsProvider credentialsProvider() {
        if (isLocalStack()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }

    private boolean isLocalStack() {
        return endpoint != null && !endpoint.isEmpty();
    }
}
