package br.com.postech.feedback.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(
		scanBasePackages = "br.com.postech.feedback",
		exclude = {
			DataSourceAutoConfiguration.class,
			HibernateJpaAutoConfiguration.class,
			JpaRepositoriesAutoConfiguration.class
		}
)
public class FeedbackNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackNotificationApplication.class, args);
	}

}
