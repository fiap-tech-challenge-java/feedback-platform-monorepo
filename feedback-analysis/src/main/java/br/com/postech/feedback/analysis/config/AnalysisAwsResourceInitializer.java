package br.com.postech.feedback.analysis.config;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import br.com.postech.feedback.core.config.AwsResourceInitializer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;

/**
 * Inicializador de recursos AWS para o serviço de Analysis.
 *
 * Recursos criados automaticamente no LocalStack:
 * - SNS Topic para notificações
 */
@Component
public class AnalysisAwsResourceInitializer extends AwsResourceInitializer {

    private final SnsClient snsClient;

    public AnalysisAwsResourceInitializer(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    @Override
    protected void initializeResources() {
        createTopicIfNotExists();
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
