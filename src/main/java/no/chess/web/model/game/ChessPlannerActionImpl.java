package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.List;

import aima.core.logic.planning.ActionSchema;
import aima.core.logic.fol.kb.data.Literal;
import aima.core.logic.fol.parsing.ast.AtomicSentence;
import aima.core.logic.fol.parsing.ast.Constant;
import aima.core.logic.fol.parsing.ast.Predicate;
import aima.core.logic.fol.parsing.ast.Term;
import aima.core.logic.fol.parsing.ast.Variable;
import no.games.chess.planning.ChessPlannerAction;
import no.games.chess.planning.PlannerState;

/**
 * ChessPlannerActionImpl
 * This class implements the ChessPlannerAction interface.
 * PlannerActions are used as part of the PlannerState.
 * And a Planner State is part of the Planner Game and search tree.
 * Each PlannerAction contains an Action Schema.
 * An Action Schema is either a ground schema generated from available Chess Actions, 
 * or a Lifted Action schema determined by the state of the game.
 * The move number
 * Possible available positions
 * Possible opponent pieces to take  
 * 
 * @author oluf
 *
 */
public class ChessPlannerActionImpl implements ChessPlannerAction<ActionSchema> {
	private List<ActionSchema> actionSchemas;
	private ActionSchema actionSchema;
	private APlayer player;
	private String actionName;
	private int moveNr;
	private Double actionValue = null;
	
	public ChessPlannerActionImpl(ActionSchema actionSchema, APlayer player, int moveNr) {
		super();
		this.actionSchema = actionSchema;
		this.player = player;
		this.moveNr = moveNr;
		actionName = actionSchema.getName();
		actionValue = new Double(0);
	}

	@Override
	public boolean isNoOp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ActionSchema getActionSchema() {
		// TODO Auto-generated method stub
		return actionSchema;
	}

	@Override
	public List<ActionSchema> getActionSchemas() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setActionSchemas(List<ActionSchema> actionSchemas) {
		this.actionSchemas = actionSchemas;
	}

	public void setActionSchema(ActionSchema actionSchema) {
		this.actionSchema = actionSchema;
	}

	/**
	 * findactionValue
	 * This method attempt to determine the rank of an action schema by
	 * analyzing the preconditions and effect
	 * @return
	 */
	public Double findactionValue() {
		List<Literal> effectLiterals = actionSchema.getEffects();
		int size = effectLiterals.size();
		String name = null;
		List<Term>terms = null;
		for (Literal l: effectLiterals) {
			AtomicSentence atom = l.getAtomicSentence();
            name = atom.getSymbolicName(); 
            Predicate thePredicate = (Predicate)atom;
            terms = thePredicate.getArgs(); // Terms are constants or variables
 		}
		if (terms != null) { // If any term is a variable then this action schema is a lifted action schema
			List<Variable> variableList = new ArrayList();
			List<Constant> constantList = new ArrayList();
			for (Term term:terms) {
				if (term instanceof Constant) {
					Constant termName = (Constant) term;
					constantList.add(termName);
				}
				if (term instanceof Variable) {
					Variable termName = (Variable) term;
					variableList.add(termName);
				}
			}
		}
		return actionValue;
	}
	/**
	 * findPlannerState
	 * This method attempt to determine the rank of an action schema by
	 * analyzing the preconditions and effect
	 * @return
	 */
	@Override
	public PlannerState findPlannerState(ChessPlannerAction a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlannerState findPlannerState(PlannerState s) {
		// TODO Auto-generated method stub
		return null;
	}

}
