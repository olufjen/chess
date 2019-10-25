package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.GamePiece;

/**
 * This class represent the Pawn chesspiece 
 * It implements the method legalMoves for the Pawn 
 * @author oluf
 * @param <P>
 *
 */
public class APawn extends AbstractGamePiece<Position>  implements ChessPieceType {

	private pieceType localType = pieceType.PAWN;
	private pieceColor localColor;
	private int[][] reachablesqueres;
	private String[][] reachablepiecePosition;
	private HashMap<String,Position> newPositions; // contains positions reachable by the piece 
	private HashMap<String,Position> ontologyPositions; // Represent the ontology positions
	private int size = 8;
	private String color;
	private ChessPiece myPiece;
	private Position myPosition;
	
	public APawn() {
		super();
		reachablesqueres = new int[size][size];
		reachablepiecePosition = new String[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				reachablesqueres[i][j] = 0;
			}
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				reachablepiecePosition[i][j] = null;
			}
		}
	}


	public APawn(Position myPosition, ChessPiece myPiece) {
		super();
		color = myPiece.getColor();
		if (color.equals("w"))
			localColor = pieceColor.WHITE;
		else
			localColor = pieceColor.BLACK;
		this.myPiece = myPiece;
		this.myPosition = myPosition;
		reachablesqueres = new int[size][size];
		reachablepiecePosition = new String[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				reachablesqueres[i][j] = 0;
			}
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				reachablepiecePosition[i][j] = null;
			}
		}
		getLegalmoves(myPosition);
	}

	public APawn(Position myPosition) {
		super();
		reachablesqueres = new int[size][size];
		reachablepiecePosition = new String[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				reachablesqueres[i][j] = 0;
			}
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				reachablepiecePosition[i][j] = null;
			}
		}
		getLegalmoves(myPosition);
	}


	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public ChessPiece getMyPiece() {
		return myPiece;
	}

	public void setMyPiece(ChessPiece myPiece) {
		this.myPiece = myPiece;
	}

	public Position getMyPosition() {
		return myPosition;
	}

	public void setMyPosition(Position myPosition) {
		this.myPosition = myPosition;
	}

	@Override
	public boolean checkName(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public  boolean test(GamePiece piece) {
		return piece.getPieceType() == localType;
	}
	public int getSize() {
		return reachablesqueres.length;
	}
	public void clear() {
		for (int i = 0; i < getSize(); i++) {
			for (int j = 0; j < getSize(); j++) {
				reachablesqueres[i][j] = 0;
			}
		}
		for (int i = 0; i < getSize(); i++) {
			for (int j = 0; j < getSize(); j++) {
				reachablepiecePosition[i][j] = null;
			}
		}
	}
	
	public pieceType getLocalType() {
		return localType;
	}

	public void setLocalType(pieceType localType) {
		this.localType = localType;
	}

	public int[][] getReachablesqueres() {
		return reachablesqueres;
	}

	public void setReachablesqueres(int[][] reachablesqueres) {
		this.reachablesqueres = reachablesqueres;
	}

	public String[][] getReachablepiecePosition() {
		return reachablepiecePosition;
	}

	public void setReachablepiecePosition(String[][] reachablepiecePosition) {
		this.reachablepiecePosition = reachablepiecePosition;
	}



	/**
	 * getLegalmoves
	 * This method returns reachable positions by the Pawn chesspiece
	 * This method is called when the piece is created, and when the piece is moved.
	 * When the piece is moved it is called from the local produceLegalmoves method
	 * @param position
	 * @return
	 */
	public void getLegalmoves(Position position){
	
		XYLocation loc = position.getXyloc();
		String posName = position.getPositionName();
		APawnMoveRuler pawnRules = new APawnMoveRuler();
		List<XYLocation> locations = ChessFunctions.moveRule(this, pawnRules);
		if (newPositions == null)
			newPositions = new HashMap();
		for (XYLocation xloc:locations) {
			int x = xloc.getXCoOrdinate();
			int y = xloc.getYCoOrdinate();
			reachablesqueres[x][y] = 1;
			reachablepiecePosition[x][y] = "P";
			createPosition(newPositions, x, y);
		}
/*		
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		if (y != 0 && color.equals("w")) {  
			if (y < 7 && y > 1) {
				reachablesqueres[x][y+1] = 1;
				reachablepiecePosition[x][y+1] = "P";
				createPosition(newPositions, x, y+1);

			}
		}
		if (y != 0 && color.equals("b")) {  
			if (y >1 && y < 6) {
				reachablesqueres[x][y-1] = 1;
				reachablepiecePosition[x][y-1] = "P";
				createPosition(newPositions, x, y-1);

			}
		}
		if (y == 1 && color.equals("w")) {
			reachablesqueres[x][y+1] = 1;
			reachablepiecePosition[x][y+1] = "P";
			createPosition(newPositions, x, y+1);
			reachablesqueres[x][y+2] = 1;
			reachablepiecePosition[x][y+2] = "P";
			createPosition(newPositions, x, y+2);
		}	
		if (y == 6 && color.equals("b")) {
			reachablesqueres[x][y-1] = 1;
			reachablepiecePosition[x][y-1] = "P";
			createPosition(newPositions, x, y-1);
			reachablesqueres[x][y-2] = 1;
			reachablepiecePosition[x][y-2] = "P";
			createPosition(newPositions, x, y-2);
		}*/
		
	}
	/**
	 * createPosition
	 * This method creates a new chess position based on a XYLocation
	 * @param newPositions
	 * @param x
	 * @param y
	 */
	private void createPosition(HashMap<String,Position> newPositions,int x,int y) {
		XYLocation newloc = new XYLocation(x,y);
		Position newPosxyp = new Position(newloc,false,null);
		newPositions.put(newPosxyp.getPositionName(), newPosxyp);
	}

	public HashMap<String,Position> getNewPositions() {
		return newPositions;
	}

	@Override
	public HashMap getLegalmoves() {
		return newPositions;
		
	}

	@Override
	public pieceType getPieceType() {
		
		return localType;
	}

	@Override
	public Position getmyPosition() {
		
		return myPosition;
	}

	/*
	 * produceLegalmoves
	 * This method produces new legal moves after the piece has moved
	 * This method is called when the piece is moved to a new position
	 * @Param Position the new position for the piece
	 * 
	*/
	@Override
	public void produceLegalmoves(Position position) {
		newPositions.clear();
		myPosition = position;
		getLegalmoves(position);
		createontPosition(newPositions);
	}
	/**
	 * createontPosition
	 * This method moves any ontologypositions to the list of positions reachable by this piece
	 * It is called from the determinPieceType method of the AgamePiece object and the produceLegalmoves method
	 * @param newPositions a HashMap of positions calculated by the piecetype

	 */
	protected void createontPosition(HashMap<String,Position> newPositions) {
//		XYLocation newloc = new XYLocation(x,y);
		List<Position> tempPositions = new ArrayList(newPositions.values());
		for (Position pos : tempPositions) {
			String name = pos.getPositionName();
			Position ontPosition = ontologyPositions.get(name);
			if (ontPosition != null) {
				newPositions.put(name, ontPosition);
			}
		}

	}
	
	@Override
	public pieceColor getPieceColor() {
		
		return localColor;
	}


	@Override
	public HashMap<String, Position> getOntologyPositions() {
		
		return this.ontologyPositions;
	}


	@Override
	public void setOntologyPositions(HashMap<String, Position> ontologyPositions) {
		this.ontologyPositions = ontologyPositions;
		
	}




}
