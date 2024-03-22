package no.chess.web.server.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import no.basic.ontology.model.OntologyModel;
import no.chess.ontology.BlackBoardPosition;
import no.chess.web.model.ChessBoard;
import no.chess.web.model.ChessMoves;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.ChessRules;
import no.chess.web.model.EightQueenProblem;
import no.chess.web.model.PlayGame;
import no.chess.web.model.Position;
import no.chess.web.model.QueensEnvironment;
import no.chess.web.model.game.APlayer;
import no.chess.web.model.game.AgamePiece;
import no.chess.web.model.game.ApieceMove;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.FileRepresentation;
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

import aima.core.agent.Agent;
import aima.core.agent.impl.AbstractEnvironment;
import aima.core.search.csp.Constraint;
import aima.core.search.csp.Domain;
import aima.core.search.csp.Variable;
import aima.core.search.csp.examples.NQueensCSP;
import aima.core.util.datastructure.XYLocation;
import freemarker.template.SimpleScalar;

/**
 * @author olj
 *  Denne resursen er knyttet til startsiden i
 *  Sjakkspillet
 */
public class RapporterChessStartServerResourceHTML extends ChessServerResource {

	
	private String delMelding = "delmelding";
	private String meldeTxtId = "melding";
	private String passordCheck = "none";
	private String displayPassord = "passord";
	private String position = "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R";
	private String startPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
	private String tilgjengeligPos = "t";
/*
 * Session keys	
 */
	private String rulesKey = "rules";
	private String rulelabelKey = "rulelabels";
	private String movesKey = "moves";
	private String blackmovesKey = "blackmoves";
	private String ontologyKey = "ontologyfile";
	private String availableKey = "available";
	private List<String> rules;
	private List<String> labels;
	private List<ChessRules> chessRules;
	private List<String> moves;
	private List<String> blackMoves;
	private ChessMoves chessMove;
	private ArrayList<ChessMoves> chessMoves;

	private List<Position> availableMoves = null;
	private List<String>availablePosNames = null;
	
	
	public String getTilgjengeligPos() {
		return tilgjengeligPos;
	}

	public void setTilgjengeligPos(String tilgjengeligPos) {
		this.tilgjengeligPos = tilgjengeligPos;
	}

	public List<String> getAvailablePosNames() {
		return availablePosNames;
	}

	public void setAvailablePosNames(List<String> availablePosNames) {
		this.availablePosNames = availablePosNames;
	}

	public String getAvailableKey() {
		return availableKey;
	}

	public void setAvailableKey(String availableKey) {
		this.availableKey = availableKey;
	}

	public List<Position> getAvailableMoves() {
		return availableMoves;
	}

	public void setAvailableMoves(List<Position> availableMoves) {
		this.availableMoves = availableMoves;
	}

	public String getMovesKey() {
		return movesKey;
	}

	public void setMovesKey(String movesKey) {
		this.movesKey = movesKey;
	}

	public String getBlackmovesKey() {
		return blackmovesKey;
	}

	public void setBlackmovesKey(String blackmovesKey) {
		this.blackmovesKey = blackmovesKey;
	}

	public String getOntologyKey() {
		return ontologyKey;
	}

	public void setOntologyKey(String ontologyKey) {
		this.ontologyKey = ontologyKey;
	}

	public List<String> getBlackMoves() {
		return blackMoves;
	}

	public void setBlackMoves(List<String> blackMoves) {
		this.blackMoves = blackMoves;
	}

	public String getRulesKey() {
		return rulesKey;
	}

	public void setRulesKey(String rulesKey) {
		this.rulesKey = rulesKey;
	}

	public String getRulelabelKey() {
		return rulelabelKey;
	}

