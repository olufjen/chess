package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.Arrays;
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
	private HashMap<String,Position> bishopPositions; // Are only valid for queen
	private HashMap<String,Position> attackPositions; // Are only valid for type pawn
	private HashMap<String,Position> castlePositions; // Are only valid for king and rook
	private ArrayList<Position> newlistPositions; // Refactored from newPositions olj 09.11.20 IUt contains all reachable positions
	private List<Position> removedPositions = null;
	private List<Position> preferredPositions;
	private Stack<Position> heldPositions;
	private int nofMoves = 0; // Keep track of how many moves the piece has been involved in
	private List<Integer> moveNumbers = null; // Contains all the movenumbers this piece has been involved in
	private boolean active = true; // Set if piece is active participating, set to false when removed from board
	private String predicate = "none"; //The predicate used by this object
	private List<String> predicates; 
	private boolean castlingMove = false;
	
	public AgamePiece(Position myPosition) {
		super();
		this.myPosition = myPosition;
		
	}

	public AgamePiece(Position myPosition, ChessPiece myPiece) {
		super();
		this.myPosition = myPosition;
		this.myPiece = myPiece;
		heldPositions = new Stack();
		moveNumbers = new ArrayList<Integer>();
		predicates = new ArrayList<String>();
//		determinePieceType(); Moved to setOntologyPositions: Then new available positions are replaced by ontology positions
	}

	public AgamePiece() {
		super();
		
	}
	
	public boolean isCastlingMove() {
		return castlingMove;
	}

	public void setCastlingMove(boolean castlingMove) {
		this.castlingMove = castlingMove;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
		String str[] = this.predicate.split(";");
		predicates = Arrays.asList(str);
	}
	public String returnPredicate() {
		String pred = predicates.get(0);
		String endPredicate[] = pred.split(":");
		return endPredicate[0];
	}
	public List<String> getPredicates() {
		return predicates;
	}

	public void setPredicates(List<String> predicates) {
		this.predicates = predicates;
	}

	public HashMap<String, Position> getOntologyPositions() {
		return ontologyPositions;
	}

	public HashMap<String, Position> getCastlePositions() {
		return castlePositions;
	}

	public void setCastlePositions(HashMap<String, Position> castlePositions) {
		this.castlePositions = castlePositions;
	}

	public HashMap<String, Position> getBishopPositions() {
		return bishopPositions;
	}

	public void setBishopPositions(HashMap<String, Position> bishopPositions) {
		this.bishopPositions = bishopPositions;
	}

	/**
	 * setOntologyPositions
	 * This method is called from AchessGame so that ontologypositions are available
	 * Then determinePieceType is called
	 * @param ontologyPositions
	 */
	public void setOntologyPositions(HashMap<String, Position> ontologyPositions) {
		this.ontologyPositions = ontologyPositions;
		determinePieceType();
	}

	public int getNofMoves() {
		return nofMoves;
	}

	public void setNofMoves(int nofMoves) {
		this.nofMoves = this.nofMoves + 1;
	}

	public List<Integer> getMoveNumbers() {
		return moveNumbers;
	}

	public void setMoveNumbers(List<Integer> moveNumbers) {
		this.moveNumbers = moveNumbers;
	}

	public boolean isActive() {
		return active;
	}

	public List<Position> getRemovedPositions() {
		return removedPositions;
	}

	public void setRemovedPositions(List<Position> removedPositions) {
		this.removedPositions = removedPositions;
	}

	/**
	 * checkRemoved
	 * This method checks if a particular position is removed from the available positions
	 * @since 02.03.21
	 * Checks also position name
	 * @param pos
	 * @return true if removed
	 */
	public boolean checkRemoved(Position pos) {
		boolean removed = false;
		String posName = pos.getPositionName();
		if (removedPositions != null) {
			for (Position position:removedPositions) {
				String pName = position.getPositionName();
				if (pos == position) {
					removed = true;
					break;
				}
				if (posName.equals(pName)) {
					removed = true;
					break;
				}
			}
		}

		return removed;
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
	
	public HashMap<String, Position> getAttackPositions() {
		return attackPositions;
	}

	public void setAttackPositions(HashMap<String, Position> attackPositions) {
		this.attackPositions = attackPositions;
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
				APawn pawn = (APawn) chessType;
//				pawn.setMother(this);
				attackPositions = pawn.getAttackPositions();
				gamePiece = (GamePiece) chessType;
//				gamePiece.g
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
				newlistPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newlistPositions) {
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
				newlistPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newlistPositions) {
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
				newlistPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newlistPositions) {
					System.out.println("Knight positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}	*/
				break;
			case "K":
				myType = pieceType.KING;
				chessType = new Aking(myPosition,myPiece);
				gamePiece = (GamePiece) chessType;
				Aking king = (Aking) chessType;
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
//				reacablePositions = chessType.getNewPositions();
				newlistPositions = new ArrayList(reacablePositions.values());
				if (nofMoves == 0) {
					king.makeCastlemove();
					castlePositions = king.getCastlePositions();
				}
/*				for (Position pos:newlistPositions) {
					System.out.println("King positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;
			case "R":
				myType = pieceType.ROOK;
				chessType = new ARook(myPosition,myPiece);
				String pieceColorR = myPiece.getColor();
				gamePiece = (GamePiece) chessType;
				ARook rook = (ARook) chessType;
				reacablePositions = gamePiece.getNewPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				gamePiece.setOntologyPositions(ontologyPositions);
				if (nofMoves == 0) {
					rook.makeCastlemove();
					castlePositions = rook.getCastlePositions();
				}				
//				reacablePositions = chessType.getNewPositions();
				newlistPositions = new ArrayList(reacablePositions.values());
/*				for (Position pos:newlistPositions) {
					System.out.println(pieceColorR+" Rook positions: "+pos.getPositionName() + " " + pos.getPositionColor());
				}*/
				break;
			case "Q":
				myType = pieceType.QUEEN;
				chessType = new AQueen(myPosition,myPiece);
				String pieceColorQ = myPiece.getColor();
				gamePiece = (GamePiece) chessType;
				AQueen queen = (AQueen) chessType;
				reacablePositions = gamePiece.getNewPositions();
				bishopPositions = queen.getBishopPositions();
				createPosition(reacablePositions); // To replace created positions with ontology positions
				createPosition(bishopPositions);
				gamePiece.setOntologyPositions(ontologyPositions);
//				reacablePositions = chessType.getNewPositions();
				newlistPositions = new ArrayList(reacablePositions.values());
				newlistPositions.addAll(bishopPositions.values());
/*				for (Position pos:newlistPositions) {
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

	public void setMyPosition(Position newPosition) {
		if (this.myPosition != null) {
			heldPositions.push(this.myPosition);
			this.heldPosition = myPosition;
		}
		this.myPosition = newPosition;
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
	 * or in case the piece has been moved as part of a search
	 */
	public void restorePosition() {
/*		if (active && heldPositions != null) {
			heldPositions.clear();
		}*/
		if (heldPositions != null && !heldPositions.isEmpty())
			this.myPosition = heldPositions.pop();
		if (myPosition != null) // Added 1.08.20
			active = true;
		
//		myPosition = heldPosition;
	}
	
	public Position getHeldPosition() {
		if (heldPosition == null && heldPositions == null)
			return null;
		if (heldPosition == null && heldPositions.isEmpty())
			return null;
		if (!heldPositions.isEmpty())
			heldPosition = heldPositions.pop();
		return heldPosition;
	}

	public void setHeldPosition(Position heldPosition) {
		this.heldPosition = heldPosition;
		if (heldPosition == null) {
			heldPositions.clear();
		}
		if (heldPosition != null) {
			heldPositions.push(heldPosition);
		}
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



	public void setNewlistPositions(ArrayList<Position> newPositions) {
		this.newlistPositions = newPositions;
	}

	public ArrayList<Position> getNewlistPositions() {
		return newlistPositions;
	}


	public String toString() {
		StringBuilder result = new StringBuilder();
		String posName = "Removed!!!";
		XYLocation localXY = new XYLocation(0,0);
		String pActive = " Active";
		if (!active)
			pActive = " Taken!!!";
		if (myPosition != null) {
			posName = myPosition.getPositionName();
			localXY = myPosition.getXyloc();
		}
		
		result.append("Piece position "+posName + " X, Y "+localXY.toString()  + " "+myPiece.getName()+ " "+myPiece.getPosition().toString()+" "+ myType+pActive);
	
		String na = myPiece.getPieceName();
		result.append("Name " + na + "\n" + "Available positions\n");
		for (Position pos:newlistPositions) {
			result.append("Position: "+pos.getPositionName() + " " + pos.getPositionColor() + " X, Y "+pos.getXyloc().toString() );
			if (pos.isInUse()) {
				result.append(" Occupied by: "+pos.getUsedBy().getOntlogyName() + "\n");
			}else {
				result.append("\n");
			}
		}
		result.append("Removed positions\n");
		if (removedPositions != null && !removedPositions.isEmpty()) {
			for (Position pos:removedPositions) {
				result.append("Position: "+pos.getPositionName() + " " + pos.getPositionColor() + " X, Y "+pos.getXyloc().toString());
				if (pos.isInUse()) {
					result.append(" Occupied by: "+pos.getUsedBy().getOntlogyName() + "\n");
				}else {
					result.append("\n");
				}

			}	
		}
		if(castlePositions != null && !castlePositions.isEmpty() && (myType == myType.KING || myType == myType.ROOK) && nofMoves == 0) {
			List<Position> castle = new ArrayList(castlePositions.values());
			result.append("castle positions\n");
			for (Position pos:castle) {
				result.append("Position: "+pos.getPositionName() + " " + pos.getPositionColor() + " X, Y "+pos.getXyloc().toString());
				if (pos.isInUse()) {
					result.append(" Occupied by: "+pos.getUsedBy().getOntlogyName() + "\n");
				}else {
					result.append("\n");
				}
			}
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
		newlistPositions = null;
		newlistPositions = new ArrayList(reacablePositions.values());
//		newlistPositions =  (List<Position>) reacablePositions.values();
	}

	/* (non-Javadoc)
	 * @see no.games.chess.AbstractGamePiece#produceLegalmoves(java.lang.Object)
	 */
	@Override
	public void produceLegalmoves(Position position) {

		if (chessType instanceof APawn) {
			APawn p = (APawn) chessType;
			p.produceLegalmoves(position);
			attackPositions = p.getAttackPositions();
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
		newlistPositions = null;
		newlistPositions = new ArrayList(reacablePositions.values());
		
	}

	public boolean checkPositions() {
		boolean newPos = false;
		if (newlistPositions == null || newlistPositions.isEmpty()) {
			newlistPositions = null;
			newlistPositions = new ArrayList(reacablePositions.values());
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
