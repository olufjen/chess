package no.chess.web.model.game;

import java.util.List;

import aima.core.search.adversarial.IterativeDeepeningAlphaBetaSearch;
import no.games.chess.ChessPlayer;
import no.games.chess.GamePiece;
import no.games.chess.PieceMove;
import no.games.chess.planning.AbstractPlannerGame;
import no.games.chess.planning.ChessPlannerAction;
import no.games.chess.planning.PlannerState;

/**
 * AplannerGame
 * This class is an extension of the AbstractPlannerGame which again is an implementation of the Game interface
 * described in the book Artificial Intelligence A Modern Approach (3rd Edition): page 165.
 * It is implemented as a ChessPlannerSearch which is an extension of IterativeDeepeningAlphaBetaSearch
 * @author oluf
 *
 */
public class AplannerGame extends AbstractPlannerGame {
	private APlayer player;
	private PlannerStateImpl plannerState;
	
	public APlayer getPlayer() {
		return player;
	}

	public void setPlayer(APlayer player) {
		this.player = player;
	}

	public PlannerStateImpl getPlannerState() {
		return plannerState;
	}

	public void setPlannerState(PlannerStateImpl plannerState) {
		this.plannerState = plannerState;
	}

	@Override
	public double analyzeState(PlannerState state) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUtility(PlannerState state, ChessPlayer player) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public PlannerState getInitialState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChessPlayer<GamePiece, PieceMove>[] getPlayers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChessPlayer<GamePiece, PieceMove> getPlayer(PlannerState state) {
		
		PlannerStateImpl localState = (PlannerStateImpl) state;
//		return localState.getPlayer();
		return localState.getPlayerTomove();
	}

	@Override
	public List<ChessPlannerAction> getActions(PlannerState state) {
		PlannerStateImpl localState = (PlannerStateImpl) state;
		return null;
	}

	@Override
	public PlannerState getResult(PlannerState plannerState, ChessPlannerAction action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTerminal(PlannerState state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double analyzePieceandPosition(ChessPlannerAction action) {
		// TODO Auto-generated method stub
		return 0;
	}

}
