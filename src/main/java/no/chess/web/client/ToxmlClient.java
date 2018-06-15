package no.chess.web.client;

import java.io.IOException;
import java.io.OutputStream;

import org.restlet.data.Digest;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class ToxmlClient {


	    public static void main(String[] args) throws Exception {
	        ClientResource mailClient = new ClientResource(
	                "http://localhost:8080/fagprosedyrer_web/restapi/toxml/chunkylover53/mails/123");
	        AppendableRepresentation mailRepresentation = null;
	        InputRepresentation iMr = null;
	      
	        OutputRepresentation oMr = null;
	       
	      
	        Representation mr = null;
	        Representation nRep = null;
	        try
	        {
	        	mr = mailClient.get();
	        }catch(ResourceException re){
	        	System.out.println(re.getMessage());
	        }
	        iMr = (InputRepresentation)mr;
	        MediaType imt = iMr.getMediaType();
	        String name = imt.getName();
	        Class objectType = mr.getClass();
	        String cName = objectType.getName();
	        MediaType mt = new MediaType(name);
	        Variant target = new Variant(mt);
	        nRep = mailClient.toRepresentation(iMr, target);
//	        oMr = (OutputRepresentation)nRep;
	        String tx = nRep.getText();
//	        mailRepresentation.append("abcd");
	        Tag tag = iMr.getTag();
	        Digest digest = iMr.getDigest();
	        Disposition disposition = iMr.getDisposition();
	      
/*
 * Kallet .getText produserer feilmelding ved .put kall:
 * Caused by: java.io.IOException: Couldn't read the XML representation. Premature end of file.
 * at no.naks.web.server.resource.ToxmlServerresource.store(ToxmlServerresource.java:57)	        
 */
	        System.out.println("Klasse: " + cName);
	        System.out.println("Text: " + tx);
	       try
	       {
	    	   mailClient.put(nRep);
	       }catch(ResourceException re){
	    	   System.out.println(re.getMessage());
	       }
	    }
	
}
