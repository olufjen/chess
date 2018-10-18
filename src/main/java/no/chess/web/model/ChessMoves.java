package no.chess.web.model;

/**
 * This class represent moves in a chessgame
 * @author Oluf
 *
 */
public class ChessMoves {

	private String whiteMove;
	private String blackMove;
	private int moveNr;

	public ChessMoves() {
		super();
		whiteMove = "-";
		blackMove = "-";
		moveNr = 0;
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
	
}
