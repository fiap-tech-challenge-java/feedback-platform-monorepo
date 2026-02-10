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
 *
 * ⚠️ NOTA: Em produção (AWS Lambda), esta classe NÃO executa durante o startup
 * porque CommandLineRunner causa timeout de inicialização. As filas são criadas
 * sob-demanda quando necessário ou via infraestrutura como código (CloudFormation/Terraform).
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

    /**
     * Método público para criar a fila sob-demanda (sem bloquear startup)
     * Útil para cenários onde a fila precisa ser criada em tempo de execução
     */
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
