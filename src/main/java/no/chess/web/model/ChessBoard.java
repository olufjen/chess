package no.chess.web.model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.protege.owl.codegeneration.WrappedIndividual;
import org.restlet.Request;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.values.SQWRLResultValue;

import com.hp.hpl.jena.sparql.core.Var;

import aima.core.environment.nqueens.NQueensBoard;
import aima.core.search.csp.Domain;
import aima.core.search.csp.Variable;
import aima.core.search.csp.examples.NQueensCSP;
import aima.core.util.datastructure.XYLocation;
import no.basic.ontology.control.OntologyContainer;
import no.basic.ontology.model.FileModel;
import no.basic.ontology.model.OntologyModel;
import no.basic.ontology.model.ParentModel;
/*import no.basis.felles.semanticweb.chess.BlackBoardPosition;
import no.basis.felles.semanticweb.chess.BlackPiece;
import no.basis.felles.semanticweb.chess.Piece;
import no.basis.felles.semanticweb.chess.WhiteBoardPosition;
import no.basis.felles.semanticweb.chess.WhitePiece;*/
import no.chess.ontology.BlackBoardPosition;
import no.chess.ontology.BlackPiece;
import no.chess.ontology.BoardPosition;
import no.chess.ontology.ChessPosition;
import no.chess.ontology.Entity;
import no.chess.ontology.BFObject;
import no.chess.ontology.Piece;
import no.chess.ontology.Taken;
import no.chess.ontology.Vacant;
import no.chess.ontology.WhiteBoardPosition;
import no.chess.ontology.WhitePiece;
//import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;
import no.chess.ontology.impl.DefaultChessPiece;
import no.games.chess.AbstractGamePiece.pieceType;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Derivation;
/**
 * This class represent a front end chessboard as presented to the user
 * It also contains a chosen chess ontology model
 * It uses FEN syntax to show board positions
 * @author oluf
 *  
 */
