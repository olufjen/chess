package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.GamePiece;
import no.games.chess.AbstractGamePiece.pieceColor;

/**
 * AgamePiece represent a chesspiece owned by the chessplayer.
 * Each piece knows what its legal moves are from its current position.
 * The getLegalmoves method returns a list of new positions reachable from the piece's current position.
 * OBS: The new reachable positions must be compared to the positions held in ontlogyPositions.
 * AgamePiece contains two enum type variables:
 * pieceType for PAWN,ROOK,KNIGHT,BISHOP,QUEEN,KING
 * pieceColor for BLACK or WHITE.
 * 
 * @author oluf 
 *
 * @param <P>
 */
public class AgamePiece extends AbstractGamePiece<Position>{

	private Position myPosition;
	private Position heldPosition = null; // This position is used to hold former position if piece is removed from board       
	private ChessPiece myPiece; // Represent the ontology chesspiece
	private String color;
	private pieceType myType;
	private pieceColor localColor;
	private ChessPieceType chessType = null;
	private HashMap<String,Position> ontologyPositions; // Represent the ontology positions
	private HashMap<String,Position> reacablePositions;
	private ArrayList<Position> newPositions;
	private List<Position> preferredPositions;
	private Stack<Position> heldPositions;
	private boolean active = true; // Set if piece is active participating, set to false when removed from board
	
	public AgamePiece(Position myPosition) {
		super();
		this.myPosition = myPosition;
		
	}

	public AgamePiece(Position myPosition, ChessPiece myPiece) {
		super();
		this.myPosition = myPosition;
		this.myPiece = myPiece;
		heldPositions = new Stack();
		
//		determinePieceType(); Moved to setOntologyPositions: Then new available positions are replaced by ontology positions
	}

	public AgamePiece() {
		super();
		
	}
	
	public HashMap<String, Position> getOntologyPositions() {
		return ontologyPositions;
	}