	public void setRulelabelKey(String rulelabelKey) {
		this.rulelabelKey = rulelabelKey;
	}

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
	 * establishRules
	 * @param chessBoard
	 * This method puts rules available in the ontology up for display on screen
	 */
	private void establishRules(ChessBoard chessBoard) {
		 rules = chessBoard.getExeRules();
		 labels = chessBoard.getExeLabels();
		 chessRules = chessBoard.getChessRules();


		 if (rules == null || rules.isEmpty()) {
			 rules = new ArrayList<String>();
			 rules.add("No rules avaiable");
		 }
		 if (labels == null || labels.isEmpty()) {
			 labels = new ArrayList<String>();
			 labels.add("No rule");
		 }
	}
	/**
	 * establishMoves
	 * This method creates a new move in the list of moves, it is stored in algebraic notation
	 * @param chessBoard
	 * @param position
	 */
	private void establishMoves(ChessBoard chessBoard,Position position) {
		String move = position.getPositionName();
		String pieceName = position.getUsedBy().getName();
		String pieceType = position.getUsedBy().getPieceName();
		if (!pieceType.equals("P"))
			move = pieceType+move;
		chessMoves = chessBoard.getChessMoves();
		int last = chessMoves.size() ;
		chessMove = chessMoves.get(last-1);
		int moveNr = chessMove.getMoveNr();
		if (pieceName.contains("w")) {
			chessMove = new ChessMoves();
			chessMove.setWhiteMove(move);
			chessMove.setMoveNr(moveNr+1);
			chessMoves.add(chessMove);
		}else {
			chessMove.setBlackMove(move);
		}
		
	}
	/**
	 * getChess
	 * Denne rutinen starter med startside.html
	 * Denne rutinen henter inn nødvendige session objekter og  setter opp nettsiden for å 
	 * vise et sjakkbrett. Den blir utført når nettsiden "Sjakk" starter opp.
	 * @return
	 */
	@Get
	public Representation getChess() {

	     Reference reference = new Reference(getReference(),"..").getTargetRef();
	     Request request = getRequest();
	     Map<String, Object> dataModel = new HashMap<String, Object>();
	
//	     establishMoves();
	     String meldingsText = " ";
	     String fileName = null;
	     SimpleScalar simple = new SimpleScalar(piece);
	     SimpleScalar movedTo = new SimpleScalar(snewPosition);
	     SimpleScalar movedfrom = new SimpleScalar(soldPosition);
	     SimpleScalar chessPosition = new SimpleScalar(startPosition);
//	     SimpleScalar chessMove = new SimpleScalar(moves);
	     ChessBoard chessBoard = new ChessBoard(fileName);
	     String[] legalMoves = {"a3","a4"};
	     String fen = chessBoard.createFen();
	     String popup = "nopopup";
//	     System.out.println(fen);
/*	     moves = chessBoard.getMoves();
	     blackMoves = chessBoard.getBlackMoves();*/
	     dataModel.put(fenPosid,fen);
	     dataModel.put(popupId, popup);
	     establishRules(chessBoard);
	     chessMoves = chessBoard.getChessMoves();
	     sessionAdmin.setSessionObject(request,chessBoard, chessBoardsession);
//	     ChessPiece whitePawn1 = whitePawnpos.getUsedBy();
//	     if (whitePawn1 != null)
//	    	 whitePawn1.setLegalMoves(legalMoves);
		 dataModel.put(pieceId,simple );
		 dataModel.put(newPosId,movedTo );
		 dataModel.put(oldPosId,movedfrom );
		 dataModel.put(displayKey, chessPosition);
		 availablePosNames = new ArrayList<String>();
		 availablePosNames.add("xx");
   	 	dataModel.put(rulesKey,chessRules);
   	 	dataModel.put(movesKey, chessMoves);
   	 	dataModel.put(availableKey, availablePosNames);
 //  	 	dataModel.put(blackmovesKey, blackMoves);
//   	 	dataModel.put(rulelabelKey, labels);
   	 	
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
 	   boolean noMove = false;
    	if(form == null){
    		invalidateSessionobjects();
    	}
/*	     String[] legalMoves = {"a3","a4"};
	     ChessPiece whitePawn1 = new ChessPiece(newPos,"w","wP",legalMoves);
	     whitePawn1.setValue(1);*/
		  String popup = "nopopup";
	      	dataModel.put(popupId, popup);
    	String[] legalMoves = {"",""};
    	ChessPiece chessPiece = null;
    	availablePosNames = null;
    	availableMoves = null;
    	ChessBoard chessBoard = (ChessBoard)sessionAdmin.getSessionObject(request, chessBoardsession);
    	PlayGame game = (PlayGame)sessionAdmin.getSessionObject(request, gameboardSession); // If the game object is null, then the user has not chosen to play a game yet.
	    moves = chessBoard.getMoves();
	    blackMoves = chessBoard.getBlackMoves();
	    chessMoves = chessBoard.getChessMoves();
	    availableMoves = new ArrayList<Position>();
    	Parameter restartGame = form.getFirst("startBtnx"); // Bruker oppgir å starte på nytt
    	Parameter ontology = form.getFirst("ontBtnx"); // User wants ontology position on chessboard
    	Parameter printOntology = form.getFirst("printBtnx"); // User wants ontology printed
    	Parameter reload = form.getFirst("relBtnx"); // User wants to reload ontology from file
    	Parameter query = form.getFirst("qBtnx"); // User wants to query ontology 
    	Parameter fileselect = form.getFirst("fileinput"); // User has selected a file
      	Parameter ontlogyselect = form.getFirst("ontologyinput"); // User has selected an ontology file file
    	Parameter printGame = form.getFirst("printgame"); // User has selected to print the game
    	Parameter eightqueen = form.getFirst("eightqueen"); // User has selected to solve the eight queen problem
    	Parameter achessGame = form.getFirst("playgame"); // User has selected to play a game of chess
    	Parameter findPiece = form.getFirst("btnfind"); // User has moved mouse over a piece
    	if (printGame != null) {
//         	chessBoard.findMoves(gameMoves);
//	     	int nolines = gameMoves.size();
//	     	String mov = (String)gameMoves.get(13)+gameMoves.get(14)+gameMoves.get(15);
			Representation representation = null;
			Response response = getResponse(); 
    		chessMoves = chessBoard.getChessMoves();
    		String pdfFile = "";
    		try {
				pdfFile = printGame(chessMoves);
				representation = new FileRepresentation(pdfFile, MediaType.APPLICATION_PDF); 
			    Disposition disposition = representation.getDisposition();
			    disposition.setType(disposition.TYPE_ATTACHMENT);
			    disposition.setFilename("game" + ".pdf"); 
			    response.setEntity(representation); 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

         	String fen = chessBoard.createFen();
         	System.out.println(fen);
         	dataModel.put(fenPosid,fen);
         	SimpleScalar pieceMoved = new SimpleScalar(piece);
         	SimpleScalar movedTo = new SimpleScalar(newPos);
         	SimpleScalar chessPosition = new SimpleScalar(position);
         	dataModel.put(displayKey, chessPosition);
         	dataModel.put(pieceId,pieceMoved );
   
         	establishRules(chessBoard);
         	//		dataModel.put(pieceId,simple );
         	dataModel.put(rulesKey,chessRules);
         	dataModel.put(movesKey, chessMoves);
         	//  	 	dataModel.put(blackmovesKey, blackMoves);

         	//		 dataModel.put(pawnId,whitePawn1);
         	ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
         	Representation pasientkomplikasjonFtl = clres2.get();
         	templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
         			MediaType.TEXT_HTML);	



         	return templateRep;
    	}
    	if (ontlogyselect != null && !ontlogyselect.getValue().isEmpty()) {// This parameter is always created: Why?!!
       		String fileName = "";
    		String rep = "\\";
    		String newRep = "\\\\";
    		String user = "olj";
    		String newUser = "bruker";
    		System.out.println("ontlogy selected");
    		for (Parameter entry : form) {
    			if (entry.getValue() != null && !(entry.getValue().equals("")) && entry.getName().equals("ontologyinput")){
    					System.out.println(entry.getName() + "=" + entry.getValue());
    					fileName = entry.getValue();
    					fileName = fileName.replace(rep, newRep);
    					fileName = fileName.replace(user, newUser);
    					System.out.println(fileName);
    			}
    		}
    		 sessionAdmin.setSessionObject(request,fileName, ontologyKey);
    		 chessBoard = null;
    		 chessBoard = new ChessBoard(fileName);
    		 sessionAdmin.setSessionObject(request,chessBoard, chessBoardsession);
//    	     legalMoves = {"a3","a4"};
    	     SimpleScalar simple = new SimpleScalar(piece);
    	     SimpleScalar chessPosition = new SimpleScalar(startPosition);
    	     String fen = chessBoard.createFen();
    	     dataModel.put(fenPosid,fen);
    	     establishRules(chessBoard);
    	     chessMoves = chessBoard.getChessMoves();
    		 dataModel.put(pieceId,simple );
    		 dataModel.put(displayKey, chessPosition);

    	     Reference reference = new Reference(getReference(),"..").getTargetRef();
       	 	dataModel.put(rulesKey,chessRules);
       	 	dataModel.put(movesKey, chessMoves);

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
    	if (fileselect != null && !fileselect.getValue().isEmpty()) { // This parameter is always created: Why?!!
    		String fileName = "";
    		String rep = "\\";
    		String newRep = "\\\\";
    		String user = "olj";
    		String newUser = "bruker";
    		System.out.println("fileselect");
    		for (Parameter entry : form) {
    			if (entry.getValue() != null && !(entry.getValue().equals("")) && entry.getName().equals("fileinput")){
    					System.out.println(entry.getName() + "=" + entry.getValue());
    					fileName = entry.getValue();
    					fileName = fileName.replace(rep, newRep);
    					fileName = fileName.replace(user, newUser);
    					System.out.println(fileName);
    			}
    		}
//    		fileName = "C:\\Users\\olj\\Google Drive\\privat\\ontologies\\chessgames\\brill\\brill.pgn";
//    		fileName = "c:\\\\ullern\\\\acem\\\\radio\\\\translist.txt";
//    		chessBoard.getGameFile().setFilePath("c:\\ullern\\acem\\radio\\translist.txt");
       		chessBoard.getGameFile().setFilePath(fileName);
    		chessBoard.getGameFile().createLines();
    		 Stream lines = chessBoard.getGameFile().getLines();
//    		 System.out.println(lines);
   	     	List<String> gameMoves = chessBoard.getGameFile().sendeListe();
   	     	chessBoard.findMoves(gameMoves);
//   	     	int nolines = gameMoves.size();
//   	     	String mov = (String)gameMoves.get(13)+gameMoves.get(14)+gameMoves.get(15);
      	     String fen = chessBoard.createFen();
       	     System.out.println(fen);
       	     dataModel.put(fenPosid,fen);
        	SimpleScalar pieceMoved = new SimpleScalar(piece);
        	SimpleScalar movedTo = new SimpleScalar(newPos);
    	     SimpleScalar chessPosition = new SimpleScalar(position);
    		 dataModel.put(displayKey, chessPosition);
       	 	dataModel.put(pieceId,pieceMoved );
      		establishRules(chessBoard);
//    		dataModel.put(pieceId,simple );
    	 	dataModel.put(rulesKey,chessRules);
    		dataModel.put(movesKey, chessMoves);
//      	 	dataModel.put(blackmovesKey, blackMoves);

     //		 dataModel.put(pawnId,whitePawn1);
    	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
            Representation pasientkomplikasjonFtl = clres2.get();
            templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                    MediaType.TEXT_HTML);	



        	return templateRep;
    	}
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
    		String fileName = null;
   	     	SimpleScalar simple = new SimpleScalar(piece);
   	     	SimpleScalar movedTo = new SimpleScalar(newPos);
   	     	SimpleScalar chessPosition = new SimpleScalar(startPosition);
    		chessBoard = new ChessBoard(fileName);
   	     	moves = chessBoard.getMoves();
   	     	blackMoves = chessBoard.getBlackMoves();
     		String fen = chessBoard.createFen();
     		establishRules(chessBoard);
     		dataModel.put(pieceId,simple );
     		dataModel.put(displayKey, chessPosition);
     		chessBoard.emptyGame();
   		
      	 	dataModel.put(rulesKey,chessRules);
      		dataModel.put(movesKey, chessMoves);
//      	 	dataModel.put(blackmovesKey, blackMoves);
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
    	if (ontology != null){ // User want ontology position on board
    		chessBoard.createChessontlogyPosition();
    		String fen = chessBoard.createFen();

    		System.out.println(fen);
    		dataModel.put(fenPosid,fen);
    		SimpleScalar pieceMoved = new SimpleScalar(piece);
    		SimpleScalar movedTo = new SimpleScalar(newPos);
    		SimpleScalar chessPosition = new SimpleScalar(position);
    		dataModel.put(displayKey, chessPosition);
    		dataModel.put(pieceId,pieceMoved );
    		List<ChessRules> chessRules = chessBoard.getChessRules();
    		dataModel.put(rulesKey,chessRules);
    		dataModel.put(movesKey, chessMoves);
//    		dataModel.put(blackmovesKey, blackMoves);
    		/*      	 	dataModel.put(rulesKey,chessBoard.getExeRules());
       	 	dataModel.put(rulelabelKey, chessBoard.getExeLabels());*/
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
      		establishRules(chessBoard);
     		chessBoard.emptyGame();
//      		dataModel.put(pieceId,simple );
       	 	dataModel.put(rulesKey,chessRules);
       		dataModel.put(movesKey, chessMoves);
//      	 	dataModel.put(blackmovesKey, blackMoves);
    		 dataModel.put(displayKey, chessPosition);
       	 	dataModel.put(pieceId,pieceMoved );
     //		 dataModel.put(pawnId,whitePawn1);
    	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
            Representation pasientkomplikasjonFtl = clres2.get();
            templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                    MediaType.TEXT_HTML);	
        	return templateRep;
    	}
    	if (eightqueen != null) { // User wants to solve the eight queen problem  
//      		chessBoard.clearChessBoard();
      		EightQueenProblem queenProblem = new EightQueenProblem();
      		queenProblem.prepare();
//      		queenProblem.solveProblem();
      		
      		boolean bDone= queenProblem.solvepartProblem();
      		while (!bDone) {
      			bDone = queenProblem.solvepartProblem();
      			System.out.println(queenProblem.getStatistics());
      		}
      	     System.out.println(queenProblem.getStatistics());
      	     List<XYLocation> locations = queenProblem.getEnv().getBoard().getQueenPositions(); 
      	     XYLocation loc = locations.get(0);
      	     NQueensCSP qCSP = queenProblem.getnQueens();
      	     List<Variable> variables = qCSP.getVariables();
      	     Variable var = variables.get(0);
      	     String varName = var.getName();
      	     List<Constraint<Variable,Integer>> constraints = qCSP.getConstraints();
      	     Domain domain = qCSP.getDomain(var);
     	     chessBoard.setPiecetoPosition(queenProblem.getEnv().getBoard(),queenProblem.getnQueens());
     	     String fen = chessBoard.createFen();
      	     System.out.println(fen);
      	     dataModel.put(fenPosid,fen);
       	SimpleScalar pieceMoved = new SimpleScalar(piece);
       	SimpleScalar movedTo = new SimpleScalar(newPos);
   	     SimpleScalar chessPosition = new SimpleScalar(position);
     		establishRules(chessBoard);
    		chessBoard.emptyGame();
//     		dataModel.put(pieceId,simple );
      	 	dataModel.put(rulesKey,chessRules);
      		dataModel.put(movesKey, chessMoves);
//     	 	dataModel.put(blackmovesKey, blackMoves);
   		 dataModel.put(displayKey, chessPosition);
      	 	dataModel.put(pieceId,pieceMoved );
    //		 dataModel.put(pawnId,whitePawn1);
   	    ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
           Representation pasientkomplikasjonFtl = clres2.get();
           templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
                   MediaType.TEXT_HTML);	
       	return templateRep;
    	}
