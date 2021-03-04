package spider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com", "spider"})
public class CrawlApp {
    public static void main(String[] args) {
        SpringApplication.run(CrawlApp.class, args);
    }
}
