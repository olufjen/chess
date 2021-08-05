package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.ChessPieceType;
import no.games.chess.GamePiece;
import no.games.chess.AbstractGamePiece.pieceColor;

/**
 * This class represent the King chesspiece 
 * It implements the method legalMoves for the King 
 * @author oluf
 * @param <P>
 *
 */
public class Aking extends AbstractGamePiece<Position>  implements ChessPieceType {

	private pieceType localType = pieceType.KING;
	private pieceColor localColor;
	private int[][] reachablesqueres;
	private String[][] reachablepiecePosition;
	private int[][] castlesqueres;
	private String[][] castlepositions;
	private HashMap<String,Position> castlePositions;
	private HashMap<String,Position> newPositions;
	private HashMap<String,Position> ontologyPositions; // Represent the ontology positions
	private int size = 8;
	private String color;
	private ChessPiece myPiece;
	private Position myPosition;
	private int castlex = 2;
	private int castley = 0;
	private int castlexx = 6;
	
	public Aking() {
		super();
		reachablesqueres = new int[size][size];
		reachablepiecePosition = new String[size][size];
		castlesqueres = new int[size][size];
		castlepositions = new String[size][size];
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
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				castlesqueres[i][j] = 0;
			}
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				castlepositions[i][j] = null;
			}
		}
	}

	public Aking(Position myPosition, ChessPiece myPiece) {
		super();
		color = myPiece.getColor();
		if (color.equals("w")) {
			localColor = pieceColor.WHITE;
		}
		else {
			localColor = pieceColor.BLACK;
			castley = 7;
		}
		this.myPiece = myPiece;
		this.myPosition = myPosition;
		reachablesqueres = new int[size][size];
		reachablepiecePosition = new String[size][size];
		castlesqueres = new int[size][size];
		castlepositions = new String[size][size];
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
		if (castlePositions == null)
			castlePositions = new HashMap();
		getLegalmoves(myPosition);
	}

	public Aking(Position myPosition) {
		super();
		reachablesqueres = new int[size][size];
		reachablepiecePosition = new String[size][size];
		castlesqueres = new int[size][size];
		castlepositions = new String[size][size];
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
		if (castlePositions == null)
			castlePositions = new HashMap();
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

	public HashMap<String, Position> getCastlePositions() {
		return castlePositions;
	}

	public void setCastlePositions(HashMap<String, Position> castlePositions) {
		this.castlePositions = castlePositions;
	}

	/**
	 * makeCastlemove
	 * This method creates positions for the castle move for the King
	 */
	public void makeCastlemove() {
		castlesqueres[castlex][castley] = 1;
		castlepositions[castlex][castley] = "K";
		createPosition(castlePositions, castlex, castley);
		castlesqueres[castlexx][castley] = 1;
		castlepositions[castlexx][castley] = "K";
		createPosition(castlePositions, castlexx, castley);
		createontPosition(castlePositions);
	}

	/**
	 * getLegalmoves
	 * This method returns reachable positions by the King chesspiece
	 * @param position
	 * @return
	 */
	public void getLegalmoves(Position position){
		XYLocation loc = position.getXyloc();
		String posName = position.getPositionName();
		newPositions = new HashMap();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		if (y != 0 && x != 0) { // OBS Black king at e8 !!!!  
			if (y < 7 && x < 7) {
				reachablesqueres[x][y+1] = 1;
				reachablepiecePosition[x][y+1] = "K";
				createPosition(newPositions, x, y+1);
				reachablesqueres[x+1][y+1] = 1;
				reachablepiecePosition[x+1][y+1] = "K";
				createPosition(newPositions, x+1, y+1);
				reachablesqueres[x-1][y+1] = 1;
				reachablepiecePosition[x-1][y+1] = "K";
				createPosition(newPositions, x-1, y+1);
			}
			reachablesqueres[x][y-1] = 1;
			reachablepiecePosition[x][y-1] = "K";
			createPosition(newPositions, x, y-1);
			if (x < 7) {
				reachablesqueres[x+1][y] = 1;
				reachablepiecePosition[x+1][y] = "K";
				createPosition(newPositions, x+1, y);
				reachablesqueres[x+1][y-1] = 1;
				reachablepiecePosition[x+1][y-1] = "K";
				createPosition(newPositions, x+1, y-1);
			}
			reachablesqueres[x-1][y] = 1;
			reachablepiecePosition[x-1][y] = "K";
			createPosition(newPositions, x-1, y);
			reachablesqueres[x-1][y-1] = 1;
			reachablepiecePosition[x-1][y-1] = "K";
			createPosition(newPositions, x-1, y-1);
		}
		if (y != 0 && x == 0 && y < 7) { //  && y < 7 added 29.06.21			
			reachablesqueres[x][y+1] = 1;
			reachablepiecePosition[x][y+1] = "K";
			createPosition(newPositions, x, y+1);
			reachablesqueres[x][y-1] = 1;
			reachablepiecePosition[x][y-1] = "K";
			createPosition(newPositions, x, y-1);
			reachablesqueres[x+1][y+1] = 1;
			reachablepiecePosition[x+1][y+1] = "K";
			createPosition(newPositions, x+1, y+1);
			reachablesqueres[x+1][y] = 1;
			reachablepiecePosition[x+1][y] = "K";
			createPosition(newPositions, x+1, y);
			reachablesqueres[x+1][y-1] = 1;
			reachablepiecePosition[x+1][y-1] = "K";
			createPosition(newPositions, x+1, y-1);
		}	
		if (y == 0 && x != 0 && x < 7) { // && x < 7 added 29.06.21
			reachablesqueres[x][y+1] = 1;
			reachablepiecePosition[x][y+1] = "K";
			createPosition(newPositions, x, y+1);
			reachablesqueres[x+1][y+1] = 1;
			reachablepiecePosition[x+1][y+1] = "K";
			createPosition(newPositions, x+1, y+1);
			reachablesqueres[x-1][y+1] = 1;
			reachablepiecePosition[x-1][y+1] = "K";
			createPosition(newPositions, x-1, y+1);
			reachablesqueres[x+1][y] = 1;
			reachablepiecePosition[x+1][y] = "K";
			createPosition(newPositions, x+1, y);
			reachablesqueres[x-1][y] = 1;
			reachablepiecePosition[x-1][y] = "K";
			createPosition(newPositions, x-1, y);
		}
		if (y == 0 && x == 0) {
			reachablesqueres[x][y+1] = 1;
			reachablepiecePosition[x][y+1] = "K";
			createPosition(newPositions, x, y+1);
			reachablesqueres[x+1][y+1] = 1;
			reachablepiecePosition[x+1][y+1] = "K";
			createPosition(newPositions, x+1, y+1);
			reachablesqueres[x+1][y] = 1;
			reachablepiecePosition[x+1][y] = "K";
			createPosition(newPositions, x+1, y);
		}
		
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
	@Override
	public HashMap<String,Position> getNewPositions() {
		return newPositions;
	}

	@Override
	public HashMap getLegalmoves() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public pieceType getPieceType() {
		
		return localType;
	}

	@Override
	public Position getmyPosition() {
		
		return myPosition;
	}

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
	 * It is called from the determinPieceType method and the produceLegalmove method
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
