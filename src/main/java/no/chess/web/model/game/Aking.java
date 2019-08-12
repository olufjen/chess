package no.chess.web.model.game;

import java.util.HashMap;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.ChessPieceType;
import no.games.chess.GamePiece;

/**
 * This class represent the King chesspiece 
 * It implements the method legalMoves for the King 
 * @author oluf
 * @param <P>
 *
 */
public class Aking<P> extends AbstractGamePiece<P>  implements ChessPieceType {

	private pieceType localType = pieceType.KING;
	private int[][] reachablesqueres;
	private String[][] reachablepiecePosition;
	HashMap<String,Position> newPositions;
	private int size = 8;
	private String color;
	private ChessPiece myPiece;
	private Position myPosition;
	
	public Aking() {
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

	public Aking(Position myPosition, ChessPiece myPiece) {
		super();
		color = myPiece.getColor();
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

	public Aking(Position myPosition) {
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
		if (y != 0 && x != 0) { // OBS BAck king at e8 !!!! 
			if (y < 7) {
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
		if (y != 0 && x == 0) {
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
		if (y == 0 && x != 0) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public P getmyPosition() {
		return (P) myPosition;
		
	}


}
