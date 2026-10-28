package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"application", "model", "config", "service", "controller", "dao"})
@EntityScan("model")
@EnableJpaRepositories("dao")
public class Proyecto1ErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(Proyecto1ErpApplication.class, args);
	}

}
