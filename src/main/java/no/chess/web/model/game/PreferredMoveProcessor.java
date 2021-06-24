package no.chess.web.model.game;

import no.games.chess.AbstractGamePiece.pieceType;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
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
 * This processor also removes additional positions from available positions of
 * bishop,rook, and queen when positions are occupied by friendly pieces.
 * @since 08.03.21 The queen has its own procedure for removed positions
 * @since 14.06.21 The method for removing positions from bishops has been reworked.
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
	private List<Position> bishopRemoved = null; // This list contains removed positions for the queen in bishop movements
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


	/* (non-Javadoc)
	 * @see no.games.chess.ChessProcessor#processChessObject(java.lang.Object, java.lang.Object)
	 */
	public ApieceMove processChessObject(ChessActionImpl action, AgamePiece p) {
		HashMap<String,Position> reacablePositions = null;
		HashMap<String,Position> bishopPositions = null;
		List<Position> queenscastlePositions = null;
		List<Position> queenbishopPositions = null;
		List<Position>opponentRemoved = action.getOpponentRemoved();
		removedPositions =  (List<Position>) action.getPositionRemoved(); // Removed positions are positions occupied by friendly pieces
		List<Position> availablePositions = (List<Position>) action.getAvailablePositions();
		List<Position> preferredPositions = new ArrayList<>();
		Position preferredPosition = null;
		Position from = p.getmyPosition();
		
		String name =action.getChessPiece().getMyPiece().getOntlogyName();
		String otherName = p.getMyPiece().getOntlogyName();
		if (name.equals(otherName) && name.contains("WhitePawn")){
			writer.println(name);
			HashMap<String,Position> attackPositions = p.getAttackPositions();
			List<Position> attackedPositions = new ArrayList(attackPositions.values());

			for (Position pos:attackedPositions) {
				writer.println("Attacking "+pos.toString());
			}
			
		}
/*		if (name.equals(otherName) && name.equals("WhiteBishop2")) {
			 System.out.println("PreferredMove: Checking bishop");
		}*/
		
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
		List<Position> tempList = null;	
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
			setDirection(p, availablePositions);
			setDirection(p, removedPositions);
		}
		if (pieceType instanceof ARook) {
			r = (ARook) pieceType;
		}
		if (pieceType instanceof AQueen) {
			qt = (AQueen) pieceType;
			if (p.checkBlack()) {
				writer.println("The black queen");
			}
		
			setDirection(p, availablePositions);
			setDirection(p, removedPositions);
//			writer.println("the piece is a queen ");
			bishopRemoved = action.getBishopRemoved();
			bishopPositions = p.getBishopPositions();
			reacablePositions = p.getReacablePositions();
			List<Position> queenAvailablepositions = p.getNewlistPositions();
			queenscastlePositions = new ArrayList(reacablePositions.values()); // The queen castle movements
			queenbishopPositions = new ArrayList(bishopPositions.values());		// The queen bishop movements
			setDirection(p, queenbishopPositions);
			setDirection(p,bishopRemoved);
			tempList = new ArrayList<>();
			int pcol = from.getIntColumn();
			int prow = from.getIntRow();
			for (Position removedPos:bishopRemoved) {
//			for (Position removedPos:bishopRemoved) { // OBS: The removed positions must be separated: bishop positions and rook positions
				int row = removedPos.getIntRow();
				int col = removedPos.getIntColumn();
				String pName = removedPos.getPositionName();
/*				if (pName.equals("c2") || pName.equals("e2")) {
					System.out.println("Pos !!! "+pName);
				}*/
				checkBishopnwest(queenbishopPositions, tempList, removedPos, p);
				checkBishopneast(queenbishopPositions, tempList, removedPos, p);
				checkBishopseast(queenbishopPositions, tempList, removedPos, p);
				checkBishopswest(queenbishopPositions, tempList, removedPos, p);
				
/*				for (Position availablePos:queenbishopPositions) {
					int arow = availablePos.getIntRow();
					int acol = availablePos.getIntColumn();
					if (col < pcol && arow >= row && acol <= col) { // <= OBS !! OLJ 10.03.21
						tempList.add(availablePos);

					}
					if (col < pcol && arow < row && acol < col) {
						tempList.add(availablePos);
					}
					if (col > pcol && arow >= row && acol >= col) { // >= OBS !! OLJ 10.03.21
						tempList.add(availablePos);
					}
				}*/
			}
			for (Position removedPos:removedPositions) {
				int row = removedPos.getIntRow();
				int col = removedPos.getIntColumn();
				for (Position availablePos:queenscastlePositions) {
					int arow = availablePos.getIntRow();
					int acol = availablePos.getIntColumn();
					if (prow < row && pcol == col && arow >= row && acol == col) {
						tempList.add(availablePos);
					}
					if (prow > row && pcol == col && arow <= row && acol == col) {
						tempList.add(availablePos);
					}
					if (pcol< col && prow == row && acol <= col && arow == row) { //Horizontal to right
						tempList.add(availablePos);
					}
					if (pcol> col && prow == row && acol <= col && arow == row) { //Horizontal to left
						tempList.add(availablePos);
					}
				}
			}
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
			String pName = p.getMyPiece().getOntlogyName();
			writer.println("Checking additional removals For "+pName);
			if (tempList == null)
				tempList = new ArrayList<>();
			int pcol = from.getIntColumn();
			int prow = from.getIntRow();
			for (Position removedPos:removedPositions) {
				int row = removedPos.getIntRow();
				int col = removedPos.getIntColumn();
				for (Position availablePos:availablePositions) {
					int arow = availablePos.getIntRow();
					int acol = availablePos.getIntColumn();
					if ((r != null || pn != null ) && prow < row && pcol == col && arow > row && acol == col) { // not applicable when from row > row
						tempList.add(availablePos);
					}
					if ((r != null || pn != null ) && prow > row && pcol == col && arow < row && acol == col) { // Not applicable when the from row < row
						tempList.add(availablePos);
					}
					if (r != null && pcol< col && prow == row && acol <= col && arow == row) { //Horizontal to right
						tempList.add(availablePos);
					}
					if (r != null && pcol> col && prow == row && acol <= col && arow == row) { //Horizontal to left
						tempList.add(availablePos);
					}
/*
 * Check for bishop	OBS OBS Check again The queen option removed !!!!			
 */
/*					if ((b != null ) && col < pcol && arow > row && acol < col) {
						tempList.add(availablePos);
					}
					if ((b != null ) && col < pcol && arow < row && acol < col) {
						tempList.add(availablePos);
					}
					if ((b != null ) && col > pcol && arow < row && acol > col) {
						tempList.add(availablePos);
					}
					if ((b != null ) && col > pcol && arow > row && acol > col) { // OBS !!!
						tempList.add(availablePos);
					}*/
/*					if (b != null && arow < row && acol >= col) {
						tempList.add(availablePos);
					}
*/
				}
				if (b != null) {
					checkBishopnwest(availablePositions, tempList, removedPos, p);
					checkBishopneast(availablePositions, tempList, removedPos, p);
					checkBishopseast(availablePositions, tempList, removedPos, p);
					checkBishopswest(availablePositions, tempList, removedPos, p);
				}

			}
			for (Position removedPos:opponentRemoved) {
				int row = removedPos.getIntRow();
				int col = removedPos.getIntColumn();
				for (Position availablePos:availablePositions) {
					int arow = availablePos.getIntRow();
					int acol = availablePos.getIntColumn();
					if ((r != null || pn != null ) && prow < row && pcol == col && arow > row && acol == col) { // not applicable when from row > row
						tempList.add(availablePos);
					}
					if ((r != null || pn != null ) && prow > row && pcol == col && arow < row && acol == col) { // Not applicable when the from row < row
						tempList.add(availablePos);
					}
					if (r != null && pcol< col && prow == row && acol < col && arow == row) { //Horizontal to right
						tempList.add(availablePos);
					}
					if (r != null && pcol> col && prow == row && acol < col && arow == row) { //Horizontal to left
						tempList.add(availablePos);
					}
/*
 * Check for bishop	OBS OBS Check again The queen option removed !!!!			
 */
/*					if ((b != null ) && col < pcol && arow > row && acol < col) {
						tempList.add(availablePos);
					}
					if ((b != null ) && col < pcol && arow < row && acol < col) {
						tempList.add(availablePos);
					}
					if ((b != null ) && col > pcol && arow < row && acol > col) {
						tempList.add(availablePos);
					}
					if ((b != null ) && col > pcol && arow > row && acol > col) { // OBS !!!
						tempList.add(availablePos);
					}*/
/*					if (b != null && arow < row && acol >= col) {
						tempList.add(availablePos);
					}
*/
				}
				if (b != null) {
					checkBishopnwest(availablePositions, tempList, removedPos, p);
					checkBishopneast(availablePositions, tempList, removedPos, p);
					checkBishopseast(availablePositions, tempList, removedPos, p);
					checkBishopswest(availablePositions, tempList, removedPos, p);
				}

			}
			for (Position temp:tempList) {
				String posName = temp.getPositionName();
//				logText.append(posName+"\n");
				Position posinTable =  (Position) removedPositions.stream().filter(c -> c.getPositionName().contains(posName)).findAny().orElse(null); // Do not put position in removed table if it is there already
				if (posinTable == null) {
					writer.println("Added removed position "+temp.toString()+" For "+name);
					removedPositions.add(temp);
				}
	
			}
//			tempList = null;
//			p.setRemovedPositions(removedPositions); Must always set the removed positions for all pieces. olj 24.02.21
		}
		p.setRemovedPositions(removedPositions);
