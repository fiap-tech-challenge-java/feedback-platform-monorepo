package br.com.postech.feedback.ingestion.config;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import br.com.postech.feedback.core.config.AwsResourceInitializer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;

/**
 * Inicializador de recursos AWS para o serviço de Ingestion.
 *
 * Recursos criados automaticamente no LocalStack:
 * - SQS Queue para processamento de feedbacks
 */
@Component
public class IngestionAwsResourceInitializer extends AwsResourceInitializer {

    private final SqsClient sqsClient;

    public IngestionAwsResourceInitializer(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Override
    protected void initializeResources() {
        createQueueIfNotExists();
    }

    private void createQueueIfNotExists() {
        String queueName = AwsConfigConstants.QUEUE_INGESTION_ANALYSIS;
        try {
            var response = sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
            logger.info("✓ SQS queue '{}' created successfully: {}", queueName, response.queueUrl());
        } catch (QueueNameExistsException e) {
            logger.info("✓ SQS queue '{}' already exists", queueName);
        } catch (Exception e) {
            logger.error("✗ Failed to create SQS queue '{}': {}", queueName, e.getMessage());
        }
    }
}
