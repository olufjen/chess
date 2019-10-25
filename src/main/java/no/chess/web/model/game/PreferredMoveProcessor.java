package no.chess.web.model.game;

import no.games.chess.AbstractGamePiece.pieceType;

import java.util.ArrayList;
import java.util.List;

import no.chess.web.model.Position;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessProcessor;

/**
 * PreferredMoveProcessor
 * This class calculates a preferred move (ApieceMove) for a given piece and a chessaction.
 * This ApieceMove contains a Preferred Position for a given piece and action.
 * It is created and called when the chess action is created and from The active player's calculatepreferredPosition method.
 * Also when an action is analyzed and found not to contain any preferred position (the action.getpreferredPosition() method).
 * Then The active player's calculatepreferredPosition method is called, to determine a preferred position.
 * This processor also removes positions from available positions of
 * bishop,rook, and queen when positions are occupied by friendly pieces.
 * 
 * @author oluf
 *
 */
public class PreferredMoveProcessor implements ChessProcessor<ChessActionImpl,AgamePiece, ApieceMove> {


	
	public ApieceMove processChessObject(ChessActionImpl action, AgamePiece p) {
		List<Position> removedPositions =  (List<Position>) action.getPositionRemoved(); // Removed positions are positions occupied by friendly pieces
		List<Position> availablePositions = (List<Position>) action.getAvailablePositions();
		List<Position> preferredPositions = new ArrayList<>();
		Position preferredPosition = null;
		Position from = p.getmyPosition();
		ABishop b = null;
		ARook r = null;
		AQueen qt = null;
		
		try {
			b = ChessFunctions.findpieceType(p,(AgamePiece c )->p.getPieceType() == p.getMyType().BISHOP);
		} catch (ClassCastException e) {
			System.out.println(e.getMessage());
//			e.printStackTrace();
		}
		try {
			r = ChessFunctions.findpieceType(p,(AgamePiece c )->p.getPieceType() == p.getMyType().ROOK);
		} catch (ClassCastException e) {
			System.out.println(e.getMessage());
//			e.printStackTrace();
		}
		try {
			qt = ChessFunctions.findpieceType(p,(AgamePiece c )->p.getPieceType() == p.getMyType().QUEEN);
		} catch (ClassCastException e) {
			System.out.println(e.getMessage());
//			e.printStackTrace();
		}

		if (b != null || r != null || qt != null) {
			List<Position> tempList = new ArrayList<>();
			for (Position removedPos:removedPositions) {
				int row = removedPos.getIntRow();
				for (Position availablePos:availablePositions) {
					int arow = availablePos.getIntRow();
					if (arow >= row) {
						tempList.add(availablePos);
					}
					if (arow < row) {
						tempList.add(availablePos);
					}
				}
			}
			for (Position temp:tempList) {
				String posName = temp.getPositionName();
//				logText.append(posName+"\n");
				removedPositions.add(temp);
			}
			tempList = null;

		}
		for (Position availablePos:availablePositions) {
			boolean available = true;
			for (Position removedPos:removedPositions) {
				if (removedPos.getPositionName().equals(availablePos.getPositionName())) {
					available = false;
					break;
				}

			}
			if (available) {
				preferredPositions.add(availablePos);
			}
		}
		if (preferredPositions.isEmpty()) {
			p.setPreferredPositions(null);
			return null;
		}
		p.setPreferredPositions(preferredPositions);
		for (Position preferredPos:preferredPositions) {
			if (preferredPos.isInUse()) {
				preferredPosition = preferredPos;
			}
		}
		if (preferredPosition == null) {
			preferredPosition = preferredPositions.get(0);
		}
		ApieceMove move = new ApieceMove(preferredPosition,p);	
	return move;
	}


}
