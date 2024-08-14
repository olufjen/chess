package no.chess.web.model.game;

import java.util.List;

import aima.core.logic.planning.ActionSchema;
import no.games.chess.planning.ChessPlannerAction;

/**
 * ChessPlannerActionImpl
 * This class implements the ChessPlannerAction interface
 * @author oluf
 *
 */
public class ChessPlannerActionImpl implements ChessPlannerAction<ActionSchema> {
	private List<ActionSchema> actionSchemas;
	private ActionSchema actionSchema;
	
	@Override
	public boolean isNoOp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ActionSchema getActionSchema() {
		// TODO Auto-generated method stub
		return null;
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



}
