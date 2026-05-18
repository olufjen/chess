package no.chess.web.model.game;

import java.io.PrintWriter;
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
	private ActionSchema actionSchema; // The action schema for this action
	private PrintWriter writer =  null;
	private String endPos; // The end position of this action
	
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
		GroundGameState mystate = (GroundGameState) this.gameState;
		String name = actionSchema.getName();
		endPos = KnowledgeBuilder.extractString(name,'_',-1);
		writer = mystate.getWriter();
		
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
	
	public String getEndPos() {
		return endPos;
	}
	public void setEndPos(String endPos) {
		this.endPos = endPos;
	}
	public void setActionSchema(ActionSchema actionSchema) {
		this.actionSchema = actionSchema;
	}
	public String toString() {
		String ret = actionSchema.toString();
		return ret;
	}
	/**
	 * performAction
	 * This method is called from the result function - "Results(s, a)".
	 * which again is called from the orSearch method when the problem testGoal function for a chosen state returns false. (The state is not the goal state)
	 * As defined:
	 * For å bruke søkemotoren til å lage en plan, må du definere en Results-funksjon som simulerer nondeterminismen:
	 * Modellering av "Results(s, a)":
	 * Når agenten (Hvit) utfører handlingen $a$:
	1. Transition: Utfør trekket $a$ på brettet.
	2. Opponent's Turn: Identifiser alle lovlige trekk for Sort ($m_1, m_2, ..., m_n$).
	3. State Set: Generer en ny KB for hvert av Sorts mulige svar.
	4. Output: Funksjonen returnerer en liste over disse KB-ene.

	 * It returns a list of Game states as a result of the action
	 */
	public List<GameState> performAction() {
		GroundGameState state = (GroundGameState) this.gameState; // The state this action is performed in
		List<GameState> states = new ArrayList<GameState>();
		List<GroundGameAction> opponentActions = state.getOpponentGameActions();
		for (GroundGameAction oppaction:opponentActions) { // All opponent's actions - For all opponent actions - create a new state
			ActionSchema schema = oppaction.getActionSchema();
			writer.println("Results(s, a) - performAction: Creates a state for the Opponent action schema "+schema.getName());
			GroundGameState actionState = new GroundGameState(state.getPlayer(),state.getOpponent(),state.getMoveNr(),state.getKnowledgeBase(),state.getThePerceptor(),state.getActionSchemas(), state.getOpponentActions(),state.getPositionList(),schema); // Creates a groundgamestate based on a possible opponent actionschema
			actionState.setActions(state.gettheRelavantActions());
			actionState.setOpponentGameActions(opponentActions);
			actionState.setOppAction(oppaction);
			states.add(actionState);
			if (actionState.isContextFound())
				break;
			if (actionState.isOpeningFound())
				break;
		}
		writer.flush();
		states.add(state); // Also adds the current state?
		return states;
	}

}
