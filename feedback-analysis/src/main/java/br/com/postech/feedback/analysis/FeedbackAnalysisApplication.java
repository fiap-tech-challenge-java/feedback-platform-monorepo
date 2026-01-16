package br.com.postech.feedback.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(
		scanBasePackages = "br.com.postech.feedback",
		exclude = {
			DataSourceAutoConfiguration .class,
			HibernateJpaAutoConfiguration .class,
			JpaRepositoriesAutoConfiguration .class
		}
)
public class FeedbackAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbackAnalysisApplication.class, args);
	}

}
