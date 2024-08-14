package no.chess.web.model.game;

import java.util.List;

import aima.core.logic.planning.ActionSchema;
import no.games.chess.ChessPlayer;
import no.games.chess.planning.ChessPlannerAction;
import no.games.chess.planning.PlannerState;

public class PlannerStateImpl implements PlannerState {
	private APlayer player;
	private List<ActionSchema> actionSchemas;
	private List<ChessPlannerAction> plannerActions;
	private ChessPlannerAction plannerAction;
	
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
