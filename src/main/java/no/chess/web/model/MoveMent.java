package no.chess.web.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import aima.core.environment.nqueens.NQueensBoard;
import no.chess.ontology.BlackPiece;
import no.chess.ontology.WhitePiece;

/**
 * This class contains a (aima) chessboard with a solution to a chess problem.
 * When a piece is moved it is placed in the correct HashSet of moved pieces.
 * The piece remains there until it is free to move again.
 * @author oluf
 *
 */
public class MoveMent {

	private	NQueensBoard board = null;
	private HashSet<BlackPiece> movedblackPieces;
	private HashSet<WhitePiece> movedwhitePieces;
	private HashMap<String,Position> positions;
	private HashMap<String,Position> usedPositions;
	private HashMap<String,Position> notusedPositions;
	private HashMap<String,Position> availablePositions;
	private List<Position> usedPositionlist;
	private List<Position> notusedPositionlist;
	private List<Position> availablePositionlist;
	private List<Position> positionlist; // THe original HashMap of positions as a list
	
	public MoveMent(NQueensBoard board, HashMap<String, Position> positions) {
		super();
		this.board = board;
		this.positions = positions;
		positionlist = new ArrayList(positions.values());
		setusedunused();
		availablePositions = new HashMap();
		availablePositionlist = new ArrayList();
	}
	
	public HashMap<String, Position> getUsedPositions() {
		return usedPositions;
	}

	public void setUsedPositions(HashMap<String, Position> usedPositions) {
		this.usedPositions = usedPositions;
	}

	public HashMap<String, Position> getNotusedPositions() {
		return notusedPositions;
	}

	public void setNotusedPositions(HashMap<String, Position> notusedPositions) {
		this.notusedPositions = notusedPositions;
	}

	public HashMap<String, Position> getAvailablePositions() {
		return availablePositions;
	}

	public void setAvailablePositions(HashMap<String, Position> availablePositions) {
		this.availablePositions = availablePositions;
	}

	public List<Position> getUsedPositionlist() {
		return usedPositionlist;
	}

	public void setUsedPositionlist(List<Position> usedPositionlist) {
		this.usedPositionlist = usedPositionlist;
	}

	public List<Position> getNotusedPositionlist() {
		return notusedPositionlist;
	}

	public void setNotusedPositionlist(List<Position> notusedPositionlist) {
		this.notusedPositionlist = notusedPositionlist;
	}

	public List<Position> getAvailablePositionlist() {
		return availablePositionlist;
	}

	public void setAvailablePositionlist(List<Position> availablePositionlist) {
		this.availablePositionlist = availablePositionlist;
	}

	public List<Position> getPositionlist() {
		return positionlist;
	}

	public void setPositionlist(List<Position> positionlist) {
		this.positionlist = positionlist;
	}

	public HashMap<String, Position> getPositions() {
		return positions;
	}
	public void setPositions(HashMap<String, Position> positions) {
		this.positions = positions;
	}
	public NQueensBoard getBoard() {
		return board;
	}
	public void setBoard(NQueensBoard board) {
		this.board = board;
	}
	public HashSet<BlackPiece> getMovedblackPieces() {
		return movedblackPieces;
	}
	public void setMovedblackPieces(HashSet<BlackPiece> movedblackPieces) {
		this.movedblackPieces = movedblackPieces;
	}
	public HashSet<WhitePiece> getMovedwhitePieces() {
		return movedwhitePieces;
	}
	public void setMovedwhitePieces(HashSet<WhitePiece> movedwhitePieces) {
		this.movedwhitePieces = movedwhitePieces;
	}
	
	public void setusedunused() {
		usedPositionlist = (List<Position>) ((List<Position>) positionlist).stream().filter(Position::isInUse).collect(Collectors.toList());
		notusedPositionlist = (List<Position>) ((List<Position>) positionlist).stream().filter(Position::notisInUse).collect(Collectors.toList());

//		notusedPositions = (List<Position>) ((List<Position>) positions).stream().filter(board.queenExistsAt(position.getXyloc())).collect(Collectors.toList());
	}
	public void setQueensPositions() {
		StringBuilder result = new StringBuilder();
		for (Position position:usedPositionlist) {
			if (!board.queenExistsAt(position.getXyloc())) {
				availablePositionlist.add(position);
				position.checkUsed();
			}
			
		}
		int i = 0;
		int max = availablePositionlist.size();
		for (Position position:notusedPositionlist) {
			if (board.queenExistsAt(position.getXyloc()) && i <= max) {
				Position available = availablePositionlist.get(i);
				position.setUsedBy(available.getUsedBy());
				position.setPieces( available.getPieces());
				position.setInUse(true);
				available.setPieces(null);
				available.setUsedBy();
//				available.setInUse(false);
				available.checkUsed();
				position.checkUsed();
				i++;
				result.append("\n From position ").append(available.getPositionName()).append(" To ").append(position.getPositionName());
			}else {
				position.checkUsed();
			}
			if (i>max) {
				result.append("\n No more available positions ").append(i).append(" max: ").append(max);
			}
		}
		System.out.println(result.toString());
		
	}
	
}