/**
 * @author bruker
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
	private HashSet<Taken> allTakenPositions;
	private HashSet<Vacant> allVacantPositions;	
	private HashSet<ChessPosition> allChessPositions;
	private NodeSet<OWLClass> allClasses;
	private Set<OWLClass> allSetClasses;
	private List<OWLClass> allOntclasses;
	private ArrayList<String> exeRules;
	private ArrayList<String> exeLabels;
	private ChessRules chessRule;
	private ArrayList<ChessRules> chessRules;
	private Position position;
	private OntologyContainer modelContainer;
	private List<String> moves;
	private List<String> blackMoves;
	private ArrayList<ChessMoves> chessMoves;
	private ChessMoves chessMove;
	private FileModel gameFile;
	private String algebraicNotation = null; // contains the algebraic notation of the latest move
	private String ontologyKey = "ontologyfile";
	
	private boolean opposingOccupied = false; // True when a  piece move to an occupied position
	private boolean castling = false; // true when castling takes place
	/**
	 * positions contains all available board positions and information on whether they are occupied
	 */
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
	public ChessBoard(String fileName) {
		super();
/*
 * From the chess ontology		
 */

		if (fileName == null) {
			 chessModel  = new OntologyModel();

		}else
			chessModel = new OntologyModel(fileName);
		 gameFile = new FileModel();
		 System.out.println("Fetching individuals removed");
//		 individuals = chessModel.getIndividuals(); // These individuals are not used. OLJ 30.08.18
//		 System.out.println("Found individuals");
//		 chessModel.getOntindividuals();
		 chessRules = new ArrayList();
		 chessRule = new ChessRules();
		 chessRules.add(chessRule);
		 chessMove = new ChessMoves();
		 chessMoves = new ArrayList();
		 chessMoves.add(chessMove);
/*
 * This routines takes a long time !!!		 
 */
//		 properties = chessModel.getProperties(individuals);
// ==================================		 
		 
//		 System.out.println("Properties collected");	
//		 chessModel.getGenRulereasoner().getGraphCapabilities().toString();
		 System.out.println("Reasoner capabilities "+chessModel.getGenRulereasoner().getGraphCapabilities().toString());	
		 System.out.println("Clarkpellet reasoner for ontModel "+chessModel.getClarkpelletReasoner().toString());	
		 modelContainer = chessModel.getModelContainer();
		 createStartPosition();
	     moves = new ArrayList<String>();
	     blackMoves = new ArrayList<String>();
//		 createOntologyposition();
	}
	

	public String getAlgebraicNotation() {
		return algebraicNotation;
	}


	public void setAlgebraicNotation(String algebraicNotation) {
		this.algebraicNotation = algebraicNotation;
	}


	public FileModel getGameFile() {
		return gameFile;
	}


	public void setGameFile(FileModel gameFile) {
		this.gameFile = gameFile;
	}


	public ArrayList<ChessMoves> getChessMoves() {
		return chessMoves;
	}


	public void setChessMoves(ArrayList<ChessMoves> chessMoves) {
		this.chessMoves = chessMoves;
	}


	public ChessMoves getChessMove() {
		return chessMove;
	}


	public void setChessMove(ChessMoves chessMove) {
		this.chessMove = chessMove;
	}


	public ChessRules getChessRule() {
		return chessRule;
	}


	public List<String> getBlackMoves() {
		return blackMoves;
	}


	public void setBlackMoves(List<String> blackMoves) {
		this.blackMoves = blackMoves;
	}


	public List<String> getMoves() {
		return moves;
	}


	public void setMoves(List<String> moves) {
		this.moves = moves;
	}


	public void setChessRule(ChessRules chessRule) {
		this.chessRule = chessRule;
	}


	public ArrayList<ChessRules> getChessRules() {
		return chessRules;
	}


	public void setChessRules(ArrayList<ChessRules> chessRules) {
		this.chessRules = chessRules;
	}


	public ArrayList<String> getExeRules() {
		return exeRules;
	}

	public void setExeRules(ArrayList<String> exeRules) {
		this.exeRules = exeRules;
	}

	public ArrayList<String> getExeLabels() {
		return exeLabels;
	}

	public void setExeLabels(ArrayList<String> exeLabels) {
		this.exeLabels = exeLabels;
	}
	
	/**
	 * emptyGame
	 * This method clears the list of moves from the board
	 */
	public void emptyGame() {
		chessMoves.clear();
		chessMoves.add(chessMove);
	}
	/**
	 * establishMoves
	 * This method creates a new move in the list of moves, it is stored in algebraic notation
	 * @since 26.03.21 enabled for castling
	 * @param chessBoard
	 * @param position The new position
	 */
	private void establishMoves(Position position,String oldName) {
		String move = position.getPositionName();
		String stroke = "";
		String startPos = "";
		String pieceName = position.getUsedBy().getName();
		if (castling) {
			algebraicNotation = "o-o";
			move = algebraicNotation;
			castling = false;
			chessMoveAdm(pieceName,move);
			return;
		}
		if (opposingOccupied) {
			stroke = "x";
			startPos = oldName;
		}

		String pieceType = position.getUsedBy().getPieceName();
		if (!pieceType.equals("P"))
			move = pieceType+stroke+move;
		else
			move = startPos+stroke+move;
		chessMoveAdm(pieceName,move);
		
/*		chessMoves = getChessMoves();
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
		}*/
		algebraicNotation = move; // contains the algebraic notation of the latest move
	}
	private void chessMoveAdm(String pieceName,String move) {
		chessMoves = getChessMoves();
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
	 * findMoves
	 * This method runs through a list of moves read from a pgn file of chessmoves, and
	 * creates individual moves in Algebraic notation.
	 * @param moveLines a list of moves 
	 */
	public void findMoves(List<String>moveLines) {
		int wInd = -1;
		boolean endGame = false;boolean moveCount = false;
		for (String moveLine : moveLines) {
			if (!moveLine.startsWith("[")) {
				int len = moveLine.length();
				int firstInd = 0;
				int subind = 1;int subinx = 0;
				if (moveCount)
					subind = 2;
				firstInd = moveLine.indexOf(".");
				do {
					int secondInn = moveLine.indexOf(".", firstInd+3);
					if (secondInn < 0) {
						secondInn = len +2;
					}
					String moveNr = moveLine.substring(firstInd-subind, firstInd-subinx);
					String move = moveLine.substring(firstInd+1, secondInn-2);
					String wMove = "";
					String bMove = "";
					move = move.trim();
					wInd = move.indexOf(" ") + 1;
					if (wInd > 0) {
						wMove = move.substring(0,wInd);
						bMove = move.substring(wInd);
						int mv = Integer.parseInt(moveNr);
						System.out.println("Moves: "+moveNr+ wMove+" "+bMove);
						firstInd = secondInn+1;subind = 2;subinx = 1;
						if (mv >= 9) {
							subind = 3;
							moveCount = true;
						}
						ChessMoves chessMove = new ChessMoves(wMove,bMove,mv);
						chessMoves.add(chessMove);
					}
				endGame = move.contains("1-0");	
				}while (firstInd < len);
				if (endGame)
					break;
			}

			
		}
	}
	/**
	 * clearChessBoard
	 * This method creates an empty chessboard
	 */
	public void clearChessBoard() {
		for (int i = 0; i <64;i++){
			Position position = positions.get(allPositions[i]);
			ChessPiece piece = position.getUsedBy();
//			if (piece != null && !piece.getOntlogyName().equals("WhiteQueen") && !piece.getOntlogyName().equals("BlackQueen") ) {
				position.setUsedBy();
//				position.setInUse(false);
				System.out.println("Clearchessboard: Setting position empty:  "+position.getPositionName());
//			}


		}
			

	}
	/**
	 * createStartPosition()
	 * This method creates a startposition without involving ontology
	 * It collects all defined positions and all defined pieces from the ontology model (org.semanticweb.owlapi.model.OWLOntology)
	 * They are stored in Hashsets: blackPositions, whitePositions, blackPieces, whitePieces.
	 */
	public void createStartPosition(){
/*		 blackPositions = chessModel.getallgivenBlackpositions();
		 whitePositions = chessModel.getallgivenWhitepositions();
		 blackPieces = chessModel.getallgivenBlackpieces();
		 whitePieces = chessModel.getallgivenWhitepieces();
		 allTakenPositions = chessModel.getAllTakenPositions();
		 allVacantPositions = chessModel.getAllVacantPositions();
		 allChessPositions = chessModel.getAllChessPositions();*/
		 
		 blackPositions = modelContainer.getallgivenBlackpositions();
		 whitePositions = modelContainer.getallgivenWhitepositions();
		 blackPieces = modelContainer.getallgivenBlackpieces();
		 whitePieces = modelContainer.getallgivenWhitepieces();
		 allTakenPositions = modelContainer.getAllTakenPositions();
		 allVacantPositions = modelContainer.getAllVacantPositions();
		 allChessPositions = modelContainer.getAllChessPositions();
		 allSetClasses = modelContainer.getAllClasses();
//		 allClasses = (NodeSet) modelContainer.getAllClasses(); // Renders class cast exception
		 allOntclasses = new ArrayList<OWLClass>(allSetClasses);
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
     * listSWRLRules
     * This method produces available rules as an arraylist of strings, so that they can be run as rules 
     * with the SQWRLQueryEngine queryEngine
     * @param ontology
     */
    public  void listSWRLRules(OWLOntology ontology) {
        //OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
    	exeRules = new ArrayList();
    	exeLabels = new ArrayList();

        for (SWRLRule rule : ontology.getAxioms(AxiomType.SWRL_RULE)) { 
        	String ruleAnnotation = null;
        	String completeRule = null;
        	String concequence = "";
        	String antecedent = "";
        	Set<SWRLAtom>bodies =  rule.getBody();
        	
        	Set<OWLAnnotation>annotations = rule.getAnnotations();
        	for (OWLAnnotation annotation : annotations) {
        		Set<OWLAnnotation>ruleannotations = annotation.getAnnotations();
        		
        		for (OWLAnnotation anno : ruleannotations) {
        			System.out.println("Rule annotation inside: "+anno.toString()); //Empty
        		}
        		ruleAnnotation = annotation.toString();
        		if (ruleAnnotation.contains("label"))
        				ruleAnnotation = createRuleLabel(ruleAnnotation);
//        		System.out.println("Rule annotation: "+annotation.toString()+" Label: "+ruleAnnotation);

        	}
        	Set<SWRLAtom>heads =  rule.getHead();
        	for (SWRLAtom head : heads) {
        		SWRLPredicate pred = head.getPredicate();
        	
        		String ruleString = createRuleString(pred.toString(),head.toString());
//        		System.out.println("Rulehead: "+head.toString()+" Predicate: "+pred.toString()+ " Rulestring: "+ruleString);
        		if (!concequence.isEmpty())
        			concequence = concequence + "^" + ruleString;
        		else
        			concequence = ruleString;
        	}
        	for (SWRLAtom body : bodies) {
        		SWRLPredicate pred = body.getPredicate();
        		String ruleString = createRuleString(pred.toString(),body.toString());
//        		System.out.println("Rulebody: "+body.toString()+" Predicate "+pred.toString()+ " Rulestring: "+ruleString);
        		if (!antecedent.isEmpty())
        			antecedent = antecedent + "^"+ruleString;
        		else
        			antecedent = ruleString;
        	}
        	completeRule = antecedent + "->" + concequence;
   		 	chessRule = new ChessRules();
        	exeRules.add(completeRule);
        	exeLabels.add(ruleAnnotation);
        	chessRule.setExeLabels(ruleAnnotation);
        	chessRule.setExeRules(completeRule);
        	chessRules.add(chessRule);
        	
         //   System.out.println("Rule : "+renderer.render(rule)); 
        }
//        System.out.println("Ruleset : "+exeRules+ " Labels "+exeLabels);
        int pos = 0;
        for (String label:exeLabels) {
        	if (label.equals("takenpos"))
        		break;
        	pos++;
        }
        SQWRLQueryEngine queryEngine = chessModel.getQueryEngine();
        SQWRLResult result = null;
        try {
			result = queryEngine.runSQWRLQuery(exeLabels.get(pos), exeRules.get(pos));
		} catch (SQWRLException | SWRLParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			if (result != null) {
//				System.out.println(exeLabels.get(pos) + ": " + result.toString());
				int nRows = result.getNumberOfRows();
				int nC = result.getNumberOfColumns();
				for (int i=0;i<nRows;i++) {
					SQWRLResultValue resValue = result.getValue(0, i);
					SQWRLResultValue resValue2 = result.getValue(1, i);
//					System.out.println(exeLabels.get(pos) + " row number:"+ i+" " + resValue.toString()+" "+resValue2.toString());
					
				}

			}
		} catch (SQWRLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    } 
	private void reasonWith() {
		OWLDataFactory factory = chessModel.getOwlDatafactory();
//		PrefixOWLOntologyFormat pm = chessModel.getPm();
//		OWLReasoner owlReasoner = chessModel.getOwlReasoner();
		PelletReasoner pelletReasoner = chessModel.getClarkpelletReasoner();
//		OWLObjectRenderer renderer = chessModel.getOwlRenderer();
		OWLOntology ontology = chessModel.getOntModel();//
//		OWLOntology model = chessModel.getModel();
//		OWLClass pieceClass = factory.getOWLClass(":Piece", pm);
		//Prøve med model individuals !!!!
		Position position = positions.get(allPositions[0]);
		HashSet<Piece> pieces = position.getPieces();
		Piece ontologyPiece = null;
		if(pieces != null) {
			for (Piece ontPiece :pieces) {
				ontologyPiece = ontPiece;
				OWLNamedIndividual piece = ontologyPiece.getOwlIndividual();
//				System.out.println("Reasoning: Pieces from generated classes Named ind: "+piece.toString()+" ontology piece: "+ontologyPiece.toString());
			}
		}
		listSWRLRules(ontology);
//		System.out.println("Reasoning: Piece owl class: "+ pieceClass.asOWLClass().toString());
//		HashSet<OWLNamedIndividual> namedpieces = (HashSet<OWLNamedIndividual>) owlReasoner.getInstances(pieceClass, false).getFlattened();
/*		for (OWLNamedIndividual piece :namedpieces ){
			System.out.println("Reasoning: Pieces: "+piece.toString()+" Class " +renderer.render(piece)); // Does not work HashSet is empty!!!!???
		}*/
//		OWLObjectProperty occupies = factory.getOWLObjectProperty(":occupies", pm);
//		factory.get
//		System.out.println("Reasoning: Property "+occupies.toString());
//		OWLNamedIndividual blackKing = factory.getOWLNamedIndividual(":BlackKing", pm);
//		System.out.println("Reasoning: Black King named ind: "+blackKing.asOWLNamedIndividual().toString()+" Top entity "+blackKing.isTopEntity()+" Bottom entity "+blackKing.isBottomEntity());
//		Map<OWLObjectPropertyExpression,Set<OWLIndividual>> kingValues = blackKing.get
		
//		System.out.println("Reasoning: Black King "+blackKing.toString()+" rendering "+renderer.render(blackKing) );
//		HashSet<OWLNamedIndividual> individuals = (HashSet<OWLNamedIndividual>) owlReasoner.getObjectPropertyValues(blackKing, occupies).getFlattened();
//		owlReasoner.get
//		HashSet<OWLNamedIndividual> pelletindividuals = (HashSet<OWLNamedIndividual>) pelletReasoner.getObjectPropertyValues(blackKing, occupies).getFlattened();
/*		for (OWLNamedIndividual ind : individuals)
		{
			System.out.println(blackKing.toStringID()+" occupies "+renderer.render(ind));// Does not work HashSet is empty!!!!???
		}*/
//		Set<OWLClassExpression> assertedClasses = blackKing.getTypes(ontology);
//		HashSet<OWLClass> classes = (HashSet<OWLClass>) owlReasoner.getTypes(blackKing, false).getFlattened(); // Does not work HashSet is empty!!!!???
/*		for (OWLClass c: classes) {
			boolean asserted = false;
			System.out.println((asserted ? "Reasoning: asserted ":"Reasoning: inferred ")+ " class for Black King "+renderer.render(c) );
		}*/
		
	}
	/**
	 * createChessontlogyPosition
	 * This method creates positions on the chessboard given the ontology positions
	 * It is called when user wants the correct ontologypositions on the chessboard.
	 * OLJ 06.05.19 : Must be reworked when eightqueen problem has been run
	 * @since November 2020
	 * Adding predicates to pieces and positions
	 */
	public void createChessontlogyPosition(){
		for (int i = 0; i <64;i++){
			Position position = positions.get(allPositions[i]);
			ChessPiece chessPiece = position.getUsedBy();
			HashSet<Piece> pieces = position.getPieces();
			BlackBoardPosition blackPos = position.getBlackBoardPosition();
			WhiteBoardPosition whitePos = position.getWhiteBoardPosition();
			if (blackPos != null) {
		    	 String posPred = blackPos.toString();
		    	 String fposPred = extractString(posPred, '(',-1);
		    	 String endPospred = extractString(fposPred, ';',0);
		    	 position.setPredicate(endPospred);
			}
			if (whitePos != null) {
		    	 String posPred = whitePos.toString();
		    	 String fposPred = extractString(posPred, '(',-1);
		    	 String endPospred = extractString(fposPred, ';',0);
		    	 position.setPredicate(endPospred);
			}
			Piece ontologyPiece = null;
			if(pieces != null) {
				for (Piece ontPiece :pieces) {
					ontologyPiece = ontPiece;
				}
			}
/*			if (chessPiece == null) {
				
			}*/
//			boolean ischessPiece = chessPiece.getClass().equals(Entity.class);
			if (ontologyPiece != null) {
				Collection<? extends Object> nn = ontologyPiece.getHasName(); //This collection is a HashSet of strings
				HashSet<String> nnx = (HashSet<String>) ontologyPiece.getHasName();
				System.out.println(" An ontology piece: Name of piece: "+ontologyPiece.toString()+" ont piece name "+nnx.toString());
		    	 String pred = ontologyPiece.toString();
	
		    	 String firstPred = extractString(pred, '(',-1);
		    	 String endPred = extractString(firstPred, ';',0);
		    	 chessPiece.setPredicate(endPred);
//				ontologyPiece.getOwlIndividual()
			}
			String ontName = "Unknown";
			if (chessPiece != null && chessPiece.getOntlogyName() != null)
				ontName = chessPiece.getOntlogyName();
			if (position.getPositionName() == null)
				position.setPositionName("xx");
			System.out.println("Position "+position.getPositionName()+" Chessontology: Name of piece: "+chessPiece.getName()+" Name of chess piece: "+chessPiece.getPieceName()+" "+ontName);

			if (ontologyPiece == null){
				position.setUsedBy();
//				position.setInUse(false);
				System.out.println("Setting position empty:  "+position.getPositionName());
			}
		
/*			if (chessPiece != null && chessPiece.getOntlogyName() != null){
				System.out.println("Chessontology: Name of piece: "+chessPiece.getName()+" Name of chess piece: "+chessPiece.getPieceName()+" "+chessPiece.getOntlogyName());
			}*/
		}

	}
	/**
	 * createOntologyposition
	 * This method creates the chessboard positions given the chess ontology.
	 * It uses the HashSets: blackPositions, whitePositions, blackPieces, whitePieces, to find which piece occupies which position in the 
	 * ontology, and places this information in the correct position of the board.
	 * It is called from createStartPosition, when the chessBoard is created.
	 */
	public void createOntologyposition(){
		for (ChessPosition chessPos: allChessPositions) {
//			HashSet<String> names =  (HashSet<String>) chessPos.getHasName(); 
//			HashSet<Vacant> vacants = (HashSet<Vacant>) chessPos.getIsVacant();
//			OWLNamedIndividual individual = chessPos.getOwlIndividual();
/*			String posx = null;
			for (String pos : names) {
				posx = pos;
				break;
			}*/
//			System.out.println("createOntologyposition: Chess positions: "+names.toString()+" "+posx);
//			System.out.println("Chess vacant positions: "+vacants.toString()+" ");
		}

		for (BlackPiece blackPiece : blackPieces) {
			HashSet<Taken> taken = (HashSet<Taken>) blackPiece.getOccupies();
			IRI irp = blackPiece.getOwlIndividual().getIRI();

			String irpiece = irp.toString();
			char sepp = '#';
	    	String piecename = extractString(irpiece, sepp,-1);
//			System.out.println("createOntologyposition: Black piece: "+irpiece+" "+piecename);
			 HashSet<Piece> pieces = new HashSet<Piece>();
			for (Taken takenPos : taken) {
				boolean whitePos = false;
				boolean blackPos = false;
				IRI ir = takenPos.getOwlIndividual().getIRI();
				HashSet<WrappedIndividual> individuals =  (HashSet<WrappedIndividual>)( takenPos).getIsOccupiedBy();
				Piece piece = (Piece)blackPiece;
				 pieces.add(piece); 
		    	 
		    	 for ( WrappedIndividual individual: individuals) {
		    		 boolean ispiece = individual.getClass().equals(Entity.class);
//		    		 Piece piece = (Piece)individual;
//		    		 Entity ent = (Entity)individual;
//		    		 System.out.println("createOntologyposition: individual Piece: "+individual.toString()+ " class "+ispiece);
//		    		 pieces.add(piece); 
		    	 }
		    	 String irs = ir.toString();
		    	 OWLNamedIndividual wp = takenPos.getOwlIndividual();
		    	 char sep = '#';
		    	 String name = extractString(irs, sep,-1);
		    	 String pred = blackPiece.toString();
		    	 String posPred = takenPos.toString();
		    	 String firstPred = extractString(pred, '(',-1);
		    	 String endPred = extractString(firstPred, ';',0);
		    	 String fposPred = extractString(posPred, '(',-1);
		    	 String endPospred = extractString(fposPred, ';',0);
		    	 System.out.println("createOntologyposition: Piece.tostring "+blackPiece.toString());
		    	 System.out.println("createOntologyposition: Taken position for black piece: "+irs+" "+takenPos.toString()+ " "+ wp.toString()+ " "+name);
		    	 allTakenPositions.add(takenPos);
//		    	 HashSet<BoardPosition> parts = (HashSet<BoardPosition>) takenPos.getIsPartOf();
		    	 BoardPosition boardPosition = (BoardPosition)takenPos;
	    		 blackPos = boardPosition.getClass().equals(BlackBoardPosition.class);
	    		 whitePos = boardPosition.getClass().equals(WhiteBoardPosition.class);
		    	 Position position = positions.get(name);
		    	 if (position != null){
		    		 if (blackPos)
		    			 position.setBlackBoardPosition((BlackBoardPosition)boardPosition);
		    		 if (whitePos)
		    			 position.setWhiteBoardPosition((WhiteBoardPosition)boardPosition);
		    		 //			    		  position.setPieces(pieces);
//		    		 System.out.println("createOntologyposition: Found a position for black pieces "+name+" Position "+boardPosition.toString());
		    		 position.setPredicate(endPospred);
		    		 position.setPiecePred(endPred);
		    		 position.setPieces(pieces);
		    	 }
		    
//		    	 System.out.println("Initial taken positions: "+irs+" ToString takenPos: "+takenPos.toString()+ " OWL individual: "+ wp.toString()+ " "+name);


			}
		}
		for (WhitePiece whitePiece : whitePieces) {
			HashSet<Taken> taken = (HashSet<Taken>) whitePiece.getOccupies();
		
			boolean whitePos = false;
			boolean blackPos = false;
			IRI irp = whitePiece.getOwlIndividual().getIRI();
			String irpiece = irp.toString();
			char sepp = '#';
	    	String piecename = extractString(irpiece, sepp,-1);
	    	 String pred = whitePiece.toString();
	    	 String posPred = taken.toString();
	    	 String firstPred = extractString(pred, '(',-1);
	    	 String endPred = extractString(firstPred, ';',0);
	    	 String fposPred = extractString(posPred, '(',-1);
	    	 String endPospred = extractString(fposPred, ';',0);
//			System.out.println("createOntologyposition: White piece: "+irpiece+" "+piecename);
			 HashSet<Piece> pieces = new HashSet<Piece>();
			for (Taken takenPos : taken) {
				IRI ir = takenPos.getOwlIndividual().getIRI();
//		    	 HashSet<Piece> pieces =  (HashSet<Piece>)( takenPos).getIsOccupiedBy();
	    		HashSet<WrappedIndividual> individuals =  (HashSet<WrappedIndividual>)( takenPos).getIsOccupiedBy();
				Piece piece = (Piece)whitePiece;
				pieces.add(piece);
		    	 for ( WrappedIndividual individual: individuals) {
		    		 boolean ispiece = individual.getClass().equals(Entity.class);
//		    		 Piece piece = (Piece)individual;
//		    		 Entity ent = (Entity)individual; 
//		    		 System.out.println("createOntologyposition: individual Piece: "+individual.toString()+ " class "+ispiece);
//		    		 pieces.add(piece); 
		    	 }
		    	 String irs = ir.toString();
		    	 OWLNamedIndividual wp = takenPos.getOwlIndividual();
		    	 char sep = '#';
		    	 String name = extractString(irs, sep,-1);
//		    	 HashSet<BoardPosition> parts = (HashSet<BoardPosition>) takenPos.getIsPartOf();
		    	 System.out.println("createOntologyposition: Taken position for white piece: "+irs+" Taken position toString: "+takenPos.toString()+ " OwlIndividual toString:  "+ wp.toString()+ " Name from irs "+name);
		    	 allTakenPositions.add(takenPos);
		    	 BoardPosition boardPosition = (BoardPosition)takenPos;
		    	 blackPos = boardPosition.getClass().equals(BlackBoardPosition.class);
		    	 whitePos = boardPosition.getClass().equals(WhiteBoardPosition.class);
		    	 Position position = positions.get(name);
		    	 if (position != null){
		    		 if (blackPos)
		    			 position.setBlackBoardPosition((BlackBoardPosition)boardPosition);
		    		 if (whitePos)
		    			 position.setWhiteBoardPosition((WhiteBoardPosition)boardPosition);
		    		 //			    	  position.setPieces(pieces);
//		    		 System.out.println("createOntologyposition: Found a position for white pieces"+name+" Position "+boardPosition.toString());
		    		 position.setPredicate(endPospred);
		    		 position.setPiecePred(endPred);
		    		 position.setPieces(pieces);
		    	 }
	
//		    	 System.out.println("Initial taken positions: "+irs+" ToString takenPos: "+takenPos.toString()+ " OWL individual: "+ wp.toString()+ " "+name);

			}
		}
		for (Taken taken : allTakenPositions) {
	    	  IRI ir = taken.getOwlIndividual().getIRI();
	    	  HashSet<WrappedIndividual> pieces =  (HashSet<WrappedIndividual>)( taken).getIsOccupiedBy();
	    	  String irs = ir.toString();
	    	  OWLNamedIndividual wp = taken.getOwlIndividual();
	    	  char sep = '#';
	    	  String name = extractString(irs, sep,-1);
	    	  System.out.println("createOntologyposition: Initial taken positions: "+irs+" "+taken.toString()+ " "+ wp.toString()+ " "+name);
	    	  for (WrappedIndividual piece : pieces) {
	    		  System.out.println("createOntologyposition: Position occupied by: "+piece.toString());
	    	  }
	    	  Position position = positions.get(name);
/*	    	  if (position != null){
//	    		  position.setWhiteBoardPosition(takenPos);
	    		  position.setPieces(pieces);
	    	  }*/
		}
		reasonWith();
/*		for (Vacant vacant : allVacantPositions) {
			 System.out.println("Initial vacant positions: "+ vacant.toString());
		}*/
		
/*			Iterator<BlackBoardPosition> blackPosIterator =  blackPositions.iterator();
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
		      }*/	      
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
				if (position.isInUse() && position.getUsedBy() != null){ //midlertidig !!
					pieceName = position.getUsedBy().getName().substring(1);
					String color = position.getUsedBy().getColor();
					if (color.equals("b"))
						pieceName = pieceName.toLowerCase();
					boardRow[i][j] = pieceName;
				}else{
					boardRow[i][j] = "f";
/*					if ( position.getUsedBy() == null) {
						System.out.println("No piece: "+position.toString()
						);
					}*/
				}

				String cp = "ledig";
				if (position.isInUse()&& position.getUsedBy() != null){
					cp = position.getUsedBy().getName();
				}
//				System.out.println(position.getPositionName()+ " "+cp+ " "+boardRow[i][j]+" "+fenCount+ " "+i+" "+j );
				fenCount--;
			}

		}
		return produceFen(boardRow);
//		return startFEN;
	}

	/**
	 * findPiece
	 * This method finds which piece occupies a given position
	 * @param posName
	 * @param pieceName Not Used !!
	 * @return
	 */
	public ChessPiece findPiece(String posName, String pieceName) {
		String localposName = "";
		ChessPiece piece = null;
		for (int i = 0; i < 64; i++) {
			Position position = (Position) positions.get(allPositions[i]);
			localposName = position.getPositionName();
			if (posName.equals(localposName)) {
				piece = position.getUsedBy();
				break;
			}
		}
		return piece;

	}
	/**
	 * findPiece
	 * This method finds which piece occupies a given position
	 * @param posName
	 * @return
	 */
	public ChessPiece findPiece(String posName) {
		String localposName = "";
		ChessPiece piece = null;
		for (int i = 0; i < 64; i++) {
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

	/**
	 * checkPosition
	 * This method checks if a position is occupied by a piece
	 * @param pos: Position to be checked (The name of the position)
	 * @return true if position is occupied
	 */
	public boolean checkPosition(String pos) {
		boolean inUse = false;
		for (int i = 0; i < 64; i++) {
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
	/**
	 * determineMove
	 * This method determines if a move is considered legal and if an opposing piece need to be removed.
	 * It is called: 
	 * 1. From RapporterChessStartServerResource when a user makes a move
	 * 2. From the playgame object in the proposemove method.
	 * 
	 * The chess piece that is moved receives the new position if it is accepted
	 * It uses the establishMoves method to create a move in algebraic notation
	 * And it uses the chesspiece acceptMove method to set the opposing piece passive
	 * 
	 */
	public void determineMove(String oldPos,String newPos,String piece) {
		opposingOccupied = false;
		String oldName = oldPos.substring(0, 1);
    	ChessPiece chessPiece = findPiece(oldPos,piece);
  		pieceType type = chessPiece.getMyPiece().getPieceType();
    	if (chessPiece != null) {
    		chessPiece.setPosition(newPos);
    		castling = chessPiece.getMyPiece().isCastlingMove();
  
    	}
    	if (chessPiece == null) {
    		System.out.println("!!Chessboard nullpointer!!"+oldPos+" ny Posisjon "+newPos);
    	}
       	Position oldPosition = findPostion(oldPos);
       	Position newPosition = findPostion(newPos);
       	if (checkPosition(newPos)) { // If position is occupied find which piece it is occupied by and determine if move is legal
       		
       		boolean accept = chessPiece.acceptMove(newPos, oldPosition,newPosition); // This method sets the opposing piece passive
       		opposingOccupied = accept;
       		if(!accept)
       			chessPiece.setPosition(oldPos);
       		if (accept)
       			establishMoves(newPosition,oldName);

       	}
       	if (!checkPosition(newPos)){
//       	   	oldPosition.setUsedBy(null);
//           	oldPosition.setInUse(false);
           	HashSet pieces = oldPosition.getPieces();
/*    		if (newPosition.getPositionName().equals("a3")) {
				System.out.println("!!determinemove position !! "+newPosition.toString());
			}*/
           	oldPosition.setPieces(null);
           	oldPosition.setUsedBy();
           	newPosition.setUsedBy(chessPiece);
        	newPosition.setInUse(true);
        	newPosition.setPieces(pieces);
        	chessPiece.setPosition(newPosition.getPositionName());
 /*       	if (newPosition.getPositionColor().equalsIgnoreCase("w")) {
        		chessPiece.setWhiteBoardPosition((WhiteBoardPosition) newPosition);
        	}else {
        		chessPiece.setBlackBoardPosition((BlackBoardPosition) newPosition);
        	}*/
        	if(!castling)
        		establishMoves(newPosition,oldName);
        	if(castling && type == type.KING) 
        		establishMoves(newPosition,oldName);
       	}
		
	}


	/**
	 * setPiecetoPosition
	 * This method places the eight queens from NQueensBoard in the correct positions on the chessboard
	 * @param board The aima chessboard used
	 * @param qCSP The queens CSP
	 * The MoveMent class is used to places pieces correctly
	 */
	public void setPiecetoPosition( NQueensBoard board,NQueensCSP qCSP) {
		MoveMent movements = new MoveMent(board,positions);
		movements.setQueensPositions();
		StringBuilder result = new StringBuilder();
		List<Variable> var = qCSP.getVariables();
		Variable v = var.get(0);
		String varName = v.getName();
		for (int i = 0; i < 64; i++) {
			Position position = (Position) positions.get(allPositions[i]);
			XYLocation loc = position.getXyloc();
			if (board.queenExistsAt(loc)) {
				String color = position.getPositionColor();
				String name = position.getPositionName();
				boolean inUse = position.isInUse();
				result.append("\n xy location").append(loc.toString()).append(" Position "+name);
				if (inUse) {
					ChessPiece piece = position.getUsedBy();
					HashSet<Piece> pieces = position.getPieces();
					for (Piece occupier : pieces) {
						HashSet<String> pNames = (HashSet<String>)occupier.getHasName();
						String pName = pNames.toString();
						result.append("\nOccupied by").append(pName);
					}
					result.append("\nColor : Position ").append(color).append(" : ").append(name).append(" Piece: "+piece.getOntlogyName()+" "+piece.getPieceName());
				}

			}

		}

		 System.out.println(result.toString());
			
	

	}
}
