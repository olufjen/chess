package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.ChessPieceType;
import no.games.chess.GamePiece;

/**
 * AgamePiece represent a chesspiece owned by the chessplayer.
 * Each piece knows what its legal moves are from its current position.
 * The getLegalmoves method returns a list of new positions reachable from the piece's current position.
 * @author oluf 
 *
 * @param <P>
 */
public class AgamePiece<P> extends AbstractGamePiece<P>{

	private Position myPosition;
	private ChessPiece myPiece; // Represent the ontology chesspiece
	private String color;
	private pieceType myType;
	private ChessPieceType chessType = null;
	private HashMap<String,Position> reacablePositions;
	private ArrayList<Position> newPositions;
	
	public AgamePiece(Position myPosition) {
		super();
		this.myPosition = myPosition;
	}

	public AgamePiece(Position myPosition, ChessPiece myPiece) {
		super();
		this.myPosition = myPosition;
		this.myPiece = myPiece;
		determinePieceType();
	}

	public AgamePiece() {
		super();
		
	}
	/**
	 * createPosition
	 * This method creates a new chess position based on a XYLocation
	 * @param newPositions
	 * @param x
	 * @param y
	 */
	protected void createPosition(HashMap<String,Position> newPositions,int x,int y) {
		XYLocation newloc = new XYLocation(x,y);
		Position newPosxyp = new Position(newloc,false,null);
		newPositions.put(newPosxyp.getPositionName(), newPosxyp);
	}
	public void determinMytype() {
		ChessPieceType king = new Aking();
		king.test(this);
		
		
	}
	public void determinePieceType() {
		String name = myPiece.getPieceName();
		color = myPiece.getColor();
		switch(name) {
			case "P":
				myType = pieceType.PAWN;
				chessType = new APawn(myPosition,myPiece);
				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println("Pawn positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;
			case"B":	
				myType = pieceType.BISHOP;
				chessType = new ABishop(myPosition,myPiece);
				String pieceColorB = myPiece.getColor();
				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
				for (Position pos:newPositions) {
					System.out.println(pieceColorB+" Bishop positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}
				break;	
			case "N":
				myType = pieceType.KNIGHT;
				chessType = new AKnight(myPosition,myPiece);
				String pieceColorK = myPiece.getColor();
				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println("Knight positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}	*/
				break;
			case "K":
				myType = pieceType.KING;
				chessType = new Aking(myPosition,myPiece);
				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println("King positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;
			case "R":
				myType = pieceType.ROOK;
				chessType = new ARook(myPosition,myPiece);
				String pieceColorR = myPiece.getColor();
				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println(pieceColorR+" Rook positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;
			case "Q":
				myType = pieceType.QUEEN;
				chessType = new AQueen(myPosition,myPiece);
				String pieceColorQ = myPiece.getColor();
				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println(pieceColorQ+" Queens positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/				
				break;
			default:
				myType = pieceType.PAWN;
				break;
		}
	}
	/**
	 * getlegalMoves
	 * This methods return a set of legal moves available for the GamePiece
	 * @return A HashMap of reachable positions
	 */	
	@Override
	public HashMap<String,P> getLegalmoves() {
			return (HashMap<String, P>) reacablePositions;
	}

	@Override
	public P getmyPosition() {
		return (P) myPosition;
		
	}

	public Position getMyPosition() {
		return myPosition;
	}

	public void setMyPosition(Position myPosition) {
		this.myPosition = myPosition;
	}

	public ChessPiece getMyPiece() {
		return myPiece;
	}

	public void setMyPiece(ChessPiece myPiece) {
		this.myPiece = myPiece;
	}

	@Override
	public pieceType getPieceType() {
		
		return myType;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public pieceType getMyType() {
		return myType;
	}

	public void setMyType(pieceType myType) {
		this.myType = myType;
	}

	public HashMap<String, Position> getReacablePositions() {
		return reacablePositions;
	}

	public void setReacablePositions(HashMap<String, Position> reacablePositions) {
		this.reacablePositions = reacablePositions;
	}

	public ArrayList<Position> getNewPositions() {
		return newPositions;
	}

	public void setNewPositions(ArrayList<Position> newPositions) {
		this.newPositions = newPositions;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(myPosition.getPositionName() + " "+myPiece.getName() + " "+ myType);
	
		String na = myPiece.getPieceName();
		result.append("Name " + na + "\n" + "Available positions\n");
		for (Position pos:newPositions) {
			String cl = myPiece.getColor();
			result.append("Position color " + cl+" positions: "+pos.getPositionName() + " " + pos.getPositionColor() + "\n");

		}			
		return result.toString();
	}

	public boolean test(GamePiece piece) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean checkName(String name) {
		// TODO Auto-generated method stub
		return false;
	}



	
	
}
