package br.com.postech.feedback.reporting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.postech.feedback")
public class FeedbackReportingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackReportingApplication.class, args);
	}

}
