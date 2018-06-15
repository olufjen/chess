package no.chess.web.server.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import no.basis.felles.model.OntologyModel;
import no.basis.felles.semanticweb.chess.BlackBoardPosition;
import no.chess.web.model.ChessBoard;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;

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

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import freemarker.template.SimpleScalar;

/**
 * @author olj
 *  Denne resursen er knyttet til startsiden 
 *  Sjakkspillet
 */
public class RapporterChessStartServerResourceHTML extends ChessServerResource {

	
	private String delMelding = "delmelding";
	private String meldeTxtId = "melding";
	private String passordCheck = "none";
	private String displayPassord = "passord";
	private String position = "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R";
	private String startPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
	



	public String getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(String startPosition) {
		this.startPosition = startPosition;
	}

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

/*		OntologyModel chessModel  = new OntologyModel();
		List allIndividuals = chessModel.getIndividuals();
		Map<String, List<Statement>> properties = chessModel.getProperties(allIndividuals);
		Individual individual = (Individual)allIndividuals.get(0);
		String name = individual.getLocalName();
		List<Statement> statements = properties.get(name);
		Statement statement = statements.get(0);
		Resource chessGame = chessModel.getChessGame();
		Resource  subject   = statement.getSubject();     // get the subject
		Property  predicate = statement.getPredicate();   // get the predicate
		RDFNode   object    = statement.getObject();      // get the object
		String predikatet = statement.getPredicate().getLocalName();
//		Class<? extends IndividualImpl> ind = (Class<? extends IndividualImpl>) individual.getClass();
		OntClass ont = individual.getOntClass();
		BlackBoardPosition position = chessModel.getChessFactory().createBlackBoardPosition(name);
		*/
		
	     Reference reference = new Reference(getReference(),"..").getTargetRef();
	     Request request = getRequest();
	     Map<String, Object> dataModel = new HashMap<String, Object>();
	     String meldingsText = " ";
	     SimpleScalar simple = new SimpleScalar(piece);
	     SimpleScalar movedTo = new SimpleScalar(newPos);
	     SimpleScalar chessPosition = new SimpleScalar(startPosition);
	     
	     ChessBoard chessBoard = new ChessBoard();
	     String[] legalMoves = {"a3","a4"};
	     String fen = chessBoard.createFen();
//	     System.out.println(fen);
	     dataModel.put(fenPosid,fen);

	     sessionAdmin.setSessionObject(request,chessBoard, chessBoardsession);
//	     ChessPiece whitePawn1 = whitePawnpos.getUsedBy();
//	     if (whitePawn1 != null)
//	    	 whitePawn1.setLegalMoves(legalMoves);
		 dataModel.put(pieceId,simple );
		 dataModel.put(displayKey, chessPosition);
//		 dataModel.put(pawnId,whitePawn1);
		
//		 SimpleScalar pwd = new SimpleScalar(passordCheck);
//		 dataModel.put(displayPassord,pwd);
	     LocalReference pakke = LocalReference.createClapReference(LocalReference.CLAP_CLASS,
                 "/chess");
	    
