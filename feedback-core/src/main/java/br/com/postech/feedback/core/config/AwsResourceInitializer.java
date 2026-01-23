package br.com.postech.feedback.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;

/**
 * Classe base para inicialização automática de recursos AWS/LocalStack.
 *
 * Esta classe deve ser estendida por cada serviço para criar seus recursos específicos.
 * Só executa se aws.endpoint estiver configurado (LocalStack).
 * Na AWS real (produção), não faz nada.
 *
 * Os métodos utilitários usam Object como parâmetro para evitar dependências
 * diretas do AWS SDK no módulo core. Cada serviço faz o cast apropriado.
 */
public abstract class AwsResourceInitializer implements CommandLineRunner {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${aws.endpoint:#{null}}")
    protected String endpoint;

    @Value("${aws.resources.auto-init:true}")
    protected boolean autoInit;

    @Override
    public void run(String... args) throws Exception {
        if (!autoInit) {
            logger.info("AWS resource auto-initialization is disabled");
            return;
        }

        if (endpoint == null || endpoint.isEmpty()) {
            logger.info("Running in AWS mode - skipping resource initialization");
            return;
        }

        logger.info("Running in LocalStack mode - initializing resources...");
        initializeResources();
        logger.info("✅ AWS resources initialized successfully");
    }

    /**
     * Implementar este método para criar os recursos AWS específicos do serviço.
     */
    protected abstract void initializeResources();
}
