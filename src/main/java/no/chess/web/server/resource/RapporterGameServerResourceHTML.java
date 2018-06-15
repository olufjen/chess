package no.chess.web.server.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



import no.basis.felles.server.resource.SessionServerResource;

import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import freemarker.template.SimpleScalar;

/**
 * @author olj
 *  Denne resursen er knyttet til startsiden 
 *  Sjakkspillet
 */
public class RapporterGameServerResourceHTML extends ChessServerResource {

	
	private String delMelding = "delmelding";
	private String meldeTxtId = "melding";
	private String passordCheck = "none";
	private String displayPassord = "passord";
	private String position = "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R";



	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getDisplayPassord() {
		return displayPassord;
	}

	public void setDisplayPassord(String displayPassord) {
		this.displayPassord = displayPassord;
	}

	public String getPassordCheck() {
		return passordCheck;
	}

	public void setPassordCheck(String passordCheck) {
		this.passordCheck = passordCheck;
	}

	public String getDelMelding() {
		return delMelding;
	}

	public void setDelMelding(String delMelding) {
		this.delMelding = delMelding;
	}


	public String getMeldeTxtId() {
		return meldeTxtId;
	}

	public void setMeldeTxtId(String meldeTxtId) {
		this.meldeTxtId = meldeTxtId;
	}

	public static Object getPositions(Object object) {
	    System.out.println(object.getClass());
	    return object;
	    
	}

	/**
	 * getChess
	 * Denne rutinen starter med startside.html
	 * Denne rutinen henter inn nødvendige session objekter og  setter opp nettsiden for å 
	 * vise et sjakkbrett
	 * @return
	 */
	@Get
	public Representation getChess() {


	     Reference reference = new Reference(getReference(),"..").getTargetRef();
	     Request request = getRequest();
	     Map<String, Object> dataModel = new HashMap<String, Object>();
	     String meldingsText = " ";
	     SimpleScalar simple = new SimpleScalar(piece);
	     SimpleScalar movedTo = new SimpleScalar(newPos);
	     SimpleScalar chessPosition = new SimpleScalar(position);
		 dataModel.put(pieceId,simple );

		 dataModel.put(displayKey, chessPosition);
//		 SimpleScalar pwd = new SimpleScalar(passordCheck);
//		 dataModel.put(displayPassord,pwd);
	     LocalReference pakke = LocalReference.createClapReference(LocalReference.CLAP_CLASS,
                 "/chess");
	    
	     LocalReference localUri = new LocalReference(reference);
	
// Denne client resource forholder seg til src/main/resource katalogen !!!	
	     ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/game.html"));

	        Representation pasientkomplikasjonFtl = clres2.get();

	        TemplateRepresentation  templatemapRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
	                MediaType.TEXT_HTML);
		 return templatemapRep;
	
	}
	
    /**
     * storeChess
     * Denne rutinen rutinen kjøres dersom epost og passord er gitt fra bruker.
     * Den tar imot epost og passord og henter frem riktig meldingsinformasjon fra
     * databasen basert på melders id 
     * @param form
     * @return
     */
    /**
     * @param form
     * @return
     */
    @Post
    public Representation storeChess(Form form) {
    	TemplateRepresentation  templateRep = null;
 	    Map<String, Object> dataModel = new HashMap<String, Object>();
 	    Request request = getRequest();
	    
    	if(form == null){
    		invalidateSessionobjects();
    	}

    	for (Parameter entry : form) {
			if (entry.getValue() != null && !(entry.getValue().equals(""))){
					System.out.println(entry.getName() + "=" + entry.getValue());
					if (entry.getName().equals("piece"))
						piece = entry.getValue();
					if (entry.getName().equals("posisjon"))
						newPos = entry.getValue();
						
			}
			
    	}
    	SimpleScalar pieceMoved = new SimpleScalar(piece);
    	SimpleScalar movedTo = new SimpleScalar(newPos);
	     SimpleScalar chessPosition = new SimpleScalar(position);
		 dataModel.put(displayKey, chessPosition);
   	 	dataModel.put(pieceId,pieceMoved );

		Parameter formValue = form.getFirst("formValue"); // Bruker oppgir epost og passord
	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/game.html"));
        Representation pasientkomplikasjonFtl = clres2.get();
        templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                MediaType.TEXT_HTML);	



    	return templateRep;
    }
}
