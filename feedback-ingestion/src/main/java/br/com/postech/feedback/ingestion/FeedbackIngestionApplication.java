package br.com.postech.feedback.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "br.com.postech.feedback")
@EnableJpaRepositories(basePackages = "br.com.postech.feedback.core.repository")
@EntityScan(basePackages = "br.com.postech.feedback.core.domain")
public class FeedbackIngestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackIngestionApplication.class, args);
	}

}
