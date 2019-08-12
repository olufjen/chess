package no.chess.web.model.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import no.chess.web.model.Position;
import no.games.chess.AbstractgameBoard;

/**
 * This is the implementation of the Abstractgameboard class
 * The gameboard creates new lists. The original HashMap Positions is intact.
 * @author oluf
 *
 * @param <P>
 */
public class AgameBoard<P> extends AbstractgameBoard<P> {


	private HashMap<String,Position> availablePositions;
	private HashMap<String,Position> allPositions; // All positions on the board
	
	public AgameBoard(HashMap<String, Position> positions) {
		super((HashMap<String, P>) positions);
		this.allPositions = positions;
		positionlist = new ArrayList(positions.values());
		setusedunused();
		availablePositions = new HashMap();
		availablePositionlist = new ArrayList();
	}

	public void setusedunused() {
		usedPositionlist = (List<P>) ((List<Position>) positionlist).stream().filter(Position::isInUse).collect(Collectors.toList());
		notusedPositionlist = (List<P>) ((List<Position>) positionlist).stream().filter(Position::notisInUse).collect(Collectors.toList());

//		notusedPositions = (List<Position>) ((List<Position>) positions).stream().filter(board.queenExistsAt(position.getXyloc())).collect(Collectors.toList());
	}

	public HashMap<String, Position> getAvailablePositions() {
		return availablePositions;
	}

	public void setAvailablePositions(HashMap<String, Position> availablePositions) {
		this.availablePositions = availablePositions;
	}

	public HashMap<String, Position> getAllPositions() {
		return allPositions;
	}

	public void setAllPositions(HashMap<String, Position> allPositions) {
		this.allPositions = allPositions;
	}
	
}
