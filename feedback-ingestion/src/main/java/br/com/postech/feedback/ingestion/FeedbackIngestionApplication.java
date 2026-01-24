package br.com.postech.feedback.ingestion;

import br.com.postech.feedback.core.domain.Feedback;
import br.com.postech.feedback.ingestion.domain.FeedbackResponse;
import br.com.postech.feedback.ingestion.domain.dto.FeedbackRequest;
import br.com.postech.feedback.ingestion.domain.mapper.FeedbackInjectionApiMapper;
import br.com.postech.feedback.ingestion.domain.service.FeedbackInjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.function.Function;

@SpringBootApplication(scanBasePackages = "br.com.postech.feedback")
@EnableJpaRepositories(basePackages = "br.com.postech.feedback.core.repository")
@EntityScan(basePackages = "br.com.postech.feedback.core.domain")
public class FeedbackIngestionApplication {

	private static final Logger logger = LoggerFactory.getLogger(FeedbackIngestionApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(FeedbackIngestionApplication.class, args);
	}

	/**
	 * Bean de função para AWS Lambda.
	 * O FunctionInvoker do Spring Cloud Function procura por um Bean do tipo Function.
	 * Nome do bean = nome da função configurada em SPRING_CLOUD_FUNCTION_DEFINITION
	 */
	@Bean
	public Function<FeedbackRequest, FeedbackResponse> ingestFeedback(FeedbackInjectionService feedbackInjectionService) {
		return feedbackRequest -> {
			long startTime = System.currentTimeMillis();

			logger.info("🔧 [LAMBDA] Função ingestFeedback invocada");
			logger.debug("🔧 [LAMBDA] Payload: description='{}', rating={}",
					feedbackRequest.description(), feedbackRequest.rating());

			try {
				FeedbackInjectionApiMapper mapper = FeedbackInjectionApiMapper.INSTANCE;
				Feedback feedback = feedbackInjectionService.processFeedback(
						mapper.mapToCreateFeedback(feedbackRequest)
				);

				long duration = System.currentTimeMillis() - startTime;
				FeedbackResponse response = mapper.mapToFeedbackResponse(feedback);

				logger.info("✅ [LAMBDA] Função concluída com sucesso - ID: {}, Duração: {}ms",
						response.id(), duration);

				return response;

			} catch (Exception e) {
				long duration = System.currentTimeMillis() - startTime;
				logger.error("❌ [LAMBDA] Erro na função ingestFeedback - Duração: {}ms, Erro: {}",
						duration, e.getMessage(), e);
				throw e;
			}
		};
	}

}
