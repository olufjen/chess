package no.chess.web.model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;







import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import com.hp.hpl.jena.sparql.core.Var;

import no.basis.felles.model.OntologyModel;
import no.basis.felles.model.ParentModel;
import no.basis.felles.semanticweb.chess.BlackBoardPosition;
import no.basis.felles.semanticweb.chess.BlackPiece;
import no.basis.felles.semanticweb.chess.Piece;
import no.basis.felles.semanticweb.chess.WhiteBoardPosition;
import no.basis.felles.semanticweb.chess.WhitePiece;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Derivation;
/**
 * This class represent a chessboard
 * It uses FEN syntax to show board positions
 * @author oluf
 *
 */
public class ChessBoard extends ParentModel {
	private OntologyModel chessModel;
	private List<Individual> individuals;
	private Map<String, List<Statement>> properties;
	private Resource chessGame;
	private HashSet<BlackBoardPosition> blackPositions;
	private HashSet<WhiteBoardPosition> whitePositions;
	private HashSet<BlackPiece> blackPieces;
	private HashSet<WhitePiece> whitePieces;
	
	private Position position;
	private HashMap<String,Position> positions;
	private String startFEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
	// 27.06.17 King and Queen reversed start positions
	private String[] whiteStartpositions = { "a1", "b1", "c1", "d1", "e1",
			"f1", "g1", "h1", "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2" };
	private String[] whiteNames = { "wR", "wN", "wB", "wQ", "wK", "wB", "wN",
			"wR", "wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP" };
	private String[] blackStartpositions = { "a8", "b8", "c8", "d8", "e8",
			"f8", "g8", "h8", "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7" };
	private String[] blackNames = { "bR", "bN", "bB", "bQ", "bK", "bB", "bN",
			"bR", "bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP" };
	private String[] allPositions = { "a1", "b1", "c1", "d1", "e1", "f1", "g1",
			"h1", "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2", "a3", "b3",
			"c3", "d3", "e3", "f3", "g3", "h3", "a4", "b4", "c4", "d4", "e4",
			"f4", "g4", "h4", "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
			"a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6", "a7", "b7", "c7",
			"d7", "e7", "f7", "g7", "h7", "a8", "b8", "c8", "d8", "e8", "f8",
			"g8", "h8" };