	/**
	 * setOntologyPositions
	 * This method is called from AchessGame so that onlologypositions are available
	 * Then determinePieceType is called
	 * @param ontologyPositions
	 */
	public void setOntologyPositions(HashMap<String, Position> ontologyPositions) {
		this.ontologyPositions = ontologyPositions;
		determinePieceType();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	public void restoreValue() {
		value = orgValue;
	}
	public pieceColor getLocalColor() {
		return localColor;
	}

	public void setLocalColor(pieceColor localColor) {
		this.localColor = localColor;
	}

	public ChessPieceType getChessType() {
		return chessType;
	}

	public void setChessType(ChessPieceType chessType) {
		this.chessType = chessType;
	}
	public boolean checkBlack() {
		return localColor == pieceColor.BLACK;
	}
	public boolean checkWhite() {
		return localColor == pieceColor.WHITE;
	}
	/**
	 * createPosition
	 * This method moves any ontologypositions to the list of positions reachable by this piece
	 * It is called from the determinPieceType method
	 * @param newPositions a HashMap of positions calculated by the piecetype

	 */
	protected void createPosition(HashMap<String,Position> newPositions) {
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
	
	public void determinMytype() {
		ChessPieceType king = new Aking();
		king.test(this);
		
		
	}
	
	/**
	 * determinePieceType
	 * This method determines the type of the piece:
	 * Pawn, Rook, Knight,Bishop,King and Queen
	 */
	public void determinePieceType() {
		String name = myPiece.getPieceName();
		color = myPiece.getColor();
		if (color.equals("w"))
			localColor = pieceColor.WHITE;
		else
			localColor = pieceColor.BLACK;
		GamePiece gamePiece = null;
		switch(name) {
		
			case "P":
				myType = pieceType.PAWN;
				chessType = new APawn(myPosition,myPiece);
				gamePiece = (GamePiece) chessType;
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println("Pawn positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;
			case"B":	
				myType = pieceType.BISHOP;
				chessType = new ABishop(myPosition,myPiece);
				String pieceColorB = myPiece.getColor();
				gamePiece = (GamePiece) chessType;
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
//				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println(pieceColorB+" Bishop positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;	
			case "N":
				myType = pieceType.KNIGHT;
				chessType = new AKnight(myPosition,myPiece);
				String pieceColorK = myPiece.getColor();
				gamePiece = (GamePiece) chessType;
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
//				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println("Knight positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}	*/
				break;
			case "K":
				myType = pieceType.KING;
				chessType = new Aking(myPosition,myPiece);
				gamePiece = (GamePiece) chessType;
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
//				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println("King positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;
			case "R":
				myType = pieceType.ROOK;
				chessType = new ARook(myPosition,myPiece);
				String pieceColorR = myPiece.getColor();
				gamePiece = (GamePiece) chessType;
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
//				reacablePositions = chessType.getNewPositions();
				newPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newPositions) {
					System.out.println(pieceColorR+" Rook positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;
			case "Q":
				myType = pieceType.QUEEN;
				chessType = new AQueen(myPosition,myPiece);
				String pieceColorQ = myPiece.getColor();
				gamePiece = (GamePiece) chessType;
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
//				reacablePositions = chessType.getNewPositions();
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
	
	public List<Position> getPreferredPositions() {
		return preferredPositions;
	}

	public void setPreferredPositions(List<Position> preferredPositions) {
		this.preferredPositions = preferredPositions;
	}

	/**
	 * getlegalMoves
	 * This methods return a set of legal moves available for the GamePiece
	 * @return A HashMap of reachable positions
	 */	
	@Override
	public HashMap<String,Position> getLegalmoves() {
			return (HashMap<String, Position>) reacablePositions;
	}

	@Override
	public Position getmyPosition() {
		return (Position) myPosition;
		
	}

	public Position getMyPosition() {
		return myPosition;
	}

	public void setMyPosition(Position myPosition) {
		if (this.myPosition != null)
			heldPositions.push(this.myPosition);
		this.myPosition = myPosition;
	}

	/**
	 * setMypositionEmpty
	 * This method is used to save last used position in case the piece is removed from board
	 * @deprecated !!?? as of December 2019
	 * @param position
	 */
	public void setMypositionEmpty(Position position) {
		heldPosition = myPosition;
		myPosition = position;
		if (myPosition == null && heldPosition == null) {
			System.out.println("Both positions empty !!--");
		}
	}
	/**
	 * restorePosition
	 * This method is used to restore last held position in case a piece has been removed from the board
	 */
	public void restorePosition() {
		if (heldPositions != null && !heldPositions.isEmpty())
			this.myPosition = heldPositions.pop();
		active = true;
		
//		myPosition = heldPosition;
	}
	
	public Position getHeldPosition() {
		return heldPosition;
	}

	public void setHeldPosition(Position heldPosition) {
		this.heldPosition = heldPosition;
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



	public void setNewPositions(ArrayList<Position> newPositions) {
		this.newPositions = newPositions;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		String posName = "Removed!!!";
		XYLocation localXY = new XYLocation(0,0);
		
		if (myPosition != null) {
			posName = myPosition.getPositionName();
			localXY = myPosition.getXyloc();
		}
		
		result.append("Piece position"+posName + " X, Y "+localXY.toString()  + " "+myPiece.getName() + " "+ myType);
	
		String na = myPiece.getPieceName();
		result.append("Name " + na + "\n" + "Available positions\n");
		for (Position pos:newPositions) {
			result.append("Position: "+pos.getPositionName() + " " + pos.getPositionColor() + " X, Y "+pos.getXyloc().toString() + "\n");

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

	/**
	 * @deprecated as of December 2019
	 * @param position
	 */
	@Override
	public void getLegalmoves(Position position) {
		AgamePiece localpiece = (AgamePiece) chessType;
		localpiece.setMyPosition(position);
		localpiece.produceLegalmoves(position);
		reacablePositions = localpiece.getNewPositions();
//		reacablePositions =  chessType.getNewPositions();
		newPositions = null;
		newPositions = new ArrayList(reacablePositions.values());
//		newPositions =  (List<Position>) reacablePositions.values();
	}

	/* (non-Javadoc)
	 * @see no.games.chess.AbstractGamePiece#produceLegalmoves(java.lang.Object)
	 */
	@Override
	public void produceLegalmoves(Position position) {

		if (chessType instanceof APawn) {
			APawn p = (APawn) chessType;
			p.produceLegalmoves(position);
		}
		if (chessType instanceof AKnight) {
			AKnight p = (AKnight) chessType;
			p.produceLegalmoves(position);
		}
		if (chessType instanceof ARook) {
			ARook p = (ARook) chessType;
			p.produceLegalmoves(position);
		}	
		if (chessType instanceof ABishop) {
			ABishop p = (ABishop) chessType;
			p.produceLegalmoves(position);
		}
		if (chessType instanceof AQueen) {
			AQueen p = (AQueen) chessType;
			p.produceLegalmoves(position);
		}
		if (chessType instanceof Aking) {
			Aking p = (Aking) chessType;
			p.produceLegalmoves(position);
		}

/*		Optional<ARook> r = Optional.ofNullable((ARook) chessType);
		Optional<AKnight> n = Optional.ofNullable((AKnight) chessType);
		Optional<AQueen> q = Optional.ofNullable((AQueen) chessType);
		Optional<Aking> k = Optional.ofNullable((Aking) chessType);*/
/*		Optional<APawn> p = Optional.ofNullable(ChessFunctions.findpieceType(localpiece,(APawn piece) -> piece.getPieceType() == pieceType.PAWN));
		ABishop b = ChessFunctions.findpieceType(localpiece,(AgamePiece piece) -> piece.getPieceType() == pieceType.BISHOP);
		ARook r = ChessFunctions.findpieceType(localpiece,(AgamePiece piece) -> piece.getPieceType() == pieceType.ROOK);
		AKnight n = ChessFunctions.findpieceType(localpiece,(AgamePiece piece) -> piece.getPieceType() == pieceType.KNIGHT);
		AQueen q = ChessFunctions.findpieceType(localpiece,(AgamePiece piece) -> piece.getPieceType() == pieceType.QUEEN);
		Aking k = ChessFunctions.findpieceType(localpiece,(AgamePiece piece) -> piece.getPieceType() == pieceType.KING);
		if (k != null)
			k.produceLegalmoves(position);*/
		GamePiece gamePiece = (GamePiece) chessType;
		reacablePositions = gamePiece.getNewPositions();	
//		reacablePositions =  chessType.getNewPositions();
		newPositions = null;
		newPositions = new ArrayList(reacablePositions.values());
		
	}

	public boolean checkPositions() {
		boolean newPos = false;
		if (newPositions == null || newPositions.isEmpty()) {
			newPositions = null;
			newPositions = new ArrayList(reacablePositions.values());
			newPos = true;
		}
		return newPos;
	}
	@Override
	public pieceColor getPieceColor() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public HashMap<String, Position> getNewPositions() {
		
		return reacablePositions;
	}



	
	
}
