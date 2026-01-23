package br.com.postech.feedback.analysis.config;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

import java.net.URI;

@Configuration
public class AwsConfig {

    private static final Logger logger = LoggerFactory.getLogger(AwsConfig.class);

    @Value("${spring.cloud.aws.region.static:us-east-1}")
    private String region;

    // Se essa variável estiver preenchida (no YAML), usamos ela (LocalStack).
    // Se estiver vazia (na AWS), usamos o comportamento padrão.
    @Value("${spring.cloud.aws.endpoint:}")
    private String endpointUrl;

    @Bean
    public SnsClient snsClient() {
        var builder = SnsClient.builder()
                .region(Region.of(region));

        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")));
        }

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
                .region(Region.of(region));

        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")));
        }

        SqsClient client = builder.build();

        // Criar a fila ANTES do @SqsListener tentar se conectar
        if (endpointUrl != null && !endpointUrl.isBlank()) {
            createQueueIfNotExists(client);
        }

        return client;
    }

    /**
     * Bean SqsAsyncClient usado pelo Spring Cloud AWS SQS Listener.
     * A fila é criada de forma síncrona antes de retornar o client.
     */
    @Bean
    public SqsAsyncClient sqsAsyncClient(SqsClient sqsClient) {
        var builder = SqsAsyncClient.builder()
                .region(Region.of(region));

        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")));
        }

        return builder.build();
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
}