	     LocalReference localUri = new LocalReference(reference);
	
// Denne client resource forholder seg til src/main/resource katalogen !!!	
	     ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));

	        Representation pasientkomplikasjonFtl = clres2.get();

	        TemplateRepresentation  templatemapRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
	                MediaType.TEXT_HTML);
		 return templatemapRep;
	
	}
	
    /**
     * storeChess
     * This method accepts chessmoves from player.
     * Such moves also includes restart of game etc.
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
/*	     String[] legalMoves = {"a3","a4"};
	     ChessPiece whitePawn1 = new ChessPiece(newPos,"w","wP",legalMoves);
	     whitePawn1.setValue(1);*/
    	String[] legalMoves = {"",""};
    	ChessPiece chessPiece = null;
    	ChessBoard chessBoard = (ChessBoard)sessionAdmin.getSessionObject(request, chessBoardsession);
    	Parameter restartGame = form.getFirst("startBtnx"); // Bruker oppgir å starte på nytt
    	Parameter ontology = form.getFirst("ontBtnx"); // User wants ontology position on chessboard
    	Parameter printOntology = form.getFirst("printBtnx"); // User wants ontology printed
    	Parameter reload = form.getFirst("relBtnx"); // User wants to reload ontology from file
    	Parameter query = form.getFirst("qBtnx"); // User wants to query ontology 
    	if (query != null){
    		chessBoard.queryOntology();
     		String fen = chessBoard.createFen();
    		//   	     System.out.println(fen);
    		dataModel.put(fenPosid,fen);
    		sessionAdmin.setSessionObject(request,chessBoard, chessBoardsession);

     //		 dataModel.put(pawnId,whitePawn1);
    	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
            Representation pasientkomplikasjonFtl = clres2.get();
            templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                    MediaType.TEXT_HTML);	
        	return templateRep;   
    	}    	
    	if (reload != null){
    		sessionAdmin.getSession(request,chessBoardsession).invalidate();
    		chessBoard = null;
    		chessBoard = new ChessBoard();
     		String fen = chessBoard.createFen();
    		//   	     System.out.println(fen);
    		dataModel.put(fenPosid,fen);
    		sessionAdmin.setSessionObject(request,chessBoard, chessBoardsession);

     //		 dataModel.put(pawnId,whitePawn1);
    	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
            Representation pasientkomplikasjonFtl = clres2.get();
            templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                    MediaType.TEXT_HTML);	
        	return templateRep;   
    	}
    	if (printOntology != null){
    	 chessBoard.printOntology();	
   	     String fen = chessBoard.createFen();
   	     System.out.println(fen);
   	     dataModel.put(fenPosid,fen);

 //		 dataModel.put(pawnId,whitePawn1);
	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
        Representation pasientkomplikasjonFtl = clres2.get();
        templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                MediaType.TEXT_HTML);	
    	return templateRep;    		
    	}
    	if (ontology != null){
    		chessBoard.createChessontlogyPosition();
     	     String fen = chessBoard.createFen();
       	     System.out.println(fen);
       	     dataModel.put(fenPosid,fen);
        	SimpleScalar pieceMoved = new SimpleScalar(piece);
        	SimpleScalar movedTo = new SimpleScalar(newPos);
    	     SimpleScalar chessPosition = new SimpleScalar(position);
    		 dataModel.put(displayKey, chessPosition);
       	 	dataModel.put(pieceId,pieceMoved );
     //		 dataModel.put(pawnId,whitePawn1);
    	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
            Representation pasientkomplikasjonFtl = clres2.get();
            templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                    MediaType.TEXT_HTML);	
        	return templateRep;
    	}
    	if (restartGame != null){
    		chessBoard.createStartPosition();
      	     String fen = chessBoard.createFen();
       	     System.out.println(fen);
       	     dataModel.put(fenPosid,fen);
        	SimpleScalar pieceMoved = new SimpleScalar(piece);
        	SimpleScalar movedTo = new SimpleScalar(newPos);
    	     SimpleScalar chessPosition = new SimpleScalar(position);
    		 dataModel.put(displayKey, chessPosition);
       	 	dataModel.put(pieceId,pieceMoved );
     //		 dataModel.put(pawnId,whitePawn1);
    	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
            Representation pasientkomplikasjonFtl = clres2.get();
            templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                    MediaType.TEXT_HTML);	
        	return templateRep;
    	}
   // Player has moved a piece 	
    	for (Parameter entry : form) {
			if (entry.getValue() != null && !(entry.getValue().equals(""))){
					System.out.println(entry.getName() + "=" + entry.getValue());
					if (entry.getName().equals("piece"))
						piece = entry.getValue();
					if (entry.getName().equals("posisjon"))
						newPos = entry.getValue();
					if (entry.getName().equals("startposisjon"))
						oldPos = entry.getValue();
			}
			
    	}
    	if (newPos == null || newPos.equals("")){
    		newPos = "a2";
    	}
//    	chessPiece.setName(piece);
 
    	chessPiece = chessBoard.findPiece(oldPos,piece);
       	chessPiece.setPosition(newPos);
       	Position oldPosition = chessBoard.findPostion(oldPos);
       	Position newPosition = chessBoard.findPostion(newPos);
       	if (chessBoard.checkPosition(newPos))
       		chessPiece.setPosition(oldPos);
       	if (!chessBoard.checkPosition(newPos)){
       	   	oldPosition.setUsedBy(null);
           	oldPosition.setInUse(false);
           	HashSet pieces = oldPosition.getPieces();
           	oldPosition.setPieces(null);
           	newPosition.setUsedBy(chessPiece);
        	newPosition.setInUse(true);
        	newPosition.setPieces(pieces);
       	}

   	     String fen = chessBoard.createFen();
   	     System.out.println(fen);
   	     dataModel.put(fenPosid,fen);
    	SimpleScalar pieceMoved = new SimpleScalar(piece);
    	SimpleScalar movedTo = new SimpleScalar(newPos);
	     SimpleScalar chessPosition = new SimpleScalar(position);
		 dataModel.put(displayKey, chessPosition);
   	 	dataModel.put(pieceId,pieceMoved );
 //		 dataModel.put(pawnId,whitePawn1);
	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
        Representation pasientkomplikasjonFtl = clres2.get();
        templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                MediaType.TEXT_HTML);	



    	return templateRep;
    }
}
