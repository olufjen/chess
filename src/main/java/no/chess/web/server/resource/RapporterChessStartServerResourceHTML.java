package no.chess.web.server.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import no.chess.web.model.game.AgamePiece;

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
/*
 * Session keys	
 */
	private String rulesKey = "rules";
	private String rulelabelKey = "rulelabels";
	private String movesKey = "moves";
	private String blackmovesKey = "blackmoves";
	private String ontologyKey = "ontologyfile";
	
	private List<String> rules;
	private List<String> labels;
	private List<ChessRules> chessRules;
	private List<String> moves;
	private List<String> blackMoves;
	private ChessMoves chessMove;
	private ArrayList<ChessMoves> chessMoves;

	
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
	 * vise et sjakkbrett
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
	     SimpleScalar movedTo = new SimpleScalar(newPos);
	     SimpleScalar chessPosition = new SimpleScalar(startPosition);
//	     SimpleScalar chessMove = new SimpleScalar(moves);
	     ChessBoard chessBoard = new ChessBoard(fileName);
	     String[] legalMoves = {"a3","a4"};
	     String fen = chessBoard.createFen();
//	     System.out.println(fen);
/*	     moves = chessBoard.getMoves();
	     blackMoves = chessBoard.getBlackMoves();*/
	     dataModel.put(fenPosid,fen);
	     establishRules(chessBoard);
	     chessMoves = chessBoard.getChessMoves();
	     sessionAdmin.setSessionObject(request,chessBoard, chessBoardsession);
//	     ChessPiece whitePawn1 = whitePawnpos.getUsedBy();
//	     if (whitePawn1 != null)
//	    	 whitePawn1.setLegalMoves(legalMoves);
		 dataModel.put(pieceId,simple );
		 dataModel.put(displayKey, chessPosition);

		
   	 	dataModel.put(rulesKey,chessRules);
   	 	dataModel.put(movesKey, chessMoves);
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
	    
    	if(form == null){
    		invalidateSessionobjects();
    	}
/*	     String[] legalMoves = {"a3","a4"};
	     ChessPiece whitePawn1 = new ChessPiece(newPos,"w","wP",legalMoves);
	     whitePawn1.setValue(1);*/
    	String[] legalMoves = {"",""};
    	ChessPiece chessPiece = null;
    	ChessBoard chessBoard = (ChessBoard)sessionAdmin.getSessionObject(request, chessBoardsession);
    	PlayGame game = (PlayGame)sessionAdmin.getSessionObject(request, gameboardSession);
	    moves = chessBoard.getMoves();
	    blackMoves = chessBoard.getBlackMoves();
	    chessMoves = chessBoard.getChessMoves();
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
       	    System.out.println("Start positions\n"+game.getGame().getBoardPic());
    		SimpleScalar pieceMoved = new SimpleScalar(piece);
    		SimpleScalar movedTo = new SimpleScalar(newPos);
    		SimpleScalar chessPosition = new SimpleScalar(position);
    		establishRules(chessBoard);
    		chessBoard.emptyGame();
    		game.proposeMove();
      	    String fen = chessBoard.createFen();
//       	    System.out.println("Piece name "+chessPiece.getOntlogyName());
       	    System.out.println(fen);
       	    // End positions:
       	    System.out.println(game.getGame().getBoardPic());
//       	    game.clearChessboard();
 //      	    System.out.println(game.getGame().getBoardPic());
    		dataModel.put(fenPosid,fen);
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
/*
 * The position and piece entered by user
 * corresponds to position.positionName and piece.name !!! 
 */
    	chessPiece = chessBoard.findPiece(oldPos,piece);
//       	chessPiece.setPosition(newPos);
       	Position oldPosition = chessBoard.findPostion(oldPos);
       	Position newPosition = chessBoard.findPostion(newPos);
       	chessBoard.determineMove(oldPos, newPos, piece); // Determine if move is legal

   	    String fen = chessBoard.createFen();
   	    System.out.println("Piece name "+chessPiece.getOntlogyName());
   	    System.out.println(fen);

   	    if (game != null) {
 /*
  *  New 16.04.20 similar to proposemove
  */
 
   	    	AgamePiece movedPiece = chessPiece.getMyPiece();
 	    	chessPiece.getMyPiece().setMyPosition(newPosition);
 	    	chessPiece.getMyPiece().setHeldPosition(newPosition); // All pieces are now set in correct positions
 	    	newPosition.setUsedandRemoved(chessPiece); // When this call is removed, a piece is removed from the board!!!
 /*
  * end new	    	
  */
 	    // setHeldPosition(oldPosition); in this position removes the black pawn in move nr. 3 
  	    	game.getGame().movePiece(movedPiece, newPosition,"Opponent");// 16.04.20 Move piece before setting new position.
 // 	    	chessPiece.getMyPiece().setHeldPosition(oldPosition); // Then there are no previous positions to restore from
// setHeldPosition(oldPosition); in this position removes the black pawn in move nr. 2
//   	    	game.getGame().movePiece(oldPosition.getXyloc(),newPosition.getXyloc());
   	    	game.createMove(movedPiece, oldPosition, newPosition);
   	    	game.getActiveState().switchActivePlayer(); // New 16.04.20 After a move, must switch active player
   	    	game.getGame().createNewboard();
   	    	System.out.println(game.getGame().getBoardPic());
//   	    	game.getGame().setChosenPlayer(); // This method is only used at startup OLJ 20.04.20
   	    	game.proposeMove();
     	    fen = chessBoard.createFen();
//   	    System.out.println("Piece name "+chessPiece.getOntlogyName());
     	    System.out.println(fen);
   	    	System.out.println(game.getGame().getBoardPic());
   	    	for (ChessMoves move:chessMoves) {
   	    		System.out.println(move.toString());
   	    	}
   	    }
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
}
