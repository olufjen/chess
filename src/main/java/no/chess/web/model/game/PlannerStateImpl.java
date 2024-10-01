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
 * It is created by the Problem Solver when the planProblem method is called.
 * @author oluf
 *
 */
public class PlannerStateImpl implements PlannerState {
	private APlayer player;
	private List<ActionSchema> actionSchemas; // Ground action schemas from Chess Actions Each action schema has an initial and goal state CHECK!!
	private List<ActionSchema>otherSchemaList = null;// A list of propositionalized action schemas from the lifted action schema. It is used for problem solving
	private List<ChessPlannerAction> plannerActions;
	private ChessPlannerAction plannerAction;
	private int moveNr;
	
	public PlannerStateImpl(APlayer player, List<ActionSchema> actionSchemas,List<ActionSchema>otherSchemaList, int moveNr) {
		super();
		this.player = player;
		this.actionSchemas = actionSchemas;
		this.otherSchemaList = otherSchemaList;
		this.moveNr = moveNr;
		plannerActions = new ArrayList<ChessPlannerAction>();
		createplannerActions();
	}
	private void createplannerActions() {
		for (ActionSchema schema:actionSchemas) {
			ChessPlannerAction plannerAction = new ChessPlannerActionImpl(schema,player, moveNr);
			plannerActions.add(plannerAction);
		}
		for (ActionSchema schema:otherSchemaList) {
			ChessPlannerAction plannerAction = new ChessPlannerActionImpl(schema,player, moveNr);
			plannerActions.add(plannerAction);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ChessPlannerAction> getActions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionSchema getActionSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAction(ChessPlannerAction action) {
		// TODO Auto-generated method stub
		
	}

}
