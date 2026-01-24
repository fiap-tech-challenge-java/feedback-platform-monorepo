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

import java.net.URI;

@Configuration
public class AwsConfig {

    private static final Logger logger = LoggerFactory.getLogger(AwsConfig.class);

    @Value("${aws.region:us-east-2}")
    private String region;

    @Value("${aws.endpoint:#{null}}")
    private String endpoint;

    @Value("${aws.access-key:#{null}}")
    private String accessKey;

    @Value("${aws.secret-key:#{null}}")
    private String secretKey;

    @Bean
    public SqsClient sqsClient() {
        logger.info("🔧 [AWS] Inicializando SqsClient...");
        logger.debug("🔧 [AWS] Region: {}, Endpoint: {}, IsLocalStack: {}",
                region, endpoint != null ? endpoint : "NONE", isLocalStack());

        try {
            var builder = SqsClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(credentialsProvider());

            if (isLocalStack()) {
                logger.info("🔧 [AWS] LocalStack detectado - usando endpoint: {}", endpoint);
                builder.endpointOverride(URI.create(endpoint));
            } else {
                logger.info("🔧 [AWS] AWS Produção detectado - usando credenciais IAM");
            }

            SqsClient client = builder.build();
            logger.info("✅ [AWS] SqsClient inicializado com sucesso!");

            return client;

        } catch (Exception e) {
            logger.error("❌ [AWS] Erro ao inicializar SqsClient: {}", e.getMessage(), e);
            throw e;
        }
    }

    private AwsCredentialsProvider credentialsProvider() {
        if (isLocalStack()) {
            logger.debug("🔧 [AWS] Usando credenciais estáticas para LocalStack");
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
        }
        logger.debug("🔧 [AWS] Usando credenciais padrão (IAM) para AWS");
        return DefaultCredentialsProvider.create();
    }

    private boolean isLocalStack() {
        return endpoint != null && !endpoint.isEmpty();
    }
}
