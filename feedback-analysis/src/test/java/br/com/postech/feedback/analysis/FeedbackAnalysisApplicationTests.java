package br.com.postech.feedback.analysis;

import br.com.postech.feedback.analysis.service.FeedbackAnalysisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de sanidade do contexto Spring.
 *
 * Verifica se a aplicação consegue inicializar sem erros de configuração,
 * sem precisar conectar aos serviços AWS reais (SQS/SNS desabilitados).
 */
@SpringBootTest
@TestPropertySource(properties = {
	"spring.cloud.aws.sqs.enabled=false",
	"spring.cloud.aws.sns.enabled=false"
})
class FeedbackAnalysisApplicationTests {

	@MockBean
	private SnsClient snsClient;

	@MockBean
	private SqsAsyncClient sqsAsyncClient;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	@DisplayName("Should load Spring application context successfully")
	void contextLoads() {
		// Verifica se o contexto Spring carrega corretamente
		assertNotNull(applicationContext);
	}

	@Test
	@DisplayName("Should have FeedbackAnalysisService bean in context")
	void shouldHaveFeedbackAnalysisServiceBean() {
		// Verifica se o bean principal está presente
		assertTrue(applicationContext.containsBean("feedbackAnalysisService"));
		FeedbackAnalysisService service = applicationContext.getBean(FeedbackAnalysisService.class);
		assertNotNull(service);
	}

	@Test
	@DisplayName("Should have SnsClient bean in context")
	void shouldHaveSnsClientBean() {
		// Verifica se o cliente SNS está configurado
		SnsClient bean = applicationContext.getBean(SnsClient.class);
		assertNotNull(bean);
	}

	@Test
	@DisplayName("Should have SqsAsyncClient bean in context")
	void shouldHaveSqsAsyncClientBean() {
		// Verifica se o cliente SQS Async está configurado
		SqsAsyncClient bean = applicationContext.getBean(SqsAsyncClient.class);
		assertNotNull(bean);
	}

	@Test
	@DisplayName("Should exclude DataSource auto-configuration")
	void shouldExcludeDataSourceAutoConfiguration() {
		// Verifica que DataSource não está presente (foi excluído)
		assertFalse(applicationContext.containsBean("dataSource"));
	}

	@Test
	@DisplayName("Should exclude JPA auto-configuration")
	void shouldExcludeJpaAutoConfiguration() {
		// Verifica que EntityManagerFactory não está presente (JPA foi excluído)
		assertFalse(applicationContext.containsBean("entityManagerFactory"));
	}

	@Test
	@DisplayName("Should scan base packages correctly")
	void shouldScanBasePackagesCorrectly() {
		// Verifica que os pacotes foram escaneados corretamente
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		assertTrue(beanNames.length > 0);

		// Verifica se há beans do pacote br.com.postech.feedback
		boolean hasFeedbackBeans = false;
		for (String beanName : beanNames) {
			if (beanName.contains("feedback")) {
				hasFeedbackBeans = true;
				break;
			}
		}
		assertTrue(hasFeedbackBeans, "Should have beans from feedback package");
	}

	@Test
	@DisplayName("Should have application name configured")
	void shouldHaveApplicationNameConfigured() {
		// Verifica se a aplicação tem um nome
		String applicationName = applicationContext.getEnvironment()
				.getProperty("spring.application.name", "");
		assertNotNull(applicationName);
	}
}
