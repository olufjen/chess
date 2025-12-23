package no.chess.web.server;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.chess.web.config.GlobalVisitCounterService;

import java.net.URI;
import org.springframework.stereotype.Component;

@Component
@Path("/skjema") // <-- Må være på klassen for /api/skjema 
public class SkjemaResurs {

	private static final String SESSION_TELLER_KEY = "unikBrukTeller";
	private String message = "Melding fra bean SkjemaResurs";
	   private GlobalVisitCounterService globalCounterService;
	   
	   @GET
	   @Path("/status") 
	   @Produces(MediaType.TEXT_PLAIN)
	   public String getSkjemaStatus(
	       @Context HttpServletRequest request
	   )
	   {
	        // --- 1. SESJONSBASERT TELLER (Per unik bruker) ---
	        HttpSession session = request.getSession(true);
	        Integer sessionTeller = (Integer) session.getAttribute(SESSION_TELLER_KEY);
	        if (sessionTeller == null) {
	            sessionTeller = 0;
	        }
	        sessionTeller++;
	        session.setAttribute(SESSION_TELLER_KEY, sessionTeller);

	        // --- 2. GLOBAL TELLER (Alle besÃ¸k) ---
	        // Bruk den injiserte tjenesten direkte
	        int totalCount = globalCounterService.incrementAndGetCount();
	        
	        
	        // --- 3. RESPONS ---
	        return "--- BesÃ¸ksinformasjon for /api/skjema/status ---\n\n" +
	               "GLOBAL TELLER: Dette endepunktet har blitt kalt totalt " + totalCount + " ganger siden serverstart.\n" +
	               "SESJONSTELLER: Du har besÃ¸kt denne siden " + sessionTeller + " ganger i din nÃ¥vÃ¦rende Ã¸kt.\n" +
	               "Sesjons-ID: " + session.getId();
	    }        
    /**
     * Håndterer GET-forespørsler til /api/skjema.
     * Returnerer selve HTML-siden.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response visSkjema() {
        // Her returnerer vi en omadressering (Redirect) til den statiske HTML-filen.
        // Dette er den enkleste måten å vise en statisk HTML-side på.
        try {
            // OBS: Må peke til der index.html ligger relativt til Context-roten.
            // Hvis index.html ligger i roten av webapp-mappen:
            URI location = new URI("../forside.html"); 
            return Response.temporaryRedirect(location).build();
        } catch (Exception e) {
            return Response.serverError().entity("Feil ved lasting av skjema: " + e.getMessage()).build();
        }
    }

    /**
     * Håndterer POST-forespørsler fra skjemaet (ACTION: api/skjema METHOD: POST).
     * Bruker @FormParam for å fange data fra HTML-skjemaet.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML) // Returnerer HTML som bekreftelse
    public Response mottaSkjema(
        // @FormParam fanger verdien fra HTML-feltet med name="melding"
        @FormParam("melding") String innsendtMelding) 
    {
        // 1. Logikk: Gjør noe med den innsendte meldingen (f.eks. lagre til database, prosessering...)
        System.out.println("Mottatt fra skjema: " + innsendtMelding);
        
        // 2. Respons: Lag en bekreftelsesmelding
        String responsHtml = String.format(
        		"Meldingen '%s' er mottatt og behandlet.",
            innsendtMelding
        );

        // Returnerer en HTTP 200 OK med bekreftelses-HTML
        return Response.ok(responsHtml).build();
    }
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
    
}