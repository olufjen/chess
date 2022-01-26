package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import aima.core.util.datastructure.XYLocation;
import no.chess.web.model.ChessPiece;
import no.chess.web.model.Position;
import no.games.chess.AbstractGamePiece;
import no.games.chess.ChessFunctions;
import no.games.chess.ChessPieceType;
import no.games.chess.GamePiece;
import no.games.chess.AbstractGamePiece.pieceColor;

/**
 * This class represent the Rook chesspiece 
 * It implements the method legalMoves for the Rook 
 * @author oluf
 * @param <P>
 *
 */
public class ARook extends AbstractGamePiece<Position>  implements ChessPieceType {

	private pieceType localType = pieceType.ROOK;
	private pieceColor localColor;
	private int[][] reachablesqueres;
	private String[][] reachablepiecePosition;
	HashMap<String,Position> newPositions;
	private HashMap<String,Position> ontologyPositions; // Represent the ontology positions
	private int[][] castlesqueres;
	private String[][] castlepositions;
	private HashMap<String,Position> castlePositions;
	private HashMap<String,Position> friendPositions; // Represent positions occupied by friendly pieces
	private int size = 8;
	private String color;
	private ChessPiece myPiece;
	private Position myPosition;
	private int castlex = 3;
	private int castley = 0;
	private int castlexx = 5;
	
