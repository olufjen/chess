package no.chess.web.model;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.Percept;
import aima.core.agent.impl.AbstractEnvironment;
import aima.core.environment.nqueens.NQueensBoard;
import aima.core.environment.nqueens.QueenAction;
import aima.core.util.datastructure.XYLocation;

/**
 * This is the external environment for the 8 Queeen problem
 * It contains a board on which the 8 queens can be placed
 * It contains an implementation of an executeAction function.
 * @author oluf
 *
 */
public class QueensEnvironment extends AbstractEnvironment {
	private NQueensBoard board;
	
	
	public QueensEnvironment(NQueensBoard board) {
		super();
		this.board = board;
	}

	@Override
	public void executeAction(Agent agent, Action action) {
		if (action instanceof QueenAction) {
			QueenAction act = (QueenAction) action;
			XYLocation loc = new XYLocation(act.getX(), act.getY());
			if (act.getName() == QueenAction.PLACE_QUEEN)
				board.addQueenAt(loc);
			else if (act.getName() == QueenAction.REMOVE_QUEEN)
				board.removeQueenFrom(loc);
			else if (act.getName() == QueenAction.MOVE_QUEEN)
				board.moveQueenTo(loc);
		}

	}

	@Override
	public Percept getPerceptSeenBy(Agent anAgent) {
		// TODO Auto-generated method stub
		
		return null;
	}

	public NQueensBoard getBoard() {
		return board;
	}

	public void setBoard(NQueensBoard board) {
		this.board = board;
	}
	

}
