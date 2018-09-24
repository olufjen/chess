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

import org.protege.owl.codegeneration.WrappedIndividual;
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
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;
import org.swrlapi.sqwrl.exceptions.SQWRLException;
import org.swrlapi.sqwrl.values.SQWRLResultValue;

import com.hp.hpl.jena.sparql.core.Var;

import no.basic.ontology.control.OntologyContainer;
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
import no.chess.ontology.Object;
import no.chess.ontology.Piece;
import no.chess.ontology.Taken;
import no.chess.ontology.Vacant;
import no.chess.ontology.WhiteBoardPosition;
import no.chess.ontology.WhitePiece;
//import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
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
	private HashSet<Taken> allTakenPositions;
	private HashSet<Vacant> allVacantPositions;	
	private HashSet<ChessPosition> allChessPositions;
	private ArrayList<String> exeRules;
	private ArrayList<String> exeLabels;
	private ChessRules chessRule;
	private ArrayList<ChessRules> chessRules;
	private Position position;
	private OntologyContainer modelContainer;
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
	public ChessBoard() {
		super();
/*
 * From the chess ontology		
 */
		 chessModel  = new OntologyModel();
		 System.out.println("Fetching individuals removed");
//		 individuals = chessModel.getIndividuals(); // These individuals are not used. OLJ 30.08.18
//		 System.out.println("Found individuals");
//		 chessModel.getOntindividuals();
		 chessRules = new ArrayList();
		 chessRule = new ChessRules();
		 chessRules.add(chessRule);
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
		 
//		 createOntologyposition();
	}
	

	public ChessRules getChessRule() {
		return chessRule;
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
	 */
	public void createChessontlogyPosition(){
		for (int i = 0; i <64;i++){
			Position position = positions.get(allPositions[i]);
			ChessPiece chessPiece = position.getUsedBy();
			HashSet<Piece> pieces = position.getPieces();
			Piece ontologyPiece = null;
			if(pieces != null) {
				for (Piece ontPiece :pieces) {
					ontologyPiece = ontPiece;
				}
			}

//			boolean ischessPiece = chessPiece.getClass().equals(Entity.class);
			if (ontologyPiece != null) {
				System.out.println(" An ontology piece: Name of piece: "+ontologyPiece.toString());
//				ontologyPiece.getOwlIndividual()
			}
			String ontName = "Unknown";
			if (chessPiece != null && chessPiece.getOntlogyName() != null)
				ontName = chessPiece.getOntlogyName();
			System.out.println("Position "+position.getPositionName()+" Chessontology: Name of piece: "+chessPiece.getName()+" Name of chess piece: "+chessPiece.getPieceName()+" "+ontName);

			if (ontologyPiece == null){
				position.setUsedBy(null);
				position.setInUse(false);
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
			HashSet<String> names =  (HashSet<String>) chessPos.getHasName(); 
//			HashSet<Vacant> vacants = (HashSet<Vacant>) chessPos.getIsVacant();
//			OWLNamedIndividual individual = chessPos.getOwlIndividual();
			String posx = null;
			for (String pos : names) {
				posx = pos;
				break;
			}
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
		    	 System.out.println("createOntologyposition: Taken position for white piece: "+irs+" "+takenPos.toString()+ " "+ wp.toString()+ " "+name);
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
