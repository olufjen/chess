package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.List;

import aima.core.logic.planning.ActionSchema;
import no.chess.web.model.Position;
import no.games.chess.GamePiece;
import no.games.chess.search.nondeterministic.GameAction;
import no.games.chess.search.nondeterministic.GameState;

/**
 * GroundGameAction
 * The GroundGameAction is a sub class of GameAction used in nondeterminitic and or search (chapter 4)
 * It contains a gamepiece and a from and to postion.
 * 
 */
public class GroundGameAction extends GameAction {
	private AgamePiece piece; // The gamePiece involved in this action;
	private ActionSchema actionSchema;
	
	public GroundGameAction(GamePiece<Position> gamePiece, ActionSchema actionSchema) {
		super(gamePiece);
		piece = (AgamePiece) gamePiece;
		this.actionSchema = actionSchema;
	}
	public GroundGameAction(GamePiece<Position> gamePiece, ActionSchema actionSchema,GroundGameState state) {
		super(gamePiece);
		piece = (AgamePiece) gamePiece;
		this.actionSchema = actionSchema;
		this.gameState = state;
	}
	public AgamePiece getPiece() {
		return piece;
	}
	public void setPiece(AgamePiece piece) {
		this.piece = piece;
	}
	public ActionSchema getActionSchema() {
		return actionSchema;
	}
	public void setActionSchema(ActionSchema actionSchema) {
		this.actionSchema = actionSchema;
	}
	public String toString() {
		String ret = actionSchema.toString();
		return ret;
	}
	/**
	 * performAcion
	 * This method is called from the result function.
	 * It returns a list of Game states as a result of the action
	 */
	public List<GameState> performAcion() {
		GroundGameState state = (GroundGameState) this.gameState;
		List<GameState> states = new ArrayList<GameState>();
		states.add(state);
		return states;
	}
}