/*
 *     	 User wants to play a game of chess
 */
    	if (achessGame != null) { // User wants to play a game of chess
    		chessBoard.createChessontlogyPosition();
    		if (game == null) {
    			game = new PlayGame(chessBoard.getPositions(),chessBoard); // Creates start position based on ontology start position
    			sessionAdmin.setSessionObject(request,game, gameboardSession);
    		}
  	    	APlayer opponent = game.getGame().getLocalblackPlayer(); //OBS !! Gameplayer is always white!!!
  	    	opponent.calculateOpponentActions(game.getActiveState());   
 // 	    	opponent.calculateOpponentPositions();This call is replaced by the call to calculateOpponentActions
       	    System.out.println("Start positions\n"+game.getGame().getBoardPic());
    	
    		SimpleScalar chessPosition = new SimpleScalar(position);
//    		establishRules(chessBoard);
    		chessBoard.emptyGame();
    		game.proposeMove();
  	    	AgamePiece playerPiece = game.getLastPiece();
   		 	availablePosNames = new ArrayList<String>();
   		 	availablePosNames.add("pl"); // Wants to play a game of chess
   		 	
   	    	piece = playerPiece.getMyPiece().getName();
  	    	snewPosition = game.getNewPosition().getPositionName();
   	    	soldPosition = game.getOldPosition().getPositionName();
   	    	String fen = chessBoard.createFen();
    		return produceTemplate(dataModel,fen,chessBoard);
    	}
    	if (findPiece != null) {   		// User has marked a piece formSubmit('btnfind');
    		collectParameters(form);
        	if (newPos == null || newPos.equals("")){
        		newPos = "a2";
        	}
        	if (oldPos == null || oldPos.equals("y")){
        		oldPos = newPos;
        	}    
        	boolean findTilgjengelig = tilgjengeligPos.equals("til");
       	    chessPiece = chessBoard.findPiece(oldPos,piece);
       	   	AgamePiece movedPiece = chessPiece.getMyPiece();
      	    System.out.println("Piece marked Finding available positions "+findTilgjengelig);
      	    String fen = chessBoard.createFen();
      	   	 if (game != null && findTilgjengelig) { // 
     	    	APlayer opponent = game.getActiveState().getOpponent();
     	    	Position oldPosition = chessBoard.findPostion(oldPos);
     	    	Position newPosition = chessBoard.findPostion(newPos);
        	    	HashMap<String,Position> allMoves =  movedPiece.getLegalmoves();
        	    	Set<String> keySet = allMoves.keySet();
        	    	ArrayList<String> listOfKeys = new ArrayList<String>(keySet);
        	    	ArrayList<Position> allMoveslist = new ArrayList<>(allMoves.values());
        	    	availableMoves.addAll(allMoveslist);
        	    	availablePosNames = new ArrayList<String>();
      	    	for (Position pos:availableMoves) {
      	    		availablePosNames.add(pos.getPositionName());
      	    	}
        	 }
            return produceTemplate(dataModel,fen,chessBoard);
    	}
   // Opponent Player has moved a piece	
    	collectParameters(form); // This call replaces the structure below:
