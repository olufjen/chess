package no.chess.web.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/*
 * The spring boot application
 */
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
    	System.out.println("Starter Spring boot");
        SpringApplication.run(Application.class, args);
    }

}
