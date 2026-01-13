package br.com.postech.feedback.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "br.com.postech.feedback")
public class FeedbackNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackNotificationApplication.class, args);
	}

}
