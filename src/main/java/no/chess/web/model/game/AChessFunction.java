package no.chess.web.model.game;

import java.util.function.BiFunction;

import no.games.chess.ChessFunctions;

public class AChessFunction extends ChessFunctions {

	
	/**
	 * createMovement
	 * This functional interface creates a new possible movement based on a
	 * to position and a chesspiece
	 * @param p The to position
	 * @param a The chess piece
	 * @param f The create function
	 * @return
	 */
	public static <Position,AgamePiece,ApieceMove> ApieceMove createMovement(Position p,AgamePiece a,BiFunction<Position,AgamePiece,ApieceMove> f) {
		
		ApieceMove m = f.apply(p, a);
		return m;
		
	}
}
