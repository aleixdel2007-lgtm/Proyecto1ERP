package application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"application", "model", "config", "service", "controller", "dao"})
@EntityScan("model")
@EnableJpaRepositories("dao")
@EnableMongoRepositories("dao")
@EnableScheduling // activa los metodos con @Scheduled (cancelar pedidos pendientes caducados)
public class Proyecto1ErpApplication {
    public static void main(String[] args) {
	SpringApplication.run(Proyecto1ErpApplication.class, args);   
    }
}