/*		if (kn != null) {
			
		}*/
		for (Position availablePos:availablePositions) {
			boolean available = true;
			for (Position removedPos:removedPositions) {
				if (removedPos.getPositionName().equals(availablePos.getPositionName())) {
					available = false;
//					writer.println("Removed position "+removedPos.toString());
					break;
				}

			}
			if (available) {
				preferredPositions.add(availablePos);
			}
		}
/*		if (qt != null) {
			for (Position availablePos:availablePositions) {
				boolean available = true;
				for (Position removedPos:bishopRemoved) {
					if (removedPos.getPositionName().equals(availablePos.getPositionName())) {
						available = false;
//						writer.println("Removed position "+removedPos.toString());
						break;
					}

				}
				if (available) {
					preferredPositions.add(availablePos);
				}
			}
		}*/

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
		resetDirections(availablePositions);
		resetDirections(removedPositions);
		writer.println("Move: "+move.toString()+"\n"+"piece: "+p.toString());
		writer.close();
	return move;
	}


	public Position getHeldPosition() {
		return heldPosition;
	}
	/**
	 * checkBishopnwest
	 * @param availablePositions
	 * @param tempList
	 * @param removedPos
	 * @param piece
	 */
	private void checkBishopnwest(List<Position> availablePositions,List<Position> tempList,Position removedPos,AgamePiece piece) {
		List<XYLocation> nw = piece.getNorthWest();
		XYLocation loc = removedPos.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		for (XYLocation nwloc:nw) {
			int nwx = nwloc.getXCoOrdinate();
			int nwy = nwloc.getYCoOrdinate();
			if (nwx <= x && nwy >= y && removedPos.isNw()) { // If both x and y are less, then this position must be removed
				for (Position pos:availablePositions) {
					int ax = pos.getXyloc().getXCoOrdinate();
					int ay = pos.getXyloc().getYCoOrdinate();
					if (ax == nwx && ay == nwy && pos.isNw() && !pos.isOpponentRemove()) {
						tempList.add(pos);
					}
				}
			}
		}
	}
	/**
	 * @param availablePositions
	 * @param tempList
	 * @param removedPos
	 * @param piece
	 */
	private void checkBishopneast(List<Position> availablePositions,List<Position> tempList,Position removedPos,AgamePiece piece) {
		List<XYLocation> nw = piece.getNorthEast();
		XYLocation loc = removedPos.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		for (XYLocation nwloc:nw) { // All available xylocations north west
			int nwx = nwloc.getXCoOrdinate();
			int nwy = nwloc.getYCoOrdinate();
			if (nwx >= x && nwy >= y && removedPos.isNe()) { // If both x and y are greater, then this position must be removed
				for (Position pos:availablePositions) { // Search all available positions
					int ax = pos.getXyloc().getXCoOrdinate();
					int ay = pos.getXyloc().getYCoOrdinate();
					if (ax == nwx && ay == nwy && pos.isNe() && !pos.isOpponentRemove()) { // If this position contains the current xylocation then
						tempList.add(pos); // remove it
					}
				}
			}
		}
	}
	/**
	 * @param availablePositions
	 * @param tempList
	 * @param removedPos
	 * @param piece
	 */
	private void checkBishopseast(List<Position> availablePositions,List<Position> tempList,Position removedPos,AgamePiece piece) {
		List<XYLocation> nw = piece.getSouthEast();
		XYLocation loc = removedPos.getXyloc(); // The removed position must belong to the same direction.
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		for (XYLocation nwloc:nw) {
			int nwx = nwloc.getXCoOrdinate();
			int nwy = nwloc.getYCoOrdinate();
			if (nwx >= x && nwy <= y && removedPos.isSe()) { // If x is greater and y is less, then this position must be removed
				for (Position pos:availablePositions) {
					int ax = pos.getXyloc().getXCoOrdinate();
					int ay = pos.getXyloc().getYCoOrdinate();
					if (ax == nwx && ay == nwy && pos.isSe() && !pos.isOpponentRemove()) {
						tempList.add(pos);
					}
				}
			}
		}
	}
	/**
	 * @param availablePositions
	 * @param tempList
	 * @param removedPos
	 * @param piece
	 */
	private void checkBishopswest(List<Position> availablePositions,List<Position> tempList,Position removedPos,AgamePiece piece) {
		List<XYLocation> nw = piece.getSouthWest();
		XYLocation loc = removedPos.getXyloc();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		for (XYLocation nwloc:nw) {
			int nwx = nwloc.getXCoOrdinate();
			int nwy = nwloc.getYCoOrdinate();
			if (nwx <= x && nwy <= y && removedPos.isSw()) { // If both x and y are less, then this position must be removed
				for (Position pos:availablePositions) {
					int ax = pos.getXyloc().getXCoOrdinate();
					int ay = pos.getXyloc().getYCoOrdinate();
					if (ax == nwx && ay == nwy && pos.isSw() && !pos.isOpponentRemove()) {
						tempList.add(pos);
					}
				}
			}
		}
	}
	/**
	 * setDirection
	 * This method calculates the directions (nw,ne,sw,se)
	 * for a set of positions and its piece
	 * It is called when the piece is a bishop
	 * @param piece
	 * @param positions
	 */
	/**
	 * @param piece
	 * @param positions
	 */
	public void setDirection(AgamePiece piece, List<Position> positions) {
		List<XYLocation> nw = piece.getNorthWest();
//		Position pos =  (Position) removedPositions.stream().filter(c -> c.getPositionName().contains(posName)).findAny().orElse(null);
		calculateDirections(nw,0,positions);
		List<XYLocation> ne = piece.getNorthEast();
		calculateDirections(ne,1,positions);
		List<XYLocation> sw = piece.getSouthWest();
		calculateDirections(sw,2,positions);
		List<XYLocation> se = piece.getSouthEast();
		calculateDirections(se,3,positions);
	}
	/**
	 * calculateDirections
	 * This is a private method for setDirection
	 * @param nw
	 * @param dr
	 * @param positions
	 */
	private void calculateDirections(List<XYLocation> nw, int dr,List<Position> positions) {
		for (Position pos:positions) {
			XYLocation loc = pos.getXyloc();
			int x = loc.getXCoOrdinate();
			int y = loc.getYCoOrdinate();
			for (XYLocation nwloc:nw) {
				int nwx = nwloc.getXCoOrdinate();
				int nwy = nwloc.getYCoOrdinate();
				if (x == nwx && y == nwy && dr == 0)
					pos.setNw(true);
				if (x == nwx && y == nwy && dr == 1)
					pos.setNe(true);
				if (x == nwx && y == nwy && dr == 2)
					pos.setSw(true);
				if (x == nwx && y == nwy && dr == 3)
					pos.setSe(true);
			}
		}
	}
	/**
	 * resetDirections
	 * This method resets all directions for a position
	 * @param positions
	 */
	private void resetDirections(List<Position> positions) {
		for (Position pos:positions) {
			pos.setNe(false);
			pos.setNw(false);
			pos.setSe(false);
			pos.setSw(false);
		}
	}
}
