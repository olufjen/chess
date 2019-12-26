package no.chess.web.model.game;

import no.chess.web.model.Position;
import no.games.chess.AbstractPieceMove;

/**
 * ApieceMove represent the implementation of a piece move made by a player.
 * It contains a from position and a to position
 * It is created when a piece has been moved
 * The <F,T> is replaced by the Position class
 * @author oluf
 * @param <F> Type parameter for from Position
 * @param <T> Type parameter for to Position
 */
public class ApieceMove extends AbstractPieceMove<Position,Position> {

	private Position fromPosition;
	private Position toPosition;
	private AgamePiece piece;



	public ApieceMove(Position toPosition, AgamePiece piece) {
		super();
		this.toPosition = toPosition;
		this.piece = piece;
		this.moveNumber = 0;
		this.fromPosition = piece.getmyPosition();
		this.moveNotation = ""; // Calculate the move notation
		
		
	}

	public ApieceMove(AgamePiece piece,Position fromPosition, Position toPosition,int movenr,String moveNotation) {
		super();
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
		this.moveNumber = movenr;
		this.moveNotation = moveNotation;
		this.piece = piece;
	
	}


	public AgamePiece getPiece() {
		return piece;
	}

	public void setPiece(AgamePiece piece) {
		this.piece = piece;
	}

	public void setFromPosition(Position fromPosition) {
		this.fromPosition = fromPosition;
	}

	public void setToPosition(Position toPosition) {
		this.toPosition = toPosition;
	}


	@Override
	public Position getFromPosition() {
		
		return fromPosition;
	}


	@Override
	public Position getToPosition() {
		
		return toPosition;
	}

	public String toString() {
		return "Move\nPiece "+piece.toString()+"\nFrom position "+fromPosition.toString()+"\nTo position "+toPosition.toString()+" Move number "+moveNumber+moveNotation;
	}
}
