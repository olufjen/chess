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

/**
 * This class creates percept schemas 
 * From page 416 in the AIMA book:
 * - To solve a partially observable problem, the agent will have to 
 * reason about the percepts it will obtain when it is executing a plan.. -
 * When it is planning we create a percept schema with a precondition of the form:
 * REACHABLE(x,POS)^PIECETYPE(x,TYPE)
 * which says Is there a piece that can reach position POS and is of Type TYPE)?
 * If that is the case then we choose this piece for the next move
 * 
 */
public class APerceptor {
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

public APerceptor(Position posin,String reach,String type,String typeofPiece) {
	super();
	pos = posin;
	String posname = pos.getPositionName();
	reaches = reach;
	types = type;
	pieceName = new Variable("piecename");
	typeTerms = new ArrayList<Term>();
	typeTerms.add(pieceName);
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
	reachTerms.add(pieceName);
	reachTerms.add(topos);
	variables.addAll(reachTerms);
	variables.addAll(typeTerms);
	reachPredicate = new Predicate(reaches,reachTerms);
	typePredicate = new Predicate(types,typeTerms);
	precondition.add(new Literal((AtomicSentence) reachPredicate));
	precondition.add(new Literal((AtomicSentence)typePredicate));
	percept = new PerceptSchema("MOVE",variables,precondition);
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
	for (State state:allStates) {
		List<Literal> literals = state.getFluents();
		for (Literal lit:literals) {
			Predicate p = (Predicate) lit.getAtomicSentence();
			List<Term> terms = p.getTerms(); // Must find the correct type of constants!!
			for (Term t:terms) {
				Constant c = (Constant)t;
				stateconstants.add(c);
			}
		}
		for (Term pterm:variables) {
			if (pterm instanceof Variable) {
				Variable v = (Variable)pterm;
				String name = v.getSymbolicName();
			}
		}
		PerceptSchema conPercept = percept.getActionBySubstitution(stateconstants);
		boolean found = state.getFluents().containsAll(conPercept.getPrecondition());
		if (found) {
			return state;
		}
	}
	return null;
}

}