	/**
	 * 
	 */
	public ChessBoard() {
		super();
/*
 * From the chess ontology		
 */
		 chessModel  = new OntologyModel();
		 System.out.println("Fetching individuals");
		 individuals = chessModel.getIndividuals();
		 System.out.println("Found individuals");
/*
 * This routines takes a long time !!!		 
 */
//		 properties = chessModel.getProperties(individuals);
// ==================================		 
		 
		 System.out.println("Properties collected");	
//		 chessModel.getGenRulereasoner().getGraphCapabilities().toString();
		 System.out.println("Reasoner capabilities "+chessModel.getGenRulereasoner().getGraphCapabilities().toString());	
/*		 blackPositions = chessModel.getallgivenBlackpositions();
		 whitePositions = chessModel.getallgivenWhitepositions();
		 blackPieces = chessModel.getallgivenBlackpieces();
		 whitePieces = chessModel.getallgivenWhitepieces();*/
		 createStartPosition();
		 
//		 createOntologyposition();
	}
	/**
	 * createStartPosition()
	 * This method creates a startposition without involving ontlogy
	 * It collects all defined positions and all defined pieces from the ontology model (org.semanticweb.owlapi.model.OWLOntology)
	 * They are stored in Hashsets: blackPositions, whitePositions, blackPieces, whitePieces.
	 */
	public void createStartPosition(){
		 blackPositions = chessModel.getallgivenBlackpositions();
		 whitePositions = chessModel.getallgivenWhitepositions();
		 blackPieces = chessModel.getallgivenBlackpieces();
		 whitePieces = chessModel.getallgivenWhitepieces();
		if (positions != null){
			positions = null;
		}
		positions = new HashMap();
		for (int i = 0; i < 16; i++) {
			String[] legalMoves = {};
			ChessPiece whitePiece = new ChessPiece(whiteStartpositions[i], "w",
					whiteNames[i], legalMoves);
			Position position = new Position(whiteStartpositions[i], true,
					whitePiece);
//			position.setChessModel(chessModel);
			positions.put(position.getPositionName(), position);
			// System.out.println("Hvit brikke "+position.getPositionName()+
			// " "+position.getUsedBy().getName());
		}
		for (int i = 0; i < 16; i++) {
			String[] legalMoves = {};
			ChessPiece blackPiece = new ChessPiece(blackStartpositions[i], "b",
					blackNames[i], legalMoves);
			Position position = new Position(blackStartpositions[i], true,
					blackPiece);
//			position.setChessModel(chessModel);
			positions.put(position.getPositionName(), position);
			// System.out.println("Sort brikke "+position.getPositionName()+
			// " "+position.getUsedBy().getName());
		}
		for (int i = 0; i < 32; i++) {
			Position position = new Position(allPositions[i + 16], false, null);
//			position.setChessModel(chessModel);
			positions.put(position.getPositionName(), position);
		}
		createOntologyposition();
	}
	/**
	 * createChessontlogyPosition
	 * This method creates positions on the chessboard given the ontology positions
	 * It is called when user wants the correct ontologypositions on the chessboard.
	 */
	public void createChessontlogyPosition(){
		for (int i = 0; i <64;i++){
			Position position = positions.get(allPositions[i]);
			ChessPiece chessPiece = position.getUsedBy();
			if (chessPiece != null && chessPiece.getOntlogyName() == null){
				position.setUsedBy(null);
				position.setInUse(false);
			}
			if (chessPiece != null && chessPiece.getOntlogyName() != null){
				System.out.println("Chessontology: Name of piece: "+chessPiece.getName()+" Name of chess piece: "+chessPiece.getPieceName()+" "+chessPiece.getOntlogyName());
			}
		}
	}
	/**
	 * createOntologyposition
	 * This method creates the chessboard positions given the chess ontology.
	 * It uses the HashSets: blackPositions, whitePositions, blackPieces, whitePieces, to find which piece occupies which position in the 
	 * ontology, and places this information in the correct position of the board.
	 * It is called when the chessBoard is created.
	 */
	public void createOntologyposition(){

		Iterator<WhiteBoardPosition> whitePosIterator =  whitePositions.iterator();
	      while(whitePosIterator.hasNext()){
	    	  WhiteBoardPosition whitePos = whitePosIterator.next();
	    	  IRI ir = whitePos.getOwlIndividual().getIRI();

	    	  HashSet<Piece> pieces =  (HashSet<Piece>)whitePos.getIsOccupiedBy();
	    	  String irs = ir.toString();
	    	  OWLNamedIndividual wp = whitePos.getOwlIndividual();
	    	  char sep = '#';
	    	  String name = extractString(irs, sep,-1);
//	    	  System.out.println(irs+" "+whitePos.toString()+ " "+ wp.toString()+ " "+name);
	    	  Position position = positions.get(name);
	    	  if (position != null){
	    		  position.setWhiteBoardPosition(whitePos);
	    		  position.setPieces(pieces);
	    	  }
	      }
			Iterator<BlackBoardPosition> blackPosIterator =  blackPositions.iterator();
		      while(blackPosIterator.hasNext()){
		    	  BlackBoardPosition blackPos = blackPosIterator.next();
		    	  IRI ir = blackPos.getOwlIndividual().getIRI();

		    	  HashSet<Piece> pieces =  (HashSet<Piece>)blackPos.getIsOccupiedBy();
		    	  String irs = ir.toString();
		    	  OWLNamedIndividual wp = blackPos.getOwlIndividual();
		    	  char sep = '#';
		    	  String name = extractString(irs, sep,-1);
//		    	  System.out.println(irs+" "+blackPos.toString()+ " "+ wp.toString()+ " "+name);
		    	  Position position = positions.get(name);
		    	  if (position != null){
		    		  position.setBlackBoardPosition(blackPos);
		    		  position.setPieces(pieces);
		    	  }
		      }	      
	}
	public void queryOntology(){
		String prefix = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX owl: <http://www.w3.org/2002/07/owl#>"
				+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
				+ " PREFIX ont: <http://www.co-ode.org/ontologies/ont.owl#> ";
//		String q = prefix + "\n SELECT ?subject ?object	WHERE { ?subject ont:isOccupiedBy ?object. }";
		String q = prefix + "\n DESCRIBE ont:BoardPosition";
		List<QuerySolution> variables = chessModel.queryOntology(q);
		for (int i=0;i<variables.size();i++){
			QuerySolution qsol = variables.get(i);
			Resource subjectres = qsol.getResource("subject");
			Resource objectres = qsol.getResource("object");
	        System.out.println("Subject: "+subjectres.toString()+" "+subjectres.getLocalName()+" Object: "+objectres.toString()+" "+objectres.getLocalName());
		}
		if (chessModel.getResults() != null)
			ResultSetFormatter.out(chessModel.getResults());
		StmtIterator stm = chessModel.getInfModel().listStatements();
		PrintWriter out = new PrintWriter(System.out);
		while (stm.hasNext()){
		    Statement s = stm.nextStatement();
		   
		    for (Iterator id = chessModel.getInfModel().getDerivation(s); id.hasNext(); ) {
		        Derivation deriv = (Derivation) id.next();
		        System.out.println("Statement is " + s);
		        deriv.printTrace(out, true);
		    }
		}
	}
	public void printOntology(){
		chessModel.printOntology();
	}
	public OntologyModel getChessModel() {
		return chessModel;
	}
	public void setChessModel(OntologyModel chessModel) {
		this.chessModel = chessModel;
	}
	public List<Individual> getIndividuals() {
		return individuals;
	}
	public void setIndividuals(List<Individual> individuals) {
		this.individuals = individuals;
	}
	public Map<String, List<Statement>> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, List<Statement>> properties) {
		this.properties = properties;
	}
	public Resource getChessGame() {
		return chessGame;
	}
	public void setChessGame(Resource chessGame) {
		this.chessGame = chessGame;
	}
	public String getStartFEN() {
		return startFEN;
	}

	public void setStartFEN(String startFEN) {
		this.startFEN = startFEN;
	}

	public HashMap getPositions() {
		return positions;
	}

	public void setPositions(HashMap positions) {
		this.positions = positions;
	}

	public String[] getAllPositions() {
		return allPositions;
	}

	public void setAllPositions(String[] allPositions) {
		this.allPositions = allPositions;
	}
	/**
	 * produceFen
	 * This function produce the correct FEN string given a board representation
	 * @param boardRow
	 * @return
	 */
	private String produceFen(String[][] boardRow){
		String fen = "";
		for (int i = 0;i<8;i++){
			for (int j = 0;j<8;j++){
				if (boardRow[i][j].equals("f")){
					fen = fen + "1";
				}
				else{
					fen =  fen + boardRow[i][j];
				}
			}
			if (i != 7)
				fen = fen + "/";
//			System.out.println("Fen "+fen);
		}

		return fen;
	}
	/**
	 * createFen() This routine creates a FEN representation of a chessboard
	 * It makes use of the private function produceFen to return the correct FEN string
	 * @return The FEN representation
	 */
	public String createFen(){

		String pieceName = "";
		String[][] boardRow = new String[8][8];
		int fenCount = 63; // 0? 7?
		for (int i = 0;i<8;i++){
			for (int j = 7;j>=0;j--){
				Position position = (Position)positions.get(allPositions[fenCount]);
				if (position.isInUse()){
					pieceName = position.getUsedBy().getName().substring(1);
					String color = position.getUsedBy().getColor();
					if (color.equals("b"))
						pieceName = pieceName.toLowerCase();
					boardRow[i][j] = pieceName;
				}else{
					boardRow[i][j] = "f";
				}

				String cp = "ledig";
				if (position.isInUse()){
					cp = position.getUsedBy().getName();
				}
//				System.out.println(position.getPositionName()+ " "+cp+ " "+boardRow[i][j]+" "+fenCount+ " "+i+" "+j );
				fenCount--;
			}

		}
		return produceFen(boardRow);
//		return startFEN;
	}

	public ChessPiece findPiece(String posName, String pieceName) {
		String localposName = "";
		ChessPiece piece = null;
		for (int i = 0; i < 63; i++) {
			Position position = (Position) positions.get(allPositions[i]);
			localposName = position.getPositionName();
			if (posName.equals(localposName)) {
				piece = position.getUsedBy();
				break;
			}
		}
		return piece;

	}

	public Position findPostion(String pos) {
		Position localPosition = null;
		localPosition = (Position) positions.get(pos);
		return localPosition;
	}

	public boolean checkPosition(String pos) {
		boolean inUse = false;
		for (int i = 0; i < 63; i++) {
			Position position = (Position) positions.get(allPositions[i]);
			if (position.getPositionName().equals(pos)) {
				if (position.isInUse()) {
					inUse = true;
					break;
				}
			}
		}
		return inUse;
	}

}
