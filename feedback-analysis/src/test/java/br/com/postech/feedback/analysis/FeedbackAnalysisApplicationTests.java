package br.com.postech.feedback.analysis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

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

	@Test
	void contextLoads() {
		// Verifica se o contexto Spring carrega corretamente
	}

}
