package no.chess.web.model;

import java.util.List;

/**
 * This class represent moves in a chessgame as shown in chess algebraic notation.
 * THe source of the move is either moves on the chessboard, or from a chess game file
 * @author Oluf
 *
 */
public class ChessMoves {

	private String whiteMove;
	private String blackMove;
	private int moveNr;

	private List<String> moveLines;
	public ChessMoves() {
		super();
		whiteMove = "-";
		blackMove = "-";
		moveNr = 0;
	}
	

	public ChessMoves(String whiteMove, String blackMove, int moveNr) {
		super();
		this.whiteMove = whiteMove;
		this.blackMove = blackMove;
		this.moveNr = moveNr;
	}


	public List<String> getMoveLines() {
		return moveLines;
	}

	public void setMoveLines(List<String> moveLines) {
		this.moveLines = moveLines;
	}

	public String getWhiteMove() {
		return whiteMove;
	}

	public void setWhiteMove(String whiteMove) {
		this.whiteMove = whiteMove;
	}

	public String getBlackMove() {
		return blackMove;
	}

	public void setBlackMove(String blackMove) {
		this.blackMove = blackMove;
	}

	public int getMoveNr() {
		return moveNr;
	}

	public void setMoveNr(int moveNr) {
		this.moveNr = moveNr;
	}
	public String toString() {
		Integer mn = new Integer(moveNr);
		
		return mn.toString()+" "+whiteMove+ " "+blackMove;
	}
}
