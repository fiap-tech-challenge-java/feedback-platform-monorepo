package br.com.postech.feedback.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "br.com.postech.feedback")
@EntityScan(basePackages = "br.com.postech.feedback.core.domain")
@EnableJpaRepositories(basePackages = "br.com.postech.feedback.core.repository")
public class FeedbackReportingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackReportingApplication.class, args);
	}

}
