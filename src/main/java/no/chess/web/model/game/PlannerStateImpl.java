package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.List;

import aima.core.logic.planning.ActionSchema;
import no.games.chess.ChessPlayer;
import no.games.chess.planning.ChessPlannerAction;
import no.games.chess.planning.PlannerState;

/**
 * PlannerStateImpl
 * This class represent the Planner State of the game.
 * It contains a number of Planner Actions and Action Schemas, and the Player of the game.
 * There is one Planner Action for every Action schema
 * It is created by the Problem Solver when the planProblem method is called.
 * @author oluf
 *
 */
public class PlannerStateImpl implements PlannerState {
	private APlayer player;
	private APlayer opponent;
	private List<ActionSchema> actionSchemas; // Ground action schemas from Chess Actions Each action schema has an initial and goal state CHECK!!
											// These are action schemas produced from all possible available chess actions.	
	private List<ActionSchema>otherSchemaList = null;// A list of propositionalized action schemas from the lifted action schema. It is used for problem solving
	private List<ChessPlannerAction> plannerActions; // Contains the list of propositionalized action schemas from the lifted action schema
	private ChessPlannerAction plannerAction; // The first entry of the list of plannerActions
	private int moveNr;
	private APerceptor thePerceptor = null;
	
	public PlannerStateImpl(APlayer player,APlayer opponent, List<ActionSchema> actionSchemas,List<ActionSchema>otherSchemaList, int moveNr) {
		super();
		this.player = player;
		this.opponent = opponent;
		this.actionSchemas = actionSchemas;
		this.otherSchemaList = otherSchemaList;
		this.moveNr = moveNr;
		plannerActions = new ArrayList<ChessPlannerAction>();
		createplannerActions();
	}
	

	public PlannerStateImpl(APlayer player,APlayer opponent, List<ActionSchema> actionSchemas, int moveNr, APerceptor thePerceptor) {
		super();
		this.player = player;
		this.opponent = opponent;
		this.actionSchemas = actionSchemas;
		this.moveNr = moveNr;
		this.thePerceptor = thePerceptor;
		plannerActions = new ArrayList<ChessPlannerAction>();

		createplannerActions();
	}


	private void createplannerActions() {
		for (ActionSchema schema:actionSchemas) {
			ChessPlannerAction plannerAction = new ChessPlannerActionImpl(schema,player, moveNr, this);
			plannerActions.add(plannerAction);
		}
		if (otherSchemaList != null) {
			for (ActionSchema schema:otherSchemaList) {
				ChessPlannerAction plannerAction = new ChessPlannerActionImpl(schema,player, moveNr, this);
				plannerActions.add(plannerAction);
			}
		}

		plannerAction = plannerActions.get(0);
	}
	public List<ChessPlannerAction> getPlannerActions() {
		return plannerActions;
	}

	public void setPlannerActions(List<ChessPlannerAction> plannerActions) {
		this.plannerActions = plannerActions;
	}

	public ChessPlannerAction getPlannerAction() {
		return plannerAction;
	}

	public void setPlannerAction(ChessPlannerAction plannerAction) {
		this.plannerAction = plannerAction;
	}

	public List<ActionSchema> getActionSchemas() {
		return actionSchemas;
	}

	public void setActionSchemas(List<ActionSchema> actionSchemas) {
		this.actionSchemas = actionSchemas;
	}

	public APlayer getPlayer() {
		return player;
	}

	public void setPlayer(APlayer player) {
		this.player = player;
	}
	public List<ActionSchema> getOtherSchemaList() {
		return otherSchemaList;
	}
	public void setOtherSchemaList(List<ActionSchema> otherSchemaList) {
		this.otherSchemaList = otherSchemaList;
	}
	@Override
	public double getUtility() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setUtility(double utility) {
		// TODO Auto-generated method stub

	}

	@Override
	public ChessPlayer getPlayerTomove() {
		// TODO Auto-generated method stub
		return player;
	}

	@Override
	public ChessPlannerAction getAction() {
	
		return this.plannerAction;
	}

	@Override
	public List<ChessPlannerAction> getActions() {
			return getPlannerActions();
	}

	@Override
	public ActionSchema getActionSchema() {
		
		return null;
	}

	@Override
	public void setAction(ChessPlannerAction action) {
		this.plannerAction = action;
		
	}
/**
 * testEnd
 * This is the active goal test function
 * It determines whether a given state is a goal state
 * A proposal:
 * Use this function to 
 * fill the otherSchemaList based on parameters as shown 
 * in the PEAS tables 
 * Lifted action schemas are created with parameters in the following order:
 * Startpos, Piecename, Newpos, Piecetype, or null.
 * Lifted action schemas represent goal formulations.
 * Create other objects that implements PlannerState and ChessPlannerAction interfaces.
 * Objects that can be returned with the node
 * @return true if this is the goal state
 */	
	@Override
	public boolean testEnd(ChessPlannerAction a) {
		List<ActionSchema> schemas = a.getActionSchemas();
		ThePeas peas = new ThePeas(player,opponent,moveNr);
		String[] param = peas.selectPerformance();
		  //		  thePerceptor.createLiftedActions(null,null,"d4",null);
		thePerceptor.createLiftedActions(param); // Creates the initial and goal states based on this action schema
		this.otherSchemaList = thePerceptor.getOtherActions();
		return true;
	}

}
