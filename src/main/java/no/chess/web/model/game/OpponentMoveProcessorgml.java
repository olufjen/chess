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

import no.chess.web.model.Position;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.ChessProcessor;

/**
 * OpponentMoveProcessor
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
public class OpponentMoveProcessorgml implements ChessProcessor<APlayer,AgamePiece, ApieceMove> {

	private String outputFileName = "C:\\Users\\bruker\\Google Drive\\privat\\ontologies\\analysis\\";
	private PrintWriter writer = null;
	private Integer processNumber; // The process number is created by the piece's introw*10 + intcolumn position
	private FileWriter fw = null;
	private Position heldPosition; // This is the position held by the piece under consideration
	private List<Position> removedPositions = null;
	private List<Position> attackedPositions = null;
	private List<Position> bishopRemoved = null; // This list contains removed positions for the queen in bishop movements
	public OpponentMoveProcessorgml(Integer processNumber,String pname) {
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


	public Position getHeldPosition() {
		return heldPosition;
	}


	public List<Position> getRemovedPositions() {
		return removedPositions;
	}


	public void setRemovedPositions(List<Position> removedPositions) {
		this.removedPositions = removedPositions;
	}


	public List<Position> getAttackedPositions() {
		return attackedPositions;
	}


	public void setAttackedPositions(List<Position> attackedPositions) {
		this.attackedPositions = attackedPositions;
	}


	/* (non-Javadoc)
	 * @see no.games.chess.ChessProcessor#processChessObject(java.lang.Object, java.lang.Object)
	 */
	@Override
	public ApieceMove processChessObject(APlayer p, AgamePiece q) {
		HashMap<String,Position> reacablePositions = null;
		Position from = q.getmyPosition();
		HashMap<String,Position> bishopPositions = null;
		List<Position> queenscastlePositions = null;
		List<Position> queenbishopPositions = null;
//		List<Position>opponentRemoved = action.getOpponentRemoved();
		heldPosition = from;
		List<Position> tempList = null;	
		APawn pn = null;
		ABishop b = null;
		ARook r = null;
		AQueen qt = null;
		AKnight kn = null;
		Aking king = null;
		ChessPieceType pieceType = q.getChessType();
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
//			writer.println("the piece is a queen ");
			bishopRemoved = q.getBishopRemoved();
			bishopPositions = q.getBishopPositions();
			reacablePositions = q.getReacablePositions();
			queenscastlePositions = new ArrayList(reacablePositions.values());
			queenbishopPositions = new ArrayList(bishopPositions.values());
			tempList = new ArrayList<>();
			int pcol = from.getIntColumn();
			int prow = from.getIntRow();
			for (Position removedPos:bishopRemoved) { // OBS: The removed positions must be separated: bishop positions and rook positions
				int row = removedPos.getIntRow();
				int col = removedPos.getIntColumn();
				String pName = removedPos.getPositionName();
/*				if (pName.equals("c2") || pName.equals("e2")) {
					System.out.println("Pos !!! "+pName);
				}*/
				for (Position availablePos:queenbishopPositions) {
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
				}
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
		
		List<AgamePiece> pieces = p.getMygamePieces(); 
		List<Position> piecePositions = null;
		
		piecePositions = q.getNewlistPositions();
		HashMap<String,Position> attackPositions = q.getAttackPositions();
		if (attackPositions != null && !attackPositions.isEmpty()) {
			attackedPositions = new ArrayList(attackPositions.values());
		}
		List<Position> removedPositions = new ArrayList();
		for (Position position:piecePositions) {
			for (AgamePiece otherPiece:pieces) {
				if (otherPiece != q) {
					Position pos = otherPiece.getMyPosition();
					if (pos != null) {
						if (otherPiece.getMyPosition().getPositionName().equals(position.getPositionName())) {
							removedPositions.add(position);
						}

					}
				}
			}

		
		}
		if (b != null || r != null || qt != null || pn != null) {
			String pName = q.getMyPiece().getOntlogyName();
			writer.println("Checking additional removals For "+pName);
			tempList = new ArrayList<>();
			int pcol = from.getIntColumn();
			int prow = from.getIntRow();
			for (Position removedPos:removedPositions) {
				int row = removedPos.getIntRow();
				int col = removedPos.getIntColumn();
				for (Position availablePos:piecePositions) {
					int arow = availablePos.getIntRow();
					int acol = availablePos.getIntColumn();
					if ((r != null || pn != null) && prow < row && pcol == col && arow > row && acol == col) { // not applicable when from row > row
						tempList.add(availablePos);
					}
					if ((r != null || pn != null) && prow > row && pcol == col && arow < row && acol == col) { // Not applicable when the from row < row
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
					if ((b != null ) && col < pcol && arow > row && acol < col) {
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
			q.setRemovedPositions(removedPositions);
		}
		return null;
	}



}