/*    	for (Parameter entry : form) {
			if (entry.getValue() != null && !(entry.getValue().equals(""))){
					System.out.println(entry.getName() + "=" + entry.getValue());
					if (entry.getName().equals("piece"))
						piece = entry.getValue();
					if (entry.getName().equals("posisjon"))
						newPos = entry.getValue();
					if (entry.getName().equals("startposisjon"))
						oldPos = entry.getValue();
					if (entry.getName().equals("tilgjengeligpos"))
						tilgjengeligPos = entry.getValue();
			}
			
    	}*/
    	if (newPos == null || newPos.equals("")){
    		newPos = "a2";
    	}
    	if (oldPos == null || oldPos.equals("y")){
    		oldPos = newPos;
    	}    	
    	String moveText = "Proper move";
    	if (oldPos.equals(newPos)) { // A no move
    		noMove = true;
    		moveText = "No move";
    	}
//    	chessPiece.setName(piece);
/*
 * The position and piece entered by user
 * corresponds to position.positionName and piece.name !!! 
 */

    	boolean findTilgjengelig = tilgjengeligPos.equals("til");
   	    chessPiece = chessBoard.findPiece(oldPos,piece);
   	   	AgamePiece movedPiece = chessPiece.getMyPiece();
   	    String fen = chessBoard.createFen();
   	    System.out.println("Piece name "+chessPiece.getOntlogyName()+" Finding available positions "+findTilgjengelig);
   	    System.out.println("Game and moveflagg "+game+" "+noMove+" "+moveText);
   	    System.out.println(fen);
