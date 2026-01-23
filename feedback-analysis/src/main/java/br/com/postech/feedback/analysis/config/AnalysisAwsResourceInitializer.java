package br.com.postech.feedback.analysis.config;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import br.com.postech.feedback.core.config.AwsResourceInitializer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;

/**
 * Inicializador de recursos AWS para o serviço de Analysis.
 *
 * Recursos criados automaticamente no LocalStack:
 * - SQS Queue para receber feedbacks (que o @SqsListener escuta)
 * - SNS Topic para notificações
 */
@Component
public class AnalysisAwsResourceInitializer extends AwsResourceInitializer {

    private final SnsClient snsClient;
    private final SqsClient sqsClient;

    public AnalysisAwsResourceInitializer(SnsClient snsClient, SqsClient sqsClient) {
        this.snsClient = snsClient;
        this.sqsClient = sqsClient;
    }

    @Override
    protected void initializeResources() {
        createQueueIfNotExists();
        createTopicIfNotExists();
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

    private void createTopicIfNotExists() {
        String topicName = AwsConfigConstants.TOPIC_NOTIFICATION;
        try {
            var response = snsClient.createTopic(CreateTopicRequest.builder().name(topicName).build());
            logger.info("✓ SNS topic '{}' created/verified: {}", topicName, response.topicArn());
        } catch (Exception e) {
            logger.error("✗ Failed to create SNS topic '{}': {}", topicName, e.getMessage());
        }
    }
}
