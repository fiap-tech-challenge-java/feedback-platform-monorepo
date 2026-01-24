package br.com.postech.feedback.ingestion.config;

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

    // Se aws.endpoint não estiver definido, será uma String vazia, indicando ambiente Prod real
    @Value("${aws.endpoint:}")
    private String endpoint;

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    @Bean
    public SqsClient sqsClient() {
        boolean isLocal = isLocalStack();
        logger.info("🔧 [AWS Config] Inicializando SqsClient. Ambiente LocalStack? {}", isLocal);

        var builder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider(isLocal));

        if (isLocal) {
            logger.info("🔧 [AWS Config] Override de Endpoint para: {}", endpoint);
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    private AwsCredentialsProvider getCredentialsProvider(boolean isLocal) {
        if (isLocal) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
        }
        // Em Produção (Lambda), usa a Role associada à função automaticamente
        return DefaultCredentialsProvider.create();
    }

    private boolean isLocalStack() {
        return endpoint != null && !endpoint.isBlank();
    }
}