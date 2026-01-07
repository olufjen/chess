package no.chess.web.server.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import no.chess.web.view.FreemarkerView;
/*
 * Dette er en bean for Freemarker rammeverket
 */

@Component // <-- Forteller Spring at dette er en bønne
@Provider // Registrerer denne klassen som en JAX-RS utvidelse
@Produces(MediaType.TEXT_HTML) // Håndterer kun forespørsler som forventer HTML
public class FreemarkerMessageBodyWriter implements MessageBodyWriter<FreemarkerView> {

    private final Configuration freemarkerConfig;

    public FreemarkerMessageBodyWriter() {
        // 1. Initialiser Freemarker Konfigurasjon
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_32);
        // Angi hvor malene ligger (f.eks. i classpath under /templates/)
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/");
        freemarkerConfig.setDefaultEncoding(StandardCharsets.UTF_8.name());
        System.out.println("FreemarkerMessageBodyWriter startet");
    }

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
	      // Denne MessageBodyWriteren håndterer kun vår FreemarkerView-klasse
        return FreemarkerView.class.isAssignableFrom(type);
	}
    /** Returnerer størrelsen på innholdet (ignoreres ofte for streaming). */
    @Override
    public long getSize(FreemarkerView view, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1; // Ukjent størrelse
    }

    /** Hovedmetoden: Gjengi malen og skriv resultatet til HTTP-responsen. */
    @Override
    public void writeTo(
            FreemarkerView view, 
            Class<?> type, 
            Type genericType, 
            Annotation[] annotations, 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders, 
            OutputStream entityStream) throws IOException, WebApplicationException {
    	 
    	 System.out.println("FreemarkerMessageBodyWriter writeTo - template name "+view.getTemplateName());
        // 2. Hent malen
        Template template;
        try {
            template = freemarkerConfig.getTemplate(view.getTemplateName());
        } catch (IOException e) {
            throw new WebApplicationException("Klarte ikke laste Freemarker mal: " + view.getTemplateName(), e);
        }
//        System.out.println("FreemarkerMessageBodyWriter writeTo - template "+template.toString());
        // 3. Flett datamodellen med malen og skriv til ut-strømmen
        try (OutputStreamWriter writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8)) {
            template.process(view.getDataModel(), writer);
        } catch (TemplateException e) {
            throw new WebApplicationException("Feil under Freemarker gjengivelse.", e);
        }
    }

}
