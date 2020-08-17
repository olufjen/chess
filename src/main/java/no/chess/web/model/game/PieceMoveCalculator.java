package no.chess.web.model.game;

import java.util.HashMap;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece.pieceColor;
import no.games.chess.AbstractGamePiece.pieceType;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.GamePiece;
import no.games.chess.MoveRule;

/**
 * PieceMoveRuler
 * Based on piece type this class calculates available positions for the piece
 * @author oluf
 *
 */
public class PieceMoveRuler implements MoveRule<AgamePiece, List<XYLocation>> {

	private String color;
	@Override
	public List<XYLocation> calculateRule(AgamePiece t) {
		Position from = t.getmyPosition();
		color = t.getColor();
		pieceType type = t.getMyType();
		pieceColor colorType = t.getLocalColor();
		XYLocation loc = from.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		String posName = from.getPositionName();
		ChessPieceType pt = t.getChessType();
		GamePiece gamePiece = (GamePiece)pt;
		gamePiece.getLegalmoves(from);
		boolean whitePawn = type == pieceType.PAWN && colorType == pieceColor.WHITE;
		boolean pawnstart = whitePawn && y > 1; 

		return null;
	}
	private List<XYLocation> whitePawn(){
		return null;
				
	}

}
