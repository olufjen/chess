package no.chess.web.model.game;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import aima.core.logic.fol.kb.FOLKnowledgeBase;
import no.chess.web.model.PlayGame;
import no.games.chess.ChessAction;

/**
 * The opponent agent object,
 * contains all opponent available actions, and their action schemas.
 * The opponent agent object must be able to answer the  question,
 * Which of my pieces are threatened or lost if a make a certain move.
 * @author olj
 *
 */
public class OpponentAgent {

	private ChessStateImpl stateImpl = null;
	private ChessActionImpl localAction = null;
	private List <ChessAction> actions = null; // All actions available to the opponent
	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\opponent.txt";
	private PrintWriter writer = null;
	private FileWriter fw = null;
	private PlayGame game = null;
	private APlayer myPlayer = null;
	private APlayer opponent = null;
	private FOLKnowledgeBase folKb = null;
	
	public OpponentAgent(ChessStateImpl stateImpl, PlayGame game, APlayer myPlayer, APlayer opponent,FOLKnowledgeBase folKb) {
		super();
		this.stateImpl = stateImpl;
		this.game = game;
		this.myPlayer = myPlayer;
		this.opponent = opponent;
		this.folKb = folKb;
		actions = this.stateImpl.getActions(myPlayer);
		this.myPlayer.setActions(actions);
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	    writer = new PrintWriter(new BufferedWriter(fw));	
	}
	public ChessStateImpl getStateImpl() {
		return stateImpl;
	}
	public void setStateImpl(ChessStateImpl stateImpl) {
		this.stateImpl = stateImpl;
	}
	public ChessActionImpl getLocalAction() {
		return localAction;
	}
	public void setLocalAction(ChessActionImpl localAction) {
		this.localAction = localAction;
	}
	public List<ChessAction> getActions() {
		return actions;
	}
	public void setActions(List<ChessAction> actions) {
		this.actions = actions;
	}
	public PlayGame getGame() {
		return game;
	}
	public void setGame(PlayGame game) {
		this.game = game;
	}
	public APlayer getMyPlayer() {
		return myPlayer;
	}
	public void setMyPlayer(APlayer myPlayer) {
		this.myPlayer = myPlayer;
	}
	public APlayer getOpponent() {
		return opponent;
	}
	public void setOpponent(APlayer opponent) {
		this.opponent = opponent;
	}
	
	/**
	 * probeConsequences
	 * This action examines consequences a player's action may have.                                                                                    
	 * @param playerAction
	 */
	public void probeConsequences(ChessActionImpl playerAction) {
		writer.println("Chosen action\n"+playerAction);
		writer.println("Opponent actions:");
		for (ChessAction action:actions) {
			ChessActionImpl localAction = (ChessActionImpl) action;
			writer.println(localAction);
		}
	}
}
