package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.fol.parsing.ast.AtomicSentence;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import aima.core.logic.planning.State;
import no.chess.web.model.Position;
import no.games.chess.planning.PerceptSchema;
import no.games.chess.ChessVariables;

/**
 * This class creates percept schemas 
 * From page 416 in the AIMA book:
 * - To solve a partially observable problem, the agent will have to 
 * reason about the percepts it will obtain when it is executing a plan.. -
 * When it is planning we create a percept schema with a precondition of the form:
 * REACHABLE(x,POS)^PIECETYPE(x,TYPE)
 * which says Is there a piece that can reach position POS and is of Type TYPE)?
 * If that is the case then we choose this piece for the next move.
 * 
 */
public class APerceptor {
	/*
	 * Predicate names	
	 */
	  private String ACTION;
	  private String PROTECTED;
	  private String simpleProtected;
	  private String ATTACKED;
	  private String CAPTURE;
	  private String CONQUER;
	  private String THREATEN;
	  private String OWNER;
	  private String MOVE;
	  private String REACHABLE;
	  private String CANMOVE;
	  private String SAFEMOVE;
	  private String STRIKE;
	  private String PIECETYPE;
	  private String PLAY;
	  private String PAWN;
	  private String KNIGHT;
	  private String BISHOP;
	  private String ROOK;
	  private String KING;
	  private String QUEEN;
	  private String PAWNMOVE;
	  private String playerName =  "";
	  private String OCCUPIES = "";
	  private String PAWNATTACK ="";
	  private String playSide;
	  private String BOARD;
	  private String PLAYER;
	  private String CASTLE;
	  private String OPPONENTTO;
	  private String POSSIBLETHREAT;
      private String POSSIBLEPROTECT; // All available positions for a piece are possibly protected by that piece
      private String POSSIBLEREACH; // All available positions for a piece are possibly reachable by that piece

	  private PerceptSchema percept;
	  private String reaches;
	  private String types;
	  private Variable pieceName;
	  private Variable pieceType;
	  private Position pos;
	  private Constant topos;
	  private Constant typePiece;
	  private List<Literal> precondition = null;
	  private List<Term> typeTerms = null;
	  private List<Term> reachTerms = null;
	  private List<Term> variables = null;
	  private Predicate reachPredicate = null;
	  private Predicate typePredicate = null;

public APerceptor(Position posin,String reach,String type,String typeofPiece,String playerName) {
	super();
	setPredicatenames();
	this.playerName = playerName;
	ChessVariables.setPlayerName(playerName);
	pos = posin;
	String posname = pos.getPositionName();
	reaches = reach;
	types = type;
	pieceName = new Variable("piecename");
	typeTerms = new ArrayList<Term>();
	typeTerms.add(pieceName); // Two terms for the piecetype predicate
	if (typeofPiece == null) {
		pieceType = new Variable("type");
		typeTerms.add(pieceType);
	}else {
		typePiece = new Constant(typeofPiece);
		typeTerms.add(typePiece);
	}
	topos = new Constant(posname);
	precondition = new ArrayList();
	reachTerms = new ArrayList<Term>();
	variables = new ArrayList<Term>();
	reachTerms.add(pieceName); // Two terms for the reachable predicate
	reachTerms.add(topos);
	variables.addAll(reachTerms);
	variables.addAll(typeTerms);
	reachPredicate = new Predicate(reaches,reachTerms);
	typePredicate = new Predicate(types,typeTerms);
	precondition.add(new Literal((AtomicSentence) reachPredicate));
	precondition.add(new Literal((AtomicSentence)typePredicate));
	percept = new PerceptSchema("MOVE",variables,precondition);
}
public void setPredicatenames() {
		ACTION = KnowledgeBuilder.getACTION();
		ATTACKED = KnowledgeBuilder.getATTACKED();
		CANMOVE = KnowledgeBuilder.getCANMOVE();
		CAPTURE =  KnowledgeBuilder.getCAPTURE();
		CONQUER = KnowledgeBuilder.getCONQUER();
		MOVE =  KnowledgeBuilder.getMOVE();
		OWNER = KnowledgeBuilder.getOWNER();
		PROTECTED =  KnowledgeBuilder.getPROTECTED();
		REACHABLE = KnowledgeBuilder.getREACHABLE();
		SAFEMOVE = KnowledgeBuilder.getSAFEMOVE();
		STRIKE = KnowledgeBuilder.getSTRIKE();
		simpleProtected =  KnowledgeBuilder.getSimpleProtected();
		THREATEN = KnowledgeBuilder.getTHREATEN();
		PIECETYPE = KnowledgeBuilder.getPIECETYPE();
		PLAY = 	KnowledgeBuilder.getPLAY();
		PAWN = KnowledgeBuilder.getPAWN();
		KNIGHT = KnowledgeBuilder.getKNIGHT();
		BISHOP = KnowledgeBuilder.getBISHOP();
		ROOK = KnowledgeBuilder.getROOK();
		KING = KnowledgeBuilder.getKING();
		QUEEN = KnowledgeBuilder.getQUEEN();
		OCCUPIES = KnowledgeBuilder.getOCCUPIES();
		PAWNMOVE = KnowledgeBuilder.getPAWNMOVE();
		PAWNATTACK = KnowledgeBuilder.getPAWNATTACK();
		BOARD = KnowledgeBuilder.getBOARD();
		PLAYER = KnowledgeBuilder.getPLAYER();
		CASTLE = KnowledgeBuilder.getCASTLE();
		OPPONENTTO = KnowledgeBuilder.getOPPONENTTO();
		POSSIBLETHREAT = KnowledgeBuilder.getPOSSIBLETHREAT();
		POSSIBLEPROTECT = KnowledgeBuilder.getPOSSIBLEPROTECT();
		POSSIBLEREACH = KnowledgeBuilder.getPOSSIBLEREACH();
}
public PerceptSchema getPercept() {
	return percept;
}
public void setPercept(PerceptSchema percept) {
	this.percept = percept;
}
public Variable getPieceName() {
	return pieceName;
}
public void setPieceName(Variable pieceName) {
	this.pieceName = pieceName;
}
public Variable getPieceType() {
	return pieceType;
}
public void setPieceType(Variable pieceType) {
	this.pieceType = pieceType;
}
public Position getPos() {
	return pos;
}
public void setPos(Position pos) {
	this.pos = pos;
}
/**
 * checkPercept
 * This method checks the generated percept action against all available initial states.
 * The initial states have only ground atoms.
 * The percept action is applicable in state s if the precondition of the percept action is satisfied by s.
 * @param initStates
 * @return the chosen state or null
 */
public State checkPercept( Map<String,State>initStates) {
	List<State> allStates = new ArrayList<State>(initStates.values());
	List<Constant> stateconstants = new ArrayList<Constant>();
	List<Literal> preconditions = percept.getPrecondition();
	for (State state:allStates) {
		List<Literal> literals = state.getFluents();
		for (Literal lit:literals) {
			Predicate p = (Predicate) lit.getAtomicSentence();
			List<Term> terms = p.getTerms(); // Must find the correct type of constants, piece piecetype etc!!
			for (Term t:terms) {
				Constant c = (Constant)t;
				if (!stateconstants.contains(t))
					stateconstants.add(c);
			}
		}
		PerceptSchema conPercept = percept.getActionBySubstitution(stateconstants);
		boolean found = state.getFluents().containsAll(conPercept.getPrecondition());//is applicable in state s if the precondition of the percept action is satisfied by s.
		stateconstants.clear();
		if (found) {
			return state;
		}
	}
	return null;
}

}
