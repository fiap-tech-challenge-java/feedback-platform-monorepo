package br.com.postech.feedback.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.ses.SesClient;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${cloud.aws.region.static:}")
    private String region;

    @Value("${cloud.aws.ses.endpoint:}")
    private String endpointUrl;

    @Value("${aws.access-key:}")
    private String accessKey;

    @Value("${aws.secret-key:}")
    private String secretKey;

    private Region resolveRegion() {
        if (region != null && !region.isBlank()) {
            return Region.of(region);
        }
        return new DefaultAwsRegionProviderChain().getRegion();
    }

    private boolean isLocalEnvironment() {
        return endpointUrl != null && !endpointUrl.isBlank();
    }

    @Bean
    public SesClient sesClient() {
        var builder = SesClient.builder()
                .region(resolveRegion());

        if (isLocalEnvironment()) {
            builder.endpointOverride(URI.create(endpointUrl))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
