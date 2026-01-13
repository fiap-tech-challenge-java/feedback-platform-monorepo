package br.com.postech.feedback.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.postech.feedback")
public class FeedbackIngestionApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackIngestionApplication.class, args);
	}

}
