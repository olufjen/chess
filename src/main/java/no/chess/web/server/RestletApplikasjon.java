package no.chess.web.server;
import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;


@Component // Forteller Spring å behandle denne som en Bean
@ApplicationPath("/api")
public class RestletApplikasjon extends Application {

	@Override
    public Set<Class<?>> getClasses() {
        // Legg til alle klasser med @Path her.
        return Set.of(
            SkjemaResurs.class 
        );
    }
}
