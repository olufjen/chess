package no.chess.web.model.game;

import no.games.chess.AbstractGamePiece.pieceType;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import no.chess.web.model.Position;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessProcessor;

/**
 * PreferredMoveProcessor
 * This class calculates a preferred move (ApieceMove) for a given chessaction and its piece.
 * This ApieceMove contains a Preferred Position for a given piece and action.
 * It is created and called when the chess action is created and from The active player's calculatepreferredPosition method.
 * It is also created and used when an action is analyzed and found not to contain any preferred position (the action.getpreferredPosition() method).
 * Then The active player's calculatepreferredPosition method is called, to determine a preferred position.
 * This processor also removes positions from available positions of
 * bishop,rook, and queen when positions are occupied by friendly pieces.
 * 
 * @author oluf
 *
 */
public class PreferredMoveProcessor implements ChessProcessor<ChessActionImpl,AgamePiece, ApieceMove> {

	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\";
	private PrintWriter writer = null;
	private Integer processNumber; // The process number is created by the piece's introw*10 + intcolumn position
	private FileWriter fw = null;
	private Position heldPosition; // This is the position held by the piece under consideration
	private List<Position> removedPositions = null;
	public PreferredMoveProcessor(Integer processNumber,String pname) {
		super();
		this.processNumber = processNumber;
		this.processNumber = processNumber;
		String pNumber = processNumber.toString();
		outputFileName = outputFileName + "preferredmove"+pname + pNumber+".txt";
		try {
			fw = new FileWriter(outputFileName, true);
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	      writer = new PrintWriter(new BufferedWriter(fw));		
/*		
	      try 
	      {
	         writer = new PrintWriter(outputFileName);
	      } catch (FileNotFoundException e) {
	         System.err.println("'" + outputFileName 
	            + "' is an invalid output file.");
	      }	*/
	}


	public ApieceMove processChessObject(ChessActionImpl action, AgamePiece p) {
		removedPositions =  (List<Position>) action.getPositionRemoved(); // Removed positions are positions occupied by friendly pieces
		List<Position> availablePositions = (List<Position>) action.getAvailablePositions();
		List<Position> preferredPositions = new ArrayList<>();
		Position preferredPosition = null;
		Position from = p.getmyPosition();
		
		String name =action.getChessPiece().getMyPiece().getOntlogyName();
		String otherName = p.getMyPiece().getOntlogyName();
		if (name.equals(otherName) && name.equals("WhiteBishop2")) {
			 System.out.println("PreferredMove: Checking bishop");
		}
		
		heldPosition = from;
		String color = p.getColor();
/*
 * Checking if piece is active: Added 21.04.20		
 */
		if (!p.isActive()) {
			writer.println("Piece taken 1: "+p.toString());
			return null;
		}
		if (!p.getMyPiece().isUse()) {
			writer.println("Piece taken 2: "+p.toString());
			p.setActive(false);
			return null;
		}
			
		APawn pn = null;
		ABishop b = null;
		ARook r = null;
		AQueen qt = null;
		AKnight kn = null;
		Aking king = null;
		ChessPieceType pieceType = p.getChessType();
		if (pieceType instanceof APawn) {
			pn = (APawn) pieceType;
		}
		if (pieceType instanceof ABishop) {
			b = (ABishop) pieceType;
		}
		if (pieceType instanceof ARook) {
			r = (ARook) pieceType;
		}
		if (pieceType instanceof AQueen) {
			qt = (AQueen) pieceType;
			writer.println("the piece is a queen ");
		}
		if (pieceType instanceof AKnight) {
			kn = (AKnight) pieceType;
		}
/*		try {
			pn = ChessFunctions.findpieceType(p,(AgamePiece c )->p.getPieceType() == p.getMyType().PAWN);
		} catch (ClassCastException e) {
			System.out.println(e.getMessage());
//			e.printStackTrace();
		}		
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
		}*/
/*
 * If the chess piece is a rook, a bishop, a pawn or a queen
 * then remove all positions in path that are occupied by friendly pieces:
 * OLJ 09.09.20: This is only correct for rook
 * 
 */
		if (b != null || r != null || qt != null || pn != null) {
			String pName = p.toString();
			writer.println("Checking additional removals For "+pName);
			List<Position> tempList = new ArrayList<>();
			int pcol = from.getIntColumn();
			int prow = from.getIntRow();
			for (Position removedPos:removedPositions) {
				int row = removedPos.getIntRow();
				int col = removedPos.getIntColumn();
				for (Position availablePos:availablePositions) {
					int arow = availablePos.getIntRow();
					int acol = availablePos.getIntColumn();
					if ((r != null || pn != null || qt != null) && prow < row && pcol == col && arow > row && acol == col) { // not applicable when from row > row
						tempList.add(availablePos);
					}
					if ((r != null || pn != null || qt != null) && prow > row && pcol == col && arow < row && acol == col) { // Not applicable when the from row < row
						tempList.add(availablePos);
					}
/*
 * Check for bishop	and queen				
 */
					if ((b != null || qt != null) && col < pcol && arow > row && acol < col) {
						tempList.add(availablePos);
					}
					if ((b != null || qt != null) && col < pcol && arow < row && acol < col) {
						tempList.add(availablePos);
					}
					if ((b != null || qt != null) && col > pcol && arow < row && acol > col) {
						tempList.add(availablePos);
					}
					if ((b != null || qt != null) && col > pcol && arow > row && acol > col) {
						tempList.add(availablePos);
					}
/*					if (b != null && arow < row && acol >= col) {
						tempList.add(availablePos);
					}
*/
				}
			}
			for (Position temp:tempList) {
				String posName = temp.getPositionName();
//				logText.append(posName+"\n");
				writer.println("Added removed position "+temp.toString()+" For "+pName);
				removedPositions.add(temp);
			}
//			tempList = null;

		}
/*		if (kn != null) {
			
		}*/
		for (Position availablePos:availablePositions) {
			boolean available = true;
			for (Position removedPos:removedPositions) {
				if (removedPos.getPositionName().equals(availablePos.getPositionName())) {
					available = false;
					writer.println("Removed position "+removedPos.toString());
					break;
				}

			}
			if (available) {
				preferredPositions.add(availablePos);
			}
		}
		if (preferredPositions.isEmpty()) {
			p.setPreferredPositions(null);
			writer.println("No preferred position\n"+"piece: "+p.toString());
			writer.close();
			return null;
		}
		p.setPreferredPositions(preferredPositions);
		for (Position preferredPos:preferredPositions) {
			if (preferredPos.isInUse() && pn == null) { // The active piece is not a pawn ?!
				preferredPosition = preferredPos;
			}
		}
		if (preferredPosition == null) {
			preferredPosition = preferredPositions.get(0);
		}
		ApieceMove move = new ApieceMove(preferredPosition,p);	
		move.setPreferredPositions(preferredPositions);
		writer.println("Move: "+move.toString()+"\n"+"piece: "+p.toString());
		writer.close();
	return move;
	}


	public Position getHeldPosition() {
		return heldPosition;
	}



}
