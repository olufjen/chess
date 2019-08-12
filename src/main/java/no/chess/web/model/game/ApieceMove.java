package no.chess.web.model.game;

import no.chess.web.model.Position;
import no.games.chess.AbstractPieceMove;

/**
 * ApieceMove represent the implementation of a piece move made by a player.
 * It contains a from position and a to position
 * @author oluf
 *
 * @param <F>
 * @param <T>
 */
public class ApieceMove<Position> extends AbstractPieceMove<Position,Position> {

	private Position fromPosition;
	private Position toPosition;


	public ApieceMove(Position fromPosition, Position toPosition,int movenr,String moveNotation) {
		super();
		this.fromPosition = fromPosition;
		this.toPosition = toPosition;
		this.moveNumber = movenr;
		this.moveNotation = moveNotation;
	}

	public Position getFromPosition() {
		return  fromPosition;
	}

	public Position getToPosition() {
		return  toPosition;
	}

	public void setFromPosition(Position fromPosition) {
		this.fromPosition = fromPosition;
	}

	public void setToPosition(Position toPosition) {
		this.toPosition = toPosition;
	}

}
