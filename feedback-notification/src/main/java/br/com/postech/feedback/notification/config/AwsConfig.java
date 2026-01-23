package br.com.postech.feedback.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${spring.cloud.aws.region.static:us-east-1}")
    private String region;

    // Se essa variável estiver preenchida (no YAML), usamos ela (LocalStack).
    // Se estiver vazia (na AWS), usamos o comportamento padrão.
    @Value("${spring.cloud.aws.endpoint:}")
    private String endpointUrl;

    @Bean
    public SesClient sesClient() {
        var builder = SesClient.builder()
                .region(Region.of(region));

        if (endpointUrl != null && !endpointUrl.isBlank()) {
            builder.endpointOverride(URI.create(endpointUrl))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("test", "test")));
        }

        return builder.build();
    }
}
