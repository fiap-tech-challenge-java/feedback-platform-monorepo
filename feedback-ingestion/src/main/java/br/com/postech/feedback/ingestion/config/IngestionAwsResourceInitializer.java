package br.com.postech.feedback.ingestion.config;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import br.com.postech.feedback.core.config.AwsResourceInitializer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;

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

    public void ensureQueueExists() {
        createQueueIfNotExists();
    }

    private void createQueueIfNotExists() {
        String queueName = AwsConfigConstants.QUEUE_INGESTION_ANALYSIS;
        logger.info("📤 [SQS] Tentando criar fila SQS: '{}'", queueName);

        try {
            logger.debug("📤 [SQS] Iniciando CreateQueueRequest para fila: {}", queueName);
            var response = sqsClient.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
            logger.info("✅ [SQS] Fila SQS '{}' criada com sucesso!", queueName);
            logger.info("✅ [SQS] URL da fila: {}", response.queueUrl());
        } catch (QueueNameExistsException e) {
            logger.info("ℹ️  [SQS] Fila SQS '{}' já existe (esperado em LocalStack). Continuando...", queueName);
        } catch (Exception e) {
            logger.error("❌ [SQS] Erro ao criar fila SQS '{}': {} - {}",
                    queueName, e.getClass().getSimpleName(), e.getMessage(), e);
            logger.warn("⚠️  [SQS] Falha ao criar fila, continuando mesmo assim. Verifique conectividade com SQS/LocalStack");
        }
    }
}