/*   	    if (noMove) {
   	    	System.out.println(movedPiece.toString());
   	    }*/
   	    
   	    if (game != null && !noMove && !findTilgjengelig) { // Performs the opponent's move and creates the next move

//       	chessPiece.setPosition(newPos);
   	    	APlayer opponent = game.getActiveState().getOpponent();
   	    	Position oldPosition = chessBoard.findPostion(oldPos);
   	    	Position newPosition = chessBoard.findPostion(newPos);
   	    	AgamePiece castle = opponent.checkCastling(movedPiece, newPosition);
   	    	boolean castleCheck = false;
   	    	Position castlePos = null;
   	    	if (castle != null) {
   	    		castle.setCastlingMove(true);
   	    		movedPiece.setCastlingMove(true);
   	    		Position castlePosfrom = castle.getHeldPosition();
   	    		if (castlePosfrom == null)
   	    			castlePosfrom = castle.getMyPosition();
   	    		String fromPos = castlePosfrom.getPositionName();
   	    		castlePos = castleMove(castle, newPosition,opponent,chessBoard);
   	    		String toPos = "";
   	    		String castleName = castle.getName();
   	    		if (castlePos != null) {
   	    			castleCheck = true;
   	    			toPos = castlePos.getPositionName();
  	    			chessBoard.determineMove(fromPos, toPos, castleName); // Determine if move is legal
   	    			castle.setNofMoves(0);
   	    			castle.setMyPosition(castlePos);
   	    			castle.produceLegalmoves(castlePos);
   	    		}
   	    		
   	    	}
   	    	
   	    	HashMap<String,Position> allMoves =  movedPiece.getLegalmoves();
   	    	Set<String> keySet = allMoves.keySet();
   	    	ArrayList<String> listOfKeys = new ArrayList<String>(keySet);
   	    	ArrayList<Position> allMoveslist = new ArrayList<>(allMoves.values());
   	    	availableMoves.addAll(allMoveslist);
   	    	availablePosNames = new ArrayList<String>();
 	    	for (Position pos:availableMoves) {
 	    		availablePosNames.add(pos.getPositionName());
 	    	}

   	    	chessBoard.determineMove(oldPos, newPos, piece); // Determine if move is legal
   	    	if (castle != null) {
   	    		castle.setCastlingMove(false);
   	    		movedPiece.setCastlingMove(false);
   	    	}
   	    	/*
   	    	 * Keeps track of move numbers and the number of moves		
   	    	 */
   	    	movedPiece.setNofMoves(0);

   	    	chessPiece.getMyPiece().setMyPosition(newPosition);
   	    	/*
   	    	 *  New 16.04.20 similar to proposemove
   	    	 */

   	    	/*
   	    	 * Is this correct ??? OLJ 10.07.20 ???? 	    	
   	    	 */
   	    	// 	    	chessPiece.getMyPiece().setHeldPosition(newPosition); // All pieces are now set in correct positions: Should this line be removed ??? OLJ 10.07.20
   	    	newPosition.setUsedandRemoved(chessPiece); // When this call is removed, a piece is removed from the board!!!
 /*
  * end new	    	
  */
// 	    	System.out.println("Old position "+oldPosition.toString()+ " New position "+newPosition.toString());
 	    // setHeldPosition(oldPosition); in this position removes the black pawn in move nr. 3 
 	    	List<ApieceMove>  movesofar = game.getMovements();
 	    	 System.out.println("Moves so far ");
 	    	for (ApieceMove  piecemove:movesofar) {
 	    		System.out.println(piecemove.toString());
 	    		Position topos = piecemove.getToPosition();
 	    		AgamePiece movePiece = piecemove.getPiece();
 	    		Position piecePos = movePiece.getmyPosition();
 	    		Position heldPos = movePiece.getHeldPosition();
 	    		if (topos == heldPos && piecePos != topos) {
 	    			System.out.println(movePiece.toString());
 	    		}
 	    		
 	    	}
 	    	System.out.println("Available positions for "+piece+" before move");
 	    	for (Position pos:allMoveslist) {
 	    		System.out.println(pos.getPositionName());
 	    	}
 	    	for (String akey:listOfKeys) {
 	    		System.out.println(akey);
 	    	}
  	    	game.getGame().movePiece(movedPiece, newPosition,"Opponent");// 16.04.20 Move piece before setting new position.

 // 	    	chessPiece.getMyPiece().setHeldPosition(oldPosition); // Then there are no previous positions to restore from
// setHeldPosition(oldPosition); in this position removes the black pawn in move nr. 2
//   	    	game.getGame().movePiece(oldPosition.getXyloc(),newPosition.getXyloc());
   	    	game.createMove(movedPiece, oldPosition, newPosition); // Creates a new move for the opponent
/*
 * Put the last move to the list of movements for the player  
 * 
 */

   	    	HashMap<String,ApieceMove> myMoves = game.getActiveState().getOpponent().getMyMoves();
   	    	int index = game.getMovements().size();
   	    	ApieceMove lastMove = game.getMovements().get(index-1);
   	    	String moveNot = lastMove.getMoveNotation(); // OBS move notation is not set !!!
 
   	    	lastMove.setMoveNotation(moveNot);
   	    	myMoves.put(moveNot, lastMove);
 	    	Integer noofMoves = new Integer(lastMove.getMoveNumber());
 			movedPiece.getMoveNumbers().add(noofMoves);
  	    	movedPiece.produceLegalmoves(newPosition); // Added 23.06.20 produces new available positions for the moved piece
  	    	movedPiece.giveNewdirections();// Calculates nw,ne,sw,se for bishop and queen Added 03.06.21
 	    	opponent.calculateOpponentActions( game.getActiveState());   
 // 	    	opponent.calculateOpponentPositions(); This call is replaced by the call to calculateOpponentActions
//   	    	game.getActiveState().switchActivePlayer(); // OBS 11.08.20: Must ensure that player now switches to the game player (white) New 16.04.20 After a move, must switch active player
   	    	game.getActiveState().returnMyplayer();
   	    	game.getGame().createNewboard();
   	    	System.out.println(game.getGame().getBoardPic());
//   	    	game.getGame().setChosenPlayer(); // This method is only used at startup OLJ 20.04.20
   	    	
   	    	game.proposeMove(); //The game object proposes the next move for player
   	    	AgamePiece playerPiece = game.getLastPiece();
   	    	piece = playerPiece.getMyPiece().getName();
   	    	snewPosition = game.getNewPosition().getPositionName();
   	    	soldPosition = game.getOldPosition().getPositionName();
     	    fen = chessBoard.createFen();
//   	    System.out.println("Piece name "+chessPiece.getOntlogyName());
     	    System.out.println("moved piece "+piece+" "+fen);
   	    	System.out.println(game.getGame().getBoardPic());
 /*  	    	for (ChessMoves move:chessMoves) {
   	    		System.out.println(move.toString());
   	    	}*/
   	    } // End opponent move
