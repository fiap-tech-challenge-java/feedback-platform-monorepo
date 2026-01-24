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

import java.net.URI;

@Configuration
public class AwsSqsConfig {

    private static final Logger logger = LoggerFactory.getLogger(AwsSqsConfig.class);

    @Value("${aws.region:us-east-2}")
    private String region;

    @Value("${aws.endpoint:#{null}}")
    private String endpoint;

    @Value("${aws.access-key:test}")
    private String accessKey;

    @Value("${aws.secret-key:test}")
    private String secretKey;

    @Bean
    public SqsAsyncClient sqsAsyncClient(SqsClient sqsClient) {
        logger.info("🔧 [AWS] Inicializando SqsAsyncClient...");
        logger.debug("🔧 [AWS] Region: {}, IsLocalStack: {}", region, isLocalStack());

        try {
            var builder = SqsAsyncClient.builder()
                    .region(Region.of(region));

            if (isLocalStack()) {
                logger.info("🔧 [AWS] LocalStack detectado - configurando SqsAsyncClient com endpoint: {}", endpoint);
                builder.endpointOverride(URI.create(endpoint))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)));
            } else {
                logger.info("🔧 [AWS] AWS Produção detectado - SqsAsyncClient usará credenciais IAM");
            }

            SqsAsyncClient client = builder.build();
            logger.info("✅ [AWS] SqsAsyncClient inicializado com sucesso!");

            return client;

        } catch (Exception e) {
            logger.error("❌ [AWS] Erro ao inicializar SqsAsyncClient: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isLocalStack() {
        return endpoint != null && !endpoint.isEmpty();
    }
}