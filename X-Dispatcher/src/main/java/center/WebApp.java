package center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com", "center"})
@EnableScheduling
public class WebApp {
    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }
}
