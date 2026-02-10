package br.com.postech.feedback.reporting.config;

import br.com.postech.feedback.core.config.AwsConfigConstants;
import br.com.postech.feedback.core.config.AwsResourceInitializer;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;

/**
 * Inicializador de recursos AWS para o serviço de Reporting.
 *
 * Recursos criados automaticamente no LocalStack:
 * - S3 Bucket para armazenamento de relatórios
 * - SNS Topic para notificações
 */
@Component
public class ReportingAwsResourceInitializer extends AwsResourceInitializer {

    private final S3Client s3Client;
    private final SnsClient snsClient;

    public ReportingAwsResourceInitializer(S3Client s3Client, SnsClient snsClient) {
        this.s3Client = s3Client;
        this.snsClient = snsClient;
    }

    @Override
    protected void initializeResources() {
        createBucketIfNotExists();
        createTopicIfNotExists();
    }

    private void createBucketIfNotExists() {
        String bucketName = AwsConfigConstants.BUCKET_REPORTS;
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            logger.info("✓ S3 bucket '{}' already exists", bucketName);
        } catch (Exception e) {
            try {
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                logger.info("✓ S3 bucket '{}' created successfully", bucketName);
            } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ex) {
                logger.info("✓ S3 bucket '{}' already exists", bucketName);
            } catch (Exception ex) {
                logger.error("✗ Failed to create S3 bucket '{}': {}", bucketName, ex.getMessage());
            }
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