	public ARook() {
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

	public ARook(Position myPosition, ChessPiece myPiece) {
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
		value= 5;
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
		if (castlePositions == null)
			castlePositions = new HashMap();
		friendPositions = new HashMap<String,Position>();
		getLegalmoves(myPosition);
	}

	public ARook(Position myPosition) {
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
		if (castlePositions == null)
			castlePositions = new HashMap();
		friendPositions = new HashMap<String,Position>();
		getLegalmoves(myPosition);
	}

	public HashMap<String, Position> getFriendPositions() {
		return friendPositions;
	}

	public void setFriendPositions(HashMap<String, Position> friendPositions) {
		this.friendPositions = friendPositions;
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
	 * This method creates positions for the castle move for the Rook
	 */
	public void makeCastlemove() {
		castlesqueres[castlex][castley] = 1;
		castlepositions[castlex][castley] = "K";
		XYLocation newloc = new XYLocation(castlex,castley);
		createPosition(castlePositions, newloc);
		castlesqueres[castlexx][castley] = 1;
		castlepositions[castlexx][castley] = "K";
		XYLocation newlocx = new XYLocation(castlexx,castley);
		createPosition(castlePositions, newlocx);
		createontPosition(castlePositions);
	}

	/**
	 * getLegalmoves
	 * This method returns reachable positions by the Rook chesspiece
	 * @param position
	 * @return
	 */
	public void getLegalmoves(Position position){
		XYLocation loc = position.getXyloc();
		String posName = position.getPositionName();
		ARookMoveRule moveRule = new ARookMoveRule();
		List<XYLocation> locations = ChessFunctions.moveRule(this, moveRule);
		if (newPositions == null)
			newPositions = new HashMap();
		for (XYLocation xloc:locations) {
			int x = xloc.getXCoOrdinate();
			int y = xloc.getYCoOrdinate();
			reachablesqueres[x][y] = 1;
			reachablepiecePosition[x][y] = "P";
			createPosition(newPositions, xloc);
		}
/*		newPositions = new HashMap();
		int x = loc.getXCoOrdinate();
		int y = loc.getYCoOrdinate();
		for (int i = y;i<8;i++) {
			reachablesqueres[x][i] = 1;
			reachablepiecePosition[x][i] = "R";
			createPosition(newPositions, x,i);
		}
		for (int i = x;i<8;i++) {
			reachablesqueres[i][y] = 1;
			reachablepiecePosition[i][y] = "R";
			createPosition(newPositions,i,y);
		}
		for (int i = y;i>0;i--) {
			reachablesqueres[x][i] = 1;
			reachablepiecePosition[x][i] = "R";
			createPosition(newPositions, x,i);
		}
		for (int i = x;i>0;i--) {
			reachablesqueres[i][y] = 1;
			reachablepiecePosition[i][y] = "R";
			createPosition(newPositions, i,y);
		}*/
		
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
	 * addPositions
	 * This is a support method for the checkRemovals method
	 * @param availablePositions
	 * @param removedPositions
	 * @return
	 */
	public List<Position>addPositions(List<Position>availablePositions,List<Position>removedPositions){
		Optional<Position> minpos = Optional.empty();
		Optional<Position> availmin = Optional.empty();
		Position miny = null;
		List<Position>friendlyList = new ArrayList<Position>();
		List<Position>tempAvail2 = new ArrayList();
		List<Position>tempAvail = new ArrayList<Position>();
		List<Position>tempList = new ArrayList<Position>();
		if (removedPositions != null && !removedPositions.isEmpty()) {
			minpos = removedPositions.stream().reduce((p1,p2) -> p1.getSumDif() < p2.getSumDif() ? p1 : p2);
		}
		if (minpos.isPresent()) {
			Position minx = minpos.get();
			if (localColor == pieceColor.WHITE) {
				System.out.println("CheckRemovals The min position: "+minx.toString());
			}
			tempList = removedPositions.stream().filter(p -> minx.getMydirection() == p.getMydirection()).collect(Collectors.toList());
			// Find all available positions that have the same direction:
			tempAvail = availablePositions.stream().filter(p -> minx.getMydirection() == p.getMydirection()).collect(Collectors.toList());
			if (tempAvail != null && !tempAvail.isEmpty()) {
				tempAvail2 = tempAvail.stream().filter(p -> minx.getSumDif() < p.getSumDif()).collect(Collectors.toList());
			}
			if (tempAvail2 != null && !tempAvail2.isEmpty()) {
				availmin = tempAvail2.stream().reduce((p1,p2) -> p1.getSumDif() < p2.getSumDif() ? p1 : p2);
			}
			if (availmin.isPresent()){
				Position availx = availmin.get();
				int nx = minx.getSumDif().intValue();
				int ny = availx.getSumDif().intValue();
				if (tempAvail != null && !tempAvail.isEmpty()) {
					friendlyList = tempAvail.stream().filter(p -> availx.getSumDif() < p.getSumDif()).collect(Collectors.toList());
				}
				if (nx < ny) {
					friendlyList = null;
					friendlyList = tempAvail.stream().filter(p -> minx.getSumDif() < p.getSumDif()).collect(Collectors.toList());
				}
				if (friendlyList != null && !friendlyList.isEmpty()) {
					for (Position friend:friendlyList) {
						String name = friend.getPositionName();
						friendPositions.remove(name);
					//	friend.setFriendlyPosition(false);
					}
				}
			}
			tempList.addAll(tempAvail2);
			// must do the same with available positions !!!!
		}

	
		return tempList;
	}
	/**
	 * checkNorthSouthremovals
	 * This method returns a list of removed positions based on all available and
	 * calculated removed positions. It is used for the north/south and east/west directions
	 * @param availablePositions
	 * @param removedPositions
	 * @return
	 */
	public List<Position> checkNorthSouthremovals(List<Position>availablePositions,List<Position>removedPositions){
		List<Position>tempList = new ArrayList<Position>();
		List<Position>tempAvail = new ArrayList<Position>();
		XYLocation heldLoc = myPosition.getXyloc();
		int x = heldLoc.getXCoOrdinate();
		int y = heldLoc.getYCoOrdinate();
		for (Position removed:removedPositions) {
			XYLocation remloc = removed.getXyloc();
			int ry = remloc.getYCoOrdinate();
			int ydiff = y - ry;//Math.abs(y-ry);
			Integer sumDif = new Integer(Math.abs(ydiff));
			if (ydiff < 0) {
				removed.setSumNorth(ydiff);
				removed.setSumDif(sumDif);
			}
			if (ydiff > 0) {
				removed.setSumSouth(ydiff);
				removed.setSumDif(sumDif);
			}
			int rx = remloc.getXCoOrdinate();
			int xdiff = x - rx;
			Integer sumdifx = new Integer(Math.abs(xdiff));
			if (xdiff < 0) {
				removed.setSumWest(xdiff);
				removed.setSumDif(sumdifx);
			}
			if (xdiff > 0) {
				removed.setSumEast(xdiff);	
				removed.setSumDif(sumdifx);
			}
			removed.setLastDirection(removed.getNorthDirection());
		}
		for (Position avail:availablePositions) {
			XYLocation remloc = avail.getXyloc();
			int rx = remloc.getXCoOrdinate();
			int ry = remloc.getYCoOrdinate();
			int ydiff = y - ry;//Math.abs(y-ry);
			Integer sumDif = new Integer(Math.abs(ydiff));
			if (ydiff < 0) {
				avail.setSumNorth(ydiff);
				avail.setSumDif(sumDif);
			}
			if (ydiff > 0) {
				avail.setSumSouth(ydiff);
				avail.setSumDif(sumDif);
			}
			int xdiff = x - rx;
			Integer sumdifx = new Integer(Math.abs(xdiff));
			if (xdiff < 0) {
				avail.setSumWest(xdiff);
				avail.setSumDif(sumdifx);
			}
			if (xdiff > 0) {
				avail.setSumEast(xdiff);
				avail.setSumDif(sumdifx);
			}
		}
		List<Position>northRemoved = removedPositions.stream().filter(p -> p.getMydirection() == p.getLastDirection()).collect(Collectors.toList());
		tempList.addAll(addPositions(availablePositions,northRemoved));
		for (Position removed:removedPositions) {
			removed.setLastDirection(removed.getWestDirection());
		}
		List<Position>westRemoved = removedPositions.stream().filter(p -> p.getMydirection() == p.getLastDirection()).collect(Collectors.toList());
		tempList.addAll(addPositions(availablePositions,westRemoved));
		for (Position removed:removedPositions) {
			removed.setLastDirection(removed.getEastDirection());
		}
		List<Position>eastRemoved = removedPositions.stream().filter(p -> p.getMydirection() == p.getLastDirection()).collect(Collectors.toList());
		tempList.addAll(addPositions(availablePositions,eastRemoved));
		for (Position removed:removedPositions) {
			removed.setLastDirection(removed.getSouthDirection());
		}
		List<Position>southRemoved = removedPositions.stream().filter(p -> p.getMydirection() == p.getLastDirection()).collect(Collectors.toList());
		tempList.addAll(addPositions(availablePositions,southRemoved));
		return tempList;
	}
	@Override
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

	@Override
	public void produceLegalmoves(Position position) {
		newPositions.clear();
		myPosition = position;
		getLegalmoves(position);
		createontPosition(newPositions);
	}
	/**
	 * createPosition
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
