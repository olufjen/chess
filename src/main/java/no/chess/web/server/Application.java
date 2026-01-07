package no.chess.web.server;

//import org.apache.catalina.core.ApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
/*
 * The spring boot application
 * Dette er min Applcation class og blir startet av ServletInitializer
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
    	System.out.println("Starter Spring boot");
        SpringApplication.run(Application.class, args);
        System.out.println("Spring boot avslutter...");
    }

        // Denne metoden kjøres automatisk NÅR Spring Boot er helt ferdig lastet
        @Bean
        public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
            return args -> {
                System.out.println("Spring Boot er nå startet og klar til dyst!");
            };
        }
}
