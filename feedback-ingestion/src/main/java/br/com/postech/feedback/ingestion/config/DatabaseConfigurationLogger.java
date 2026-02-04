package br.com.postech.feedback.ingestion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listener que loga as variáveis de ambiente críticas para Lambda.
 * Ajuda a debugar problemas de configuração.
 */
@Component
public class DatabaseConfigurationLogger {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigurationLogger.class);

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String datasourceUsername;

    @Value("${spring.datasource.password}")
    private String datasourcePassword;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String hibernateDdlAuto;

    @Value("${spring.jpa.properties.hibernate.dialect}")
    private String hibernateDialect;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.endpoint}")
    private String awsEndpoint;

    @EventListener
    public void logDatabaseConfiguration(ApplicationContextInitializedEvent event) {
        logger.info("════════════════════════════════════════════════════════════════");
        logger.info("🔧 [CONFIG] DATABASE CONFIGURATION DURING STARTUP");
        logger.info("════════════════════════════════════════════════════════════════");
        logger.info("📝 [CONFIG] DATASOURCE URL: {}", maskString(datasourceUrl));
        logger.info("📝 [CONFIG] DATASOURCE USERNAME: {}", datasourceUsername);
        logger.info("📝 [CONFIG] DATASOURCE PASSWORD: {}", datasourcePassword.isEmpty() ? "[NOT SET]" : "[SET]");
        logger.info("📝 [CONFIG] HIBERNATE DDL-AUTO: {}", hibernateDdlAuto.isEmpty() ? "[DEFAULT]" : hibernateDdlAuto);
        logger.info("📝 [CONFIG] HIBERNATE DIALECT: {}", hibernateDialect.isEmpty() ? "[DEFAULT]" : hibernateDialect);
        logger.info("📝 [CONFIG] AWS REGION: {}", awsRegion.isEmpty() ? "[DEFAULT]" : awsRegion);
        logger.info("📝 [CONFIG] AWS ENDPOINT: {}", awsEndpoint.isEmpty() ? "[PRODUCTION]" : awsEndpoint);
        logger.info("════════════════════════════════════════════════════════════════");

        // Validação
        if (datasourceUrl.isEmpty() || datasourceUrl.contains("localhost")) {
            logger.warn("⚠️  [CONFIG] DATASOURCE URL está vazio ou localhost! Verifique SPRING_DATASOURCE_URL");
        }
        if (datasourceUsername.isEmpty()) {
            logger.warn("⚠️  [CONFIG] DATASOURCE USERNAME está vazio! Verifique SPRING_DATASOURCE_USERNAME");
        }
        if (datasourcePassword.isEmpty()) {
            logger.warn("⚠️  [CONFIG] DATASOURCE PASSWORD está vazio! Verifique SPRING_DATASOURCE_PASSWORD");
        }
    }

    private String maskString(String value) {
        if (value == null || value.isEmpty()) {
            return "[NOT SET]";
        }
        // Mascara password em URLs
        return value.replaceAll("password=[^&;]*", "password=***");
    }
}