/*   	 if (game != null && findTilgjengelig) { // 
	    	APlayer opponent = game.getActiveState().getOpponent();
	    	Position oldPosition = chessBoard.findPostion(oldPos);
	    	Position newPosition = chessBoard.findPostion(newPos);
   	    	HashMap<String,Position> allMoves =  movedPiece.getLegalmoves();
   	    	Set<String> keySet = allMoves.keySet();
   	    	ArrayList<String> listOfKeys = new ArrayList<String>(keySet);
   	    	ArrayList<Position> allMoveslist = new ArrayList<>(allMoves.values());
   	    	availableMoves.addAll(allMoveslist);
   	    	availablePosNames = new ArrayList<String>();
 	    	for (Position pos:availableMoves) {
 	    		availablePosNames.add(pos.getPositionName());
 	    	}
   	 }*/
   	 if (availablePosNames == null) {
	 	availablePosNames = new ArrayList<String>();
		 	availablePosNames.add("yy");
   	 }
 /*  	 dataModel.put(fenPosid,fen);
   	 SimpleScalar pieceMoved = new SimpleScalar(piece);
   	 SimpleScalar movedTo = new SimpleScalar(snewPosition);
   	 SimpleScalar movedfrom = new SimpleScalar(soldPosition);
   	 SimpleScalar chessPosition = new SimpleScalar(position);
   	 dataModel.put(newPosId,movedTo );
   	 dataModel.put(oldPosId,movedfrom );
   	 dataModel.put(displayKey, chessPosition);
   	 dataModel.put(pieceId,pieceMoved );
   	 establishRules(chessBoard);
   	 //		dataModel.put(pieceId,simple );
   	 dataModel.put(rulesKey,chessRules);
   	 dataModel.put(movesKey, chessMoves);
   	 dataModel.put(availableKey, availablePosNames);
   	 //  	 	dataModel.put(blackmovesKey, blackMoves);

   	 //		 dataModel.put(pawnId,whitePawn1);
   	 ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
   	 Representation pasientkomplikasjonFtl = clres2.get();
   	 templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
   			 MediaType.TEXT_HTML);	
   	 String page = "../chess/test.html";*/
   	 /*         if (noMove) {
        	redirectTemporary(page);
        }*/

        return produceTemplate(dataModel,fen,chessBoard);
    }
    /**
     * collectParameters
     * This method collects all parameters entered by user
     * @param form, the form containing all parameters 
     */
    private void collectParameters(Form form){
    	for (Parameter entry : form) {
    		if (entry.getValue() != null && !(entry.getValue().equals(""))){
    			System.out.println(entry.getName() + "=" + entry.getValue());
    			if (entry.getName().equals("piece"))
    				piece = entry.getValue();
    			if (entry.getName().equals("posisjon"))
    				newPos = entry.getValue();
    			if (entry.getName().equals("startposisjon"))
    				oldPos = entry.getValue();
    			if (entry.getName().equals("tilgjengeligpos"))
    				tilgjengeligPos = entry.getValue();
    		}
    	}
    }
    /**
     * produceTemplate
     * This method produces the template used by Restlet
     * @param dataModel
     * @param fen
     * @param chessBoard
     * @return
     */
    private TemplateRepresentation produceTemplate(Map<String,Object> dataModel,String fen,ChessBoard chessBoard) {
    	TemplateRepresentation  templateRep = null;
      	 dataModel.put(fenPosid,fen);
       	 SimpleScalar pieceMoved = new SimpleScalar(piece);
       	 SimpleScalar movedTo = new SimpleScalar(snewPosition);
       	 SimpleScalar movedfrom = new SimpleScalar(soldPosition);
       	 SimpleScalar chessPosition = new SimpleScalar(position);
       	 dataModel.put(newPosId,movedTo );
       	 dataModel.put(oldPosId,movedfrom );
       	 dataModel.put(displayKey, chessPosition);
       	 dataModel.put(pieceId,pieceMoved );
       	 establishRules(chessBoard);
       	 //		dataModel.put(pieceId,simple );
       	 dataModel.put(rulesKey,chessRules);
       	 dataModel.put(movesKey, chessMoves);
       	 dataModel.put(availableKey, availablePosNames);
       	 //  	 	dataModel.put(blackmovesKey, blackMoves);

       	 //		 dataModel.put(pawnId,whitePawn1);
       	 ClientResource clres2 = new ClientResource(LocalReference.createClapReference(LocalReference.CLAP_CLASS,"/chess/startside.html"));
       	 Representation pasientkomplikasjonFtl = clres2.get();
       	 templateRep = new TemplateRepresentation(pasientkomplikasjonFtl,dataModel,
       			 MediaType.TEXT_HTML);	
    	return templateRep;
    }
    /**
     * castleMove
     * This method checks if at correct castle move has been made
     * @param castle
     * @param newPosition
     * @param opponent
     * @param chessBoard
     * @return POsition correct castleposition or null
     */
    private Position castleMove(AgamePiece castle,Position newPosition,APlayer opponent,ChessBoard chessBoard) {
    	String posName = newPosition.getPositionName();
    	Position castlePos =null;
      	String pieceName = castle.getMyPiece().getOntlogyName();
      	boolean castleCheck = false;
       	System.out.println("Checking castling");
    	if (opponent.getPlayerName() == opponent.getBlackPlayer()) {
        	if (posName.equals("c8") && pieceName.equals("BlackRook1")) {
        		System.out.println("Correct piece "+castle.toString());
        		castlePos = chessBoard.findPostion("d8");
        		castleCheck = true;
        	}
        	if (posName.equals("g8") && pieceName.equals("BlackRook2")) {
        		System.out.println("Correct piece "+castle.toString());
        		castlePos = chessBoard.findPostion("f8");
        		castleCheck = true;
        	}
    	}
       	if (opponent.getPlayerName() == opponent.getWhitePlayer()) {
        	if (posName.equals("c1") && pieceName.equals("WhiteRook1")) {
        		System.out.println("Correct piece "+castle.toString());
        		castlePos = chessBoard.findPostion("d1");
        		castleCheck = true;
        	}
        	if (posName.equals("g1") && pieceName.equals("WhiteRook2")) {
        		System.out.println("Correct piece "+castle.toString());
        		castlePos = chessBoard.findPostion("f1");
        		castleCheck = true;
        	}
       	}
       	return castlePos;
    	
    }
}
