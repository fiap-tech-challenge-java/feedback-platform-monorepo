package br.com.postech.feedback.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.postech.feedback")
public class FeedbackAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackAnalysisApplication.class, args);
	}

}
