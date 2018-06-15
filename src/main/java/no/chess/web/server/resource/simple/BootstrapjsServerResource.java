package no.chess.web.server.resource.simple;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class BootstrapjsServerResource extends ServerResource {

	public BootstrapjsServerResource() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Get
	public Representation getSimple() {
		
	     ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/hemovigilans/scripts/hendelser.js"));
	       Representation innmeldingFtl = clres2.get();
	        TemplateRepresentation  templateRep = new TemplateRepresentation(innmeldingFtl, 
	                MediaType.TEXT_ALL);
		return templateRep;
	}


}
