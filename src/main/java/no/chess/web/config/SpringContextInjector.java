package no.chess.web.config;

import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import no.chess.web.server.SkjemaResurs;
import no.chess.web.server.provider.FreemarkerMessageBodyWriter;
import no.chess.web.server.resource.RapporterChessStartServerResourceHTML;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

@Component // ✅ MÅ VÆRE PÅ Forteller Spring Boot å behandle denne klassen som en Spring Bean. Dette gjør at Spring kan opprette og administrere objektet, og injisere det i RESTEasy-rammeverket.
@WebListener // <--- LEGG TIL DENNE!
public class SpringContextInjector implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
    	// --- NØKKELEN ER HER: Hent konteksten trygt ---
        ApplicationContext springContext = 
            WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());        
        if (springContext == null) {
             throw new IllegalStateException("Spring Context ble ikke injisert. Sjekk at @Component er på.");
        }
        System.out.println("SpringContextInjector - ServletContextListener henter alle beans");
        // Steg 1: Hent alle Bean-en
        SkjemaResurs skjemaResursBean = springContext.getBean(SkjemaResurs.class);
//        FreemarkerMessageBodyWriter freeMarker = springContext.getBean(FreemarkerMessageBodyWriter.class);
        RapporterChessStartServerResourceHTML chessStarterServer = springContext.getBean(RapporterChessStartServerResourceHTML.class);
        FreemarkerMessageBodyWriter bodyWriter = springContext.getBean(FreemarkerMessageBodyWriter.class);
        // Steg 2: Sett den statisk i ResteasyConfig
        ResteasyConfig.setSkjemaResursSingleton(skjemaResursBean);
        ResteasyConfig.setChessStarterServer(chessStarterServer);
//        ResteasyConfig.setFreemarkerResourceSingleton(freeMarker);
        ResteasyConfig.setFreemarkerWriterSingleton(bodyWriter);
        System.out.println("ResteasyConfig suksessfullt initiert med fullt injisert Spring Beans.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // ...
    }
}
