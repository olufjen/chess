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
 * @since 14.10.20 Positions available for attack is added
 * @since Jan 2025 : Attack positions are added to available positions. See createAttackposition
 * The HashMap friendPositions are added
 * @author oluf
 * @param <P>
 *
 */
public class APawn extends AbstractGamePiece<Position>  implements ChessPieceType {

	private pieceType localType = pieceType.PAWN;
	private static final String chessType = "PAWN"; // A class variable
	private pieceColor localColor;
	private int[][] reachablesqueres;
	private String[][] reachablepiecePosition;
	private HashMap<String,Position> newPositions; // contains positions reachable by the piece 
	private HashMap<String,Position> attackPositions; // contains positions that can be attacked by the piece The key is the position name
	private HashMap<String,Position> ontologyPositions; // Represent the ontology positions
	private HashMap<String,Position> friendPositions; // Represent positions occupied by friendly pieces
	private int size = 8;
	private String color;
	private ChessPiece myPiece;
	private Position myPosition;
	private boolean blocked = false;
	private AgamePiece mother = null; // Not necessary
	
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
		friendPositions = new HashMap<String,Position>();
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
		value= 1;
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
		friendPositions = new HashMap<String,Position>();
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
		friendPositions = new HashMap<String,Position>();
		getLegalmoves(myPosition);
	}


	public static String getChesstype() {
		return chessType;
	}


	public HashMap<String, Position> getFriendPositions() {
		return friendPositions;
	}


	public void setFriendPositions(HashMap<String, Position> friendPositions) {
		this.friendPositions = friendPositions;
	}


	public AgamePiece getMother() {
		return mother;
	}


	public void setMother(AgamePiece mother) {
		this.mother = mother;
	}


	public HashMap<String, Position> getAttackPositions() {
		return attackPositions;
	}


	public void setAttackPositions(HashMap<String, Position> attackPositions) {
		this.attackPositions = attackPositions;
	}


	public boolean isBlocked() {
		return blocked;
	}


	public void setBlocked(boolean blocked) {
		this.blocked = blocked;
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
	 * produceAttack
	 * This method produces Pawn attack positions given a position on the board
	 * @param pos
	 * @return The attack positions in a list
	 */
	public List<Position> produceAttack(Position pos) {
		int diff = -1;
		if (localColor == pieceColor.BLACK) {
			diff = 1;
		}
		XYLocation loc = pos.getXyloc();
		int x = loc.getXCoOrdinate() - 1;
		int y = loc.getYCoOrdinate() + diff;
		Position attackOne = null;
		Position attackTwo = null;
		XYLocation attacklocLeft = null;
		XYLocation attackRight = null;
		List<Position> attackPositions = new ArrayList<Position>();
		if (x >= 0 && y >= 0 ) {
			attacklocLeft = new XYLocation(x,y);
			attackOne = new Position(attacklocLeft,false,null);
			attackPositions.add(attackOne);
		}
		int xRight = loc.getXCoOrdinate() + 1;
		if (xRight <= 7 && y >= 0) {
			attackRight = new XYLocation(xRight,y);
			attackTwo = new Position(attackRight,false,null);
			attackPositions.add(attackTwo);
		}
		return attackPositions;
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
		List<XYLocation> attackLocations = pawnRules.getAttackPositions();
		if (newPositions == null)
			newPositions = new HashMap();
		if (attackPositions == null)
			attackPositions = new HashMap();
		for (XYLocation xloc:locations) {
			int x = xloc.getXCoOrdinate();
			int y = xloc.getYCoOrdinate();
			reachablesqueres[x][y] = 1;
			reachablepiecePosition[x][y] = "P";
			createPosition(newPositions,xloc);
		}
		for (XYLocation xloc:attackLocations) {
			int x = xloc.getXCoOrdinate();
			int y = xloc.getYCoOrdinate();
			reachablesqueres[x][y] = 1;
			reachablepiecePosition[x][y] = "P";
			createattackPosition(attackPositions,xloc);
		}
		
	}
	/**
	 * createPosition
	 * This method creates a new chess position based on a XYLocation
	 * @param newPositions
	 * @param x
	 * @param y
	 */
	private void createPosition(HashMap<String,Position> newPositions,XYLocation newloc) {
//		XYLocation newloc = new XYLocation(x,y);
		Position newPosxyp = new Position(newloc,false,null);
		newPositions.put(newPosxyp.getPositionName(), newPosxyp);
	}
	/**
	 * checkPawnremovals
	 * This method moves the removed list to the processor's removed list
	 * @since 01.02.22 Added a removed position at start
	 * @since 08.01.25 Attack positions must be considered as part of available positions
	 * @param availablePositions
	 * @param removedPositions
	 * @return
	 */
	public List<Position> checkPawnremovals(List<Position>availablePositions,List<Position>removedPositions){
		List<Position> removedList = new ArrayList();
		List<Position> attack = new ArrayList(attackPositions.values());
		boolean pawnColor = localColor == pieceColor.WHITE;
		XYLocation loc =  myPosition.getXyloc();
		int y = loc.getYCoOrdinate();
		if(removedPositions != null && !removedPositions.isEmpty()) {
			Position rem = removedPositions.get(0);
			String name = rem.getPositionName();
			Position attackPos =  (Position) attack.stream().filter(c -> c.getPositionName().contains(name)).findAny().orElse(null); // Do not put position in removed table if it is there already
			boolean inT = attackPos == null; // If the removed position is not part of attack positions !!!!
			if (pawnColor && y == 1) {
				blocked = true;
			}
			if (!pawnColor && y == 6) {
				blocked = true;
			}
			for (Position pos:availablePositions) {
				if (inT && pos != rem) {
					removedList.add(pos);
				}
			}
		}
		removedList.addAll(removedPositions);
		return removedList;
	}
	/**
	 * createattackPosition
	 * This method creates a new chess position based on a XYLocation
	 * @since Jan 2025 : Attack positions are added to available positions
	 * @param newPositions
	 * @param x
	 * @param y
	 */
	private void createattackPosition(HashMap<String,Position> attackPositions,XYLocation newloc) {
//		XYLocation newloc = new XYLocation(x,y);
		Position newPosxyp = new Position(newloc,false,null);
		attackPositions.put(newPosxyp.getPositionName(), newPosxyp);
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
		attackPositions.clear();
		getLegalmoves(position);
		createontPosition(newPositions);
		createontPosition(attackPositions);
	}
	/**
	 * createontPosition
	 * This method moves any ontologypositions to the list of positions reachable by this piece
	 * It is called from the determinPieceType method of the AgamePiece object and the produceLegalmoves method
	 * when the piece is moved to a new position.
	 * @param xPositions a HashMap of positions calculated by the piecetype

	 */
	protected void createontPosition(HashMap<String,Position> xPositions) {
//		XYLocation newloc = new XYLocation(x,y);
		List<Position> tempPositions = new ArrayList(xPositions.values());
		for (Position pos : tempPositions) {
			String name = pos.getPositionName();
			Position ontPosition = ontologyPositions.get(name);
			if (ontPosition != null) {
				xPositions.put(name, ontPosition);
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
