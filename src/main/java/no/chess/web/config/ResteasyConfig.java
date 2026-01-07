package no.chess.web.config;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import no.chess.web.server.RestletApplikasjon;
import no.chess.web.server.SkjemaResurs;
import no.chess.web.server.provider.FreemarkerMessageBodyWriter;
import no.chess.web.server.resource.RapporterChessStartServerResourceHTML;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;


/**
 * Subklasse av jakarta.ws.rs.core.Application
 * Definerer rotstien for alle REST-tjenester i applikasjonen. Alle JAX-RS-ressurser vil starte med denne stien. /restapi
 * DEn definerer og setter opp alle definerte beans @component som singletons
 * 
 */
@ApplicationPath("/restapi") 
public class ResteasyConfig extends Application {

    //Statisk felt som holdes av JAX-RS Application.
	// Dette er definerte Spring beans
    private static SkjemaResurs skjemaResursSingleton;
    private static FreemarkerMessageBodyWriter freemarkerWriterSingleton; // MessageBodyWriter er en Provider og benyttes for Freemarker
    private static RapporterChessStartServerResourceHTML chessStarterServer; // For dialog med bruker
    private static SpringContextInjector springcontextInjector;
    private static RestletApplikasjon restletApplication;
/* 
 * Alle set rutinene er kalt av SpringContextInjector
 */
    public static void setSkjemaResursSingleton(SkjemaResurs instance) {
        skjemaResursSingleton = instance;
        System.out.println("Singleton Skjema Resurs bean injisert til RestEasyConfig extends Application");
//        String message = skjemaResursSingleton.getMessage();
//        System.out.println("Melding fra bean "+message);
    }

	public static void setRestletApplication(RestletApplikasjon restletApplication) {
		ResteasyConfig.restletApplication = restletApplication;
	       System.out.println("Singleton restletApplication bean injisert til RestEasyConfig extends Application");

	}


	public static void setSpringcontextInjector(SpringContextInjector springcontextInjector) {
		ResteasyConfig.springcontextInjector = springcontextInjector;
		 System.out.println("Singleton springcontextInjector bean injisert til RestEasyConfig");
	}




 // Ny setter for FreemarkerMessageBodyWriter
 public static void setFreemarkerWriterSingleton(FreemarkerMessageBodyWriter instance) {
     freemarkerWriterSingleton = instance;
     System.out.println("Singleton FreemarkerWriter bean injisert til RestEasyConfig");
 }


 public static void setChessStarterServer(RapporterChessStartServerResourceHTML chessStarterServer) {
	ResteasyConfig.chessStarterServer = chessStarterServer;
    System.out.println("Singleton chessStarterServer  bean injisert til RestEasyConfig");
 }




	/**
     * getSingletons()
     * Returnerer alle definerte beans
     */
    @Override
    public Set<Object> getSingletons() {
    	System.out.println("Henter singletons i RestEasyConfig");
        if (skjemaResursSingleton == null || freemarkerWriterSingleton == null) {
            throw new IllegalStateException("Alle obligatoriske singletons ble ikke satt av Spring/Context Injector.");
       }

        // Opprett og returner et sett med alle singletons
        Set<Object> singletons = new HashSet<>();
        singletons.add(skjemaResursSingleton);
        singletons.add(freemarkerWriterSingleton);
        singletons.add(chessStarterServer);
//        singletons.add(springcontextInjector);
//        singletons.add(restletApplication);
        System.out.println("Returnerer definerte sigletons");
        return singletons;
 
    }
    
    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }

}